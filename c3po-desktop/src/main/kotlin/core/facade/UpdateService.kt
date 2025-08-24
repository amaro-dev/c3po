package core.facade

import core.model.UpdateInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

class UpdateService(private val httpClient: HttpClient = HttpClient.newHttpClient()) {

    suspend fun checkForUpdates(
        currentVersion: String,
        updateUrl: String = "https://api.github.com/repos/amaro-dev/c3po/releases/latest"
    ): UpdateInfo? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(updateUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                parseUpdateResponse(response.body(), currentVersion)
            } else {
                null
            }
        } catch (e: Exception) {
            null
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

        return UpdateInfo(
            version = version,
            downloadUrl = downloadUrl,
            checksum = null,
            releaseNotesUrl = null
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

        // Ensure download directory exists
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        val fileName = "c3po-${updateInfo.version}.dmg"
        val targetFile = File(downloadDir, fileName)

        val request = HttpRequest.newBuilder()
            .uri(URI.create(updateInfo.downloadUrl))
            .header("Accept", "application/octet-stream")
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
            
            if (response.statusCode() != 200) {
                throw RuntimeException("Download failed with status: ${response.statusCode()}")
            }

            val contentLength = response.headers().firstValue("Content-Length")
                .map { it.toLong() }.orElse(-1L)

            response.body().use { inputStream ->
                Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }

            // Emit completion
            emit(DownloadProgress(100, contentLength, contentLength))

        } catch (e: Exception) {
            // Clean up partial download
            if (targetFile.exists()) {
                targetFile.delete()
            }
            throw e
        }
    }

    fun verifyChecksum(file: File, expectedChecksum: String?): Boolean {
        if (expectedChecksum == null) return true
        
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(file.readBytes())
            val actualChecksum = hashBytes.joinToString("") { "%02x".format(it) }
            actualChecksum.equals(expectedChecksum, ignoreCase = true)
        } catch (e: Exception) {
            false
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
}