package core.facade.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class UpdateInstaller(
    private val fileManager: UpdateFileManager,
    private val platformDetector: PlatformDetector = DefaultPlatformDetector(),
    private val pathManager: UpdatePathManager = UpdatePathManager()
) {

    suspend fun installUpdate(
        installFile: File,
        onProgress: suspend (String) -> Unit = {}
    ): InstallResult {
        onProgress("Validating installation file...")
        // Add delay to simulate real validation time
        kotlinx.coroutines.delay(500)
        val validation = fileManager.validateFile(installFile)

        when (validation) {
            is UpdateFileManager.ValidationResult.NotFound ->
                return InstallResult.failure("Installation file not found: ${installFile.absolutePath}")

            is UpdateFileManager.ValidationResult.Empty ->
                return InstallResult.failure("Installation file is empty or corrupted")

            is UpdateFileManager.ValidationResult.NotReadable ->
                return InstallResult.failure("Installation file is not readable")

            is UpdateFileManager.ValidationResult.Valid -> {
                // Continue with installation
            }
        }

        // Use enhanced disk space validation with UpdatePathManager
        onProgress("Checking disk space and permissions...")
        // Add delay to simulate real disk space checking
        kotlinx.coroutines.delay(800)
        val version = extractVersionFromFilename(installFile.name)
        val readiness =
            pathManager.validateInstallationReadiness(version, "C3PO", pathManager.estimateAppSizeFromDmg(installFile))

        when (readiness) {
            is UpdatePathManager.InstallationReadiness.Failed ->
                return InstallResult.failure(readiness.reason)

            is UpdatePathManager.InstallationReadiness.InsufficientSpace ->
                return InstallResult.failure(readiness.spaceInfo.formatUserMessage())

            else -> Unit
        }

        onProgress("Starting installation process...")
        // Add delay to simulate preparation time
        kotlinx.coroutines.delay(500)
        return when (platformDetector.getCurrentPlatform()) {
            Platform.MACOS -> installDmgOnMacOS(installFile, onProgress)
            Platform.WINDOWS -> InstallResult.failure("Windows installation not yet implemented")
            Platform.LINUX -> InstallResult.failure("Linux installation not yet implemented")
            Platform.UNKNOWN -> InstallResult.failure("Unsupported platform for auto-installation")
        }
    }

    private fun extractVersionFromFilename(filename: String): String {
        // Extract version from filename like "c3po-2.1.0.dmg"
        val versionPattern = Regex("""c3po-(.+)\.dmg""", RegexOption.IGNORE_CASE)
        return versionPattern.find(filename)?.groupValues?.get(1) ?: "unknown"
    }

    private suspend fun installDmgOnMacOS(
        dmgFile: File,
        onProgress: suspend (String) -> Unit
    ): InstallResult {
        var backupFile: File? = null
        return try {
            onProgress("Mounting DMG file...")
            kotlinx.coroutines.delay(1000) // Simulate mount time
            val mountPoint = mountDmg(dmgFile)

            try {
                onProgress("Finding application bundle...")
                kotlinx.coroutines.delay(500) // Simulate search time
                val appBundle = findAppBundle(mountPoint)
                val targetLocation = getApplicationsDirectory()

                onProgress("Copying application to Applications folder...")
                kotlinx.coroutines.delay(2000) // Simulate copy time
                backupFile = copyAppBundle(appBundle, targetLocation)

                onProgress("Installation completed successfully")
                
                // Clean up backup file after successful installation
                if (backupFile != null && backupFile.exists()) {
                    onProgress("Cleaning up backup files...")
                    try {
                        if (backupFile.deleteRecursively()) {
                            kotlinx.coroutines.delay(200) // Brief delay to show cleanup message
                        }
                    } catch (e: Exception) {
                        // Log but don't fail the installation for backup cleanup issues
                        println("[UpdateInstaller] Warning: Failed to clean up backup file: ${backupFile.absolutePath}")
                    }
                }
                
                kotlinx.coroutines.delay(500) // Allow user to see completion message
                InstallResult.success(requiresRestart = true)
            } finally {
                onProgress("Unmounting DMG file...")
                kotlinx.coroutines.delay(300) // Simulate unmount time
                unmountDmg(mountPoint)
            }
        } catch (e: SecurityException) {
            InstallResult.failure("Installation permission denied. Please run as administrator or check file permissions.")
        } catch (e: IOException) {
            InstallResult.failure("Installation I/O error: ${e.message}. Please check disk space and file permissions.")
        } catch (e: InterruptedException) {
            InstallResult.failure("Installation was cancelled")
        } catch (e: Exception) {
            InstallResult.failure("Installation error: ${e.message}")
        }
    }

    private suspend fun mountDmg(dmgFile: File): File = withContext(Dispatchers.IO) {
        // Remove -quiet flag to get the mount point information we need
        val mountCommand = arrayOf("hdiutil", "attach", dmgFile.absolutePath, "-nobrowse")
        val process = ProcessBuilder(*mountCommand)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        val output = process.inputStream.bufferedReader().readText()

        if (exitCode != 0) {
            // Always log mount failures to help with debugging
            println("[UpdateInstaller] hdiutil mount failed with exit code $exitCode")
            println("[UpdateInstaller] hdiutil output: $output")
            throw IOException("Failed to mount DMG: $output")
        }

        // Always log the raw hdiutil output for debugging
        println("[UpdateInstaller] hdiutil output: $output")

        // Get all potential mount points from this DMG mount operation
        val mountPoints = output.lines()
            .filter { line -> line.contains("/Volumes/") }
            .mapNotNull { line ->
                println("[UpdateInstaller] Parsing line: $line")
                // hdiutil output format: /dev/disk2s1 \t Apple_HFS \t /Volumes/AppName
                val parts = line.split(Regex("\\s+"))
                if (parts.size >= 3) {
                    // Find the index where /Volumes/ starts and join the rest
                    val volumesIndex = parts.indexOfFirst { it.startsWith("/Volumes/") }
                    if (volumesIndex >= 0) {
                        val path = parts.drop(volumesIndex).joinToString(" ")
                        println("[UpdateInstaller] Detected path: $path")
                        if (path.startsWith("/Volumes/")) File(path) else null
                    } else null
                } else null
            }

        println("[UpdateInstaller] All detected mount points: ${mountPoints.map { it.absolutePath }}")

        // Take the first valid mount point that exists
        val mountPoint = mountPoints.firstOrNull { it.exists() }
            ?: throw IOException("Could not find a valid mount point from hdiutil output. Detected paths: ${mountPoints.map { it.absolutePath }}, Output: $output")

        println("[UpdateInstaller] Selected mount point: ${mountPoint.absolutePath}")

        mountPoint
    }

    private fun findAppBundle(mountPoint: File): File {
        val appFiles = mountPoint.listFiles { file ->
            file.isDirectory && file.name.endsWith(".app")
        }

        if (appFiles.isNullOrEmpty()) {
            throw IOException("No .app bundle found in mounted DMG: ${mountPoint.absolutePath}")
        }

        if (appFiles.size > 1) {
            val c3poApp = appFiles.find { it.name.contains("c3po", ignoreCase = true) }
            if (c3poApp != null) {
                return c3poApp
            }
        }

        return appFiles[0]
    }

    private fun getApplicationsDirectory(): File {
        return File("/Applications")
    }

    private suspend fun copyAppBundle(sourceApp: File, targetDir: File): File? = withContext(Dispatchers.IO) {
        val targetApp = File(targetDir, sourceApp.name)
        var backupApp: File? = null

        // Handle existing app by creating backup
        if (targetApp.exists()) {
            val backupName = "${sourceApp.nameWithoutExtension}-backup-${System.currentTimeMillis()}.app"
            backupApp = File(targetDir, backupName)

            // Use File.renameTo instead of mv command
            if (!targetApp.renameTo(backupApp)) {
                throw IOException("Failed to backup existing application from ${targetApp.absolutePath} to ${backupApp.absolutePath}")
            }
        }

        // Use Kotlin's built-in copyRecursively instead of cp command
        try {
            if (!sourceApp.copyRecursively(targetApp, overwrite = true)) {
                throw IOException("Failed to copy app bundle - copyRecursively returned false")
            }
        } catch (e: Exception) {
            // If copy failed and we made a backup, try to restore it
            if (backupApp != null && backupApp.exists()) {
                backupApp.renameTo(targetApp) // Attempt to restore backup
            }
            throw IOException("Failed to copy app bundle from ${sourceApp.absolutePath} to ${targetApp.absolutePath}: ${e.message}", e)
        }

        // Verify the copy was successful
        if (!targetApp.exists()) {
            // If copy failed and we made a backup, try to restore it
            if (backupApp != null && backupApp.exists()) {
                backupApp.renameTo(targetApp) // Attempt to restore backup
            }
            throw IOException("App bundle was not copied successfully to: ${targetApp.absolutePath}")
        }
        
        // Verify it's a valid app bundle (contains at least the expected structure)
        if (!targetApp.isDirectory) {
            // If verification failed and we made a backup, try to restore it
            if (backupApp != null && backupApp.exists()) {
                targetApp.deleteRecursively() // Remove the invalid copy
                backupApp.renameTo(targetApp) // Attempt to restore backup
            }
            throw IOException("Copied app bundle is not a directory: ${targetApp.absolutePath}")
        }

        // Return the backup file so it can be cleaned up later
        return@withContext backupApp
    }

    private suspend fun unmountDmg(mountPoint: File): Unit = withContext(Dispatchers.IO) {
        try {
            val unmountCommand = arrayOf("hdiutil", "detach", mountPoint.absolutePath, "-quiet")
            val process = ProcessBuilder(*unmountCommand)
                .redirectErrorStream(true)
                .start()

            process.waitFor()
        } catch (e: Exception) {
            // Log but don't throw - this is cleanup
        }
    }

    enum class Platform {
        MACOS, WINDOWS, LINUX, UNKNOWN
    }

    data class InstallResult(
        val success: Boolean,
        val message: String? = null,
        val requiresRestart: Boolean = false
    ) {
        companion object {
            fun success(requiresRestart: Boolean = false) = InstallResult(
                success = true,
                requiresRestart = requiresRestart
            )

            fun failure(message: String) = InstallResult(
                success = false,
                message = message
            )
        }
    }
}

interface PlatformDetector {
    fun getCurrentPlatform(): UpdateInstaller.Platform
}

class DefaultPlatformDetector : PlatformDetector {
    override fun getCurrentPlatform(): UpdateInstaller.Platform {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("mac") || osName.contains("darwin") -> UpdateInstaller.Platform.MACOS
            osName.contains("win") -> UpdateInstaller.Platform.WINDOWS
            osName.contains("linux") -> UpdateInstaller.Platform.LINUX
            else -> UpdateInstaller.Platform.UNKNOWN
        }
    }
}