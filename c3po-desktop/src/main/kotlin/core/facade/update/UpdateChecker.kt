package core.facade.update

import core.model.UpdateInfo
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class UpdateChecker(
    private val validator: UpdateValidator
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val minisignVerifier = MinisignVerifier()

    // Embedded public key for signature verification
    private val MINISIGN_PUBLIC_KEY = "RWTi5nN2R+gQ3Qdn1La4MCFLSmgglSU3gEuJuIwmNdUslLisUeInhJ1y"

    suspend fun checkForUpdates(currentVersion: String, updateUrl: String): UpdateInfo? {
        if (!isValidUrl(updateUrl)) {
            throw IllegalArgumentException("Invalid update URL: $updateUrl")
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(updateUrl))
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "application/json")
            .header("User-Agent", "C3PO-UpdateChecker/1.0")
            .GET()
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                200 -> parseUpdateResponse(response.body(), currentVersion)
                404 -> throw IOException("Update endpoint not found: $updateUrl")
                403 -> throw IOException("Access denied to update endpoint: $updateUrl")
                429 -> throw IOException("Rate limited by update server. Please try again later.")
                in 500..599 -> throw IOException("Update server error (${response.statusCode()}). Please try again later.")
                else -> throw IOException("Unexpected response from update server: ${response.statusCode()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: InterruptedException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Network error while checking for updates", e)
        }
    }

    /**
     * Downloads and verifies a ZIP update file with its signature.
     */
    suspend fun downloadAndVerifyUpdate(updateInfo: UpdateInfo): File {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "c3po-updates")
        tempDir.mkdirs()

        val zipFile = File(tempDir, "c3po-${updateInfo.version}-macos.zip")
        val sigFile = File(tempDir, "c3po-${updateInfo.version}-macos.zip.minisig")

        try {
            // Download ZIP file
            downloadFile(updateInfo.downloadUrl, zipFile)

            // Download signature file
            val sigUrl = updateInfo.downloadUrl.replace(".zip", ".zip.minisig")
            downloadFile(sigUrl, sigFile)

            // Verify signature
            if (!minisignVerifier.verifyFile(zipFile, sigFile, MINISIGN_PUBLIC_KEY)) {
                throw SecurityException("Signature verification failed for update file")
            }

            // Verify SHA-256 checksum if available
            val checksumValue = updateInfo.checksum
            if (!checksumValue.isNullOrBlank()) {
                if (!verifyFileChecksum(zipFile, checksumValue)) {
                    throw SecurityException("Checksum verification failed for update file")
                }
            }

            return zipFile
        } catch (e: Exception) {
            // Cleanup on failure
            zipFile.delete()
            sigFile.delete()
            throw e
        }
    }

    private suspend fun downloadFile(url: String, targetFile: File) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(10)) // Longer timeout for file downloads
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(targetFile.toPath()))

        if (response.statusCode() != 200) {
            throw IOException("Failed to download file from $url: HTTP ${response.statusCode()}")
        }
    }

    private fun verifyFileChecksum(file: File, expectedChecksum: String): Boolean {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(file.readBytes())
        val actualChecksum = hashBytes.joinToString("") { "%02x".format(it) }
        return actualChecksum.equals(expectedChecksum, ignoreCase = true)
    }

    fun validateVersion(newVersion: String, currentVersion: String): Boolean {
        return try {
            validator.compareVersions(newVersion, currentVersion)
        } catch (e: Exception) {
            throw IllegalArgumentException("Version validation failed for $newVersion vs $currentVersion", e)
        }
    }

    fun isValidUrl(url: String): Boolean {
        return try {
            val uri = URI(url)
            uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https") && uri.host != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun <T> withRetry(
        maxRetries: Int,
        delayMs: Long,
        operation: suspend () -> T
    ): T {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(delayMs)
                }
            }
        }
        throw lastException ?: RuntimeException("All retry attempts failed")
    }

    private fun parseUpdateResponse(responseBody: String, currentVersion: String): UpdateInfo? {
        return try {
            val jsonElement = json.parseToJsonElement(responseBody)
            val release = jsonElement.jsonObject

            val tagName = release["tag_name"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing tag_name in release response")

            release["name"]?.jsonPrimitive?.content ?: tagName
            val releaseNotes = release["body"]?.jsonPrimitive?.content
            release["published_at"]?.jsonPrimitive?.content
            val htmlUrl = release["html_url"]?.jsonPrimitive?.content

            val assets = release["assets"]?.jsonArray
            if (assets == null || assets.isEmpty()) {
                throw IllegalArgumentException("No assets found in release")
            }

            // Look for ZIP file instead of DMG for auto-updates
            val zipAsset = assets.find { asset ->
                val assetObj = asset.jsonObject
                val name = assetObj["name"]?.jsonPrimitive?.content ?: ""
                name.endsWith("-macos.zip", ignoreCase = true)
            }?.jsonObject ?: throw IllegalArgumentException("No macOS ZIP asset found in release")

            val downloadUrl = zipAsset["browser_download_url"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing download URL in ZIP asset")

            zipAsset["size"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

            // Try to get checksum from release metadata
            val checksum = release["zip_checksum"]?.jsonPrimitive?.content
                ?: extractChecksumFromReleaseNotes(releaseNotes)

            if (!validator.compareVersions(tagName, currentVersion)) {
                return null
            }

            UpdateInfo(
                version = tagName,
                downloadUrl = downloadUrl,
                checksum = checksum,
                releaseNotesUrl = htmlUrl
            )

        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse update response", e)
        }
    }

    private fun extractChecksumFromReleaseNotes(releaseNotes: String?): String? {
        if (releaseNotes.isNullOrBlank()) return null

        val checksumPatterns = listOf(
            Regex("""(?i)checksum[:\s]*([a-fA-F0-9]{64})"""),
            Regex("""(?i)sha-?256[:\s]*([a-fA-F0-9]{64})"""),
            Regex("""(?i)hash[:\s]*([a-fA-F0-9]{64})""")
        )

        for (pattern in checksumPatterns) {
            val match = pattern.find(releaseNotes)
            if (match != null) {
                return match.groupValues[1]
            }
        }

        return null
    }
}