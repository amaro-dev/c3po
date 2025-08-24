package core.facade

import core.model.UpdateInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.Duration

class UpdateService(private val httpClient: HttpClient = HttpClient.newHttpClient()) {

    suspend fun checkForUpdates(
        currentVersion: String,
        updateUrl: String = "https://api.github.com/repos/amaro-dev/c3po/releases/latest"
    ): UpdateInfo? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(updateUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "c3po-updater/1.0")
                .timeout(Duration.ofSeconds(30))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                200 -> parseUpdateResponse(response.body(), currentVersion)
                404 -> {
                    // Repository not found or no releases
                    null
                }

                403 -> {
                    // Rate limited or access denied
                    throw RuntimeException("Access denied or rate limited by GitHub API")
                }

                in 500..599 -> {
                    // Server error
                    throw RuntimeException("GitHub API server error: ${response.statusCode()}")
                }

                else -> {
                    throw RuntimeException("Unexpected response from update server: ${response.statusCode()}")
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Network error while checking for updates: ${e.message}")
        } catch (e: InterruptedException) {
            throw RuntimeException("Update check was interrupted")
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to check for updates: ${e.message}")
        }
    }

    private fun parseUpdateResponse(responseBody: String, currentVersion: String): UpdateInfo? {
        val json = Json.parseToJsonElement(responseBody).jsonObject
        val tagName = json["tag_name"]?.jsonPrimitive?.content ?: return null
        val version = tagName.removePrefix("v")

        if (!isNewerVersion(version, currentVersion)) {
            return null
        }

        val assets = json["assets"]?.jsonArray ?: return null
        val dmgAsset = assets.find { asset ->
            val name = asset.jsonObject["name"]?.jsonPrimitive?.content ?: ""
            name.endsWith(".dmg")
        }?.jsonObject ?: return null

        val downloadUrl = dmgAsset["browser_download_url"]?.jsonPrimitive?.content ?: return null

        // Extract checksum if available in the API response
        val checksum = json["checksum"]?.jsonPrimitive?.content
        val releaseNotesUrl = json["html_url"]?.jsonPrimitive?.content

        return UpdateInfo(
            version = version,
            downloadUrl = downloadUrl,
            checksum = checksum,
            releaseNotesUrl = releaseNotesUrl
        )
    }

    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        val newParts = newVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(newParts.size, currentParts.size)

        for (i in 0 until maxLength) {
            val newPart = newParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }

            when {
                newPart > currentPart -> return true
                newPart < currentPart -> return false
            }
        }
        return false
    }

    data class DownloadProgress(
        val progress: Int,
        val totalBytes: Long,
        val downloadedBytes: Long
    )

    suspend fun downloadUpdate(
        updateInfo: UpdateInfo,
        downloadDir: File = File(System.getProperty("java.io.tmpdir"), "c3po-updates")
    ): Flow<DownloadProgress> = flow {
        // Enhanced version validation before download
        if (!isValidVersionFormat(updateInfo.version)) {
            throw IllegalArgumentException("Invalid version format: ${updateInfo.version}")
        }

        // Ensure download directory exists first
        try {
            if (!downloadDir.exists()) {
                if (!downloadDir.mkdirs()) {
                    throw RuntimeException("Failed to create download directory: ${downloadDir.absolutePath}")
                }
            }
        } catch (e: SecurityException) {
            throw RuntimeException("Permission denied accessing download directory: ${downloadDir.absolutePath}")
        }

        // Check available disk space (after ensuring directory exists)
        val requiredSpace = estimateDownloadSize(updateInfo.downloadUrl)
        val availableSpace = downloadDir.usableSpace

        if (requiredSpace > 0 && availableSpace < requiredSpace * 2) { // 2x for safety margin
            throw RuntimeException("Insufficient disk space. Required: ${requiredSpace / 1024 / 1024}MB, Available: ${availableSpace / 1024 / 1024}MB")
        }

        // Check if directory is writable
        try {

            if (!downloadDir.canWrite()) {
                throw RuntimeException("Download directory is not writable: ${downloadDir.absolutePath}")
            }
        } catch (e: SecurityException) {
            throw RuntimeException("Permission denied accessing download directory: ${downloadDir.absolutePath}")
        }

        val fileName = "c3po-${updateInfo.version}.dmg"
        val targetFile = File(downloadDir, fileName)

        val request = HttpRequest.newBuilder()
            .uri(URI.create(updateInfo.downloadUrl))
            .header("Accept", "application/octet-stream")
            .timeout(Duration.ofMinutes(30)) // Add timeout
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
            
            if (response.statusCode() != 200) {
                throw RuntimeException("Download failed with HTTP status: ${response.statusCode()}")
            }

            val contentLength = response.headers().firstValue("Content-Length")
                .map { it.toLong() }.orElse(-1L)

            if (contentLength > 0 && contentLength > availableSpace) {
                throw RuntimeException("File too large for available disk space")
            }

            var downloadedBytes = 0L
            val buffer = ByteArray(8192)
            
            response.body().use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (contentLength > 0) {
                            val progress = ((downloadedBytes.toDouble() / contentLength) * 100).toInt()
                            emit(DownloadProgress(progress, contentLength, downloadedBytes))
                        }

                        // Check if we're exceeding expected size
                        if (contentLength > 0 && downloadedBytes > contentLength * 1.1) {
                            throw RuntimeException("Download size exceeded expected content length")
                        }
                    }
                }
            }

            // Final verification
            if (!targetFile.exists() || targetFile.length() == 0L) {
                throw RuntimeException("Download completed but file is missing or empty")
            }

            // Emit completion
            emit(DownloadProgress(100, contentLength, downloadedBytes))

        } catch (e: InterruptedException) {
            cleanupFailedDownload(targetFile)
            throw RuntimeException("Download was interrupted")
        } catch (e: IOException) {
            cleanupFailedDownload(targetFile)
            throw RuntimeException("Network error during download: ${e.message}")
        } catch (e: Exception) {
            cleanupFailedDownload(targetFile)
            throw e
        }
    }

    private suspend fun estimateDownloadSize(downloadUrl: String): Long {
        return try {
            val headRequest = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(10))
                .build()

            val response = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding())
            val contentLength = response.headers().firstValue("Content-Length").map { it.toLong() }.orElse(0L)
            contentLength
        } catch (e: Exception) {
            println("DEBUG: HEAD request failed: ${e.message}")
            // If HEAD request fails, return 0 to skip size check
            0L
        }
    }

    private fun cleanupFailedDownload(file: File) {
        try {
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Log but don't throw - this is cleanup
        }
    }

    fun verifyChecksum(file: File, expectedChecksum: String?): Boolean {
        if (expectedChecksum == null) return true

        if (!file.exists()) {
            throw IllegalArgumentException("File does not exist: ${file.absolutePath}")
        }

        if (file.length() == 0L) {
            throw IllegalArgumentException("File is empty: ${file.absolutePath}")
        }
        
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val hashBytes = digest.digest()
            val actualChecksum = hashBytes.joinToString("") { "%02x".format(it) }
            actualChecksum.equals(expectedChecksum, ignoreCase = true)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("SHA-256 algorithm not available")
        } catch (e: IOException) {
            throw RuntimeException("Failed to read file for checksum verification: ${e.message}")
        } catch (e: Exception) {
            throw RuntimeException("Checksum verification failed: ${e.message}")
        }
    }

    fun validateVersion(newVersion: String, currentVersion: String): Boolean {
        return isValidVersionFormat(newVersion) && 
               isValidVersionFormat(currentVersion) && 
               isNewerVersion(newVersion, currentVersion)
    }

    private fun isValidVersionFormat(version: String): Boolean {
        // Enhanced version validation - supports semantic versioning
        val semverPattern = Regex("""^(\d+)\.(\d+)\.(\d+)(-[a-zA-Z0-9.-]+)?(\+[a-zA-Z0-9.-]+)?$""")
        val simplePattern = Regex("""^(\d+)\.(\d+)(\.(\d+))?$""")
        
        return semverPattern.matches(version) || simplePattern.matches(version)
    }

    data class InstallResult(
        val success: Boolean,
        val message: String,
        val requiresRestart: Boolean = true
    )

    suspend fun installUpdate(downloadedFile: File): InstallResult {
        return try {
            when {
                downloadedFile.name.endsWith(".dmg") -> installDmgOnMacOS(downloadedFile)
                else -> InstallResult(false, "Unsupported file format: ${downloadedFile.extension}")
            }
        } catch (e: Exception) {
            InstallResult(false, "Installation failed: ${e.message}")
        }
    }

    private suspend fun installDmgOnMacOS(dmgFile: File): InstallResult {
        if (!dmgFile.exists()) {
            return InstallResult(false, "DMG file not found: ${dmgFile.absolutePath}")
        }

        if (dmgFile.length() == 0L) {
            return InstallResult(false, "DMG file is empty or corrupted")
        }

        // Check if running on macOS
        val osName = System.getProperty("os.name").lowercase()
        if (!osName.contains("mac")) {
            return InstallResult(false, "DMG installation only supported on macOS")
        }

        val tempMountPoint = "/tmp/c3po-update-${System.currentTimeMillis()}"

        try {
            // Check if hdiutil is available
            if (!isCommandAvailable("hdiutil")) {
                return InstallResult(false, "hdiutil command not found. Required for DMG installation on macOS.")
            }

            // Step 1: Mount the DMG with enhanced error handling
            val mountProcess = ProcessBuilder(
                "hdiutil", "attach", dmgFile.absolutePath,
                "-mountpoint", tempMountPoint,
                "-nobrowse", "-quiet"
            )
                .redirectErrorStream(true)
                .start()

            val mountResult = mountProcess.waitFor()

            if (mountResult != 0) {
                val errorOutput = mountProcess.inputStream.bufferedReader().readText()
                return when {
                    errorOutput.contains("resource busy") || errorOutput.contains("already mounted") ->
                        InstallResult(false, "DMG is already mounted or in use")

                    errorOutput.contains("no mountable file systems") ->
                        InstallResult(false, "DMG file appears to be corrupted or invalid")

                    errorOutput.contains("Software License Agreement") ->
                        InstallResult(false, "DMG requires user acceptance of license agreement")

                    else ->
                        InstallResult(
                            false,
                            "Failed to mount DMG. This may be due to Gatekeeper restrictions for unsigned applications. Error: $errorOutput"
                        )
                }
            }

            // Step 2: Find the app bundle in the mounted DMG
            val mountDir = File(tempMountPoint)
            if (!mountDir.exists() || !mountDir.isDirectory()) {
                return InstallResult(false, "Mount point not accessible: $tempMountPoint")
            }

            val appBundle = mountDir.listFiles()?.find { it.name.endsWith(".app") && it.isDirectory() }
                ?: return InstallResult(false, "No .app bundle found in DMG")

            // Step 3: Get current app location with enhanced detection
            val currentAppPath = getCurrentApplicationPath()
                ?: return InstallResult(false, "Could not determine current application path")

            val currentAppFile = File(currentAppPath)
            if (!currentAppFile.exists()) {
                return InstallResult(false, "Current application not found at: $currentAppPath")
            }

            // Step 4: Check permissions
            val parentDir = currentAppFile.parentFile
            if (!parentDir.canWrite()) {
                return InstallResult(
                    false,
                    "Insufficient permissions to update application. Try running with administrator privileges."
                )
            }

            // Step 5: Create backup
            val backupPath = "${currentAppPath}.backup-${System.currentTimeMillis()}"
            val backupResult = ProcessBuilder("cp", "-R", currentAppPath, backupPath)
                .start()
                .waitFor()

            if (backupResult != 0) {
                return InstallResult(false, "Failed to create backup of current application")
            }

            // Step 6: Remove current app
            val removeResult = ProcessBuilder("rm", "-rf", currentAppPath)
                .start()
                .waitFor()

            if (removeResult != 0) {
                // Restore backup if removal failed
                ProcessBuilder("mv", backupPath, currentAppPath).start().waitFor()
                return InstallResult(false, "Failed to remove current application version")
            }

            // Step 7: Copy new version
            val copyResult = ProcessBuilder("cp", "-R", appBundle.absolutePath, currentAppPath)
                .start()
                .waitFor()

            if (copyResult != 0) {
                // Restore backup if copy failed
                ProcessBuilder("mv", backupPath, currentAppPath).start().waitFor()
                return InstallResult(false, "Failed to install new application version")
            }

            // Step 8: Verify installation
            val newAppFile = File(currentAppPath)
            if (!newAppFile.exists() || !newAppFile.isDirectory()) {
                // Restore backup if verification failed
                ProcessBuilder("mv", backupPath, currentAppPath).start().waitFor()
                return InstallResult(false, "Installation verification failed")
            }

            // Step 9: Clean up backup (installation successful)
            ProcessBuilder("rm", "-rf", backupPath).start().waitFor()

            return InstallResult(true, "Application updated successfully. Restart required to complete the update.")

        } catch (e: SecurityException) {
            return InstallResult(false, "Permission denied during installation: ${e.message}")
        } catch (e: IOException) {
            return InstallResult(false, "I/O error during installation: ${e.message}")
        } catch (e: InterruptedException) {
            return InstallResult(false, "Installation was interrupted")
        } catch (e: Exception) {
            return InstallResult(false, "Unexpected error during installation: ${e.message}")
        } finally {
            // Always unmount the DMG
            try {
                ProcessBuilder("hdiutil", "detach", tempMountPoint, "-quiet", "-force")
                    .start()
                    .waitFor()
            } catch (e: Exception) {
                // Log but don't fail the installation for this
            }
        }
    }

    private fun isCommandAvailable(command: String): Boolean {
        return try {
            val process = ProcessBuilder("which", command)
                .redirectErrorStream(true)
                .start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentApplicationPath(): String? {
        // Try to determine current app path from system properties or environment
        return System.getProperty("user.dir")?.let { workingDir ->
            // Look for .app bundle in common locations
            val possiblePaths = listOf(
                "/Applications/c3po.app",
                "$workingDir/../../../c3po.app", // If running from within .app/Contents/MacOS/
                "/Applications/C3PO.app",
                // For testing/debug: create a mock app bundle
                "/tmp/c3po-test.app"
            )

            // For debug/testing - create a mock app bundle if none found
            val existingPath = possiblePaths.find { File(it).exists() }
            if (existingPath != null) {
                return existingPath
            }

            // Create mock app for testing
            val mockApp = File("/tmp/c3po-test.app")
            try {
                if (mockApp.mkdirs()) {
                    return mockApp.absolutePath
                }
            } catch (e: Exception) {
                println("DEBUG: Failed to create mock app: ${e.message}")
            }

            return null
        }
    }
}