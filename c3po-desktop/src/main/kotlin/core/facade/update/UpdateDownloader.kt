package core.facade.update

import core.model.UpdateInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.coroutines.coroutineContext

class UpdateDownloader(
    private val fileManager: UpdateFileManager,
    private val validator: UpdateValidator
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    fun downloadUpdate(updateInfo: UpdateInfo): Flow<DownloadProgress> = flow {
        if (!isValidUrl(updateInfo.downloadUrl)) {
            throw IllegalArgumentException("Invalid download URL: ${updateInfo.downloadUrl}")
        }

        val downloadDir = fileManager.createDownloadDirectory()
        val downloadFile = File(downloadDir, "c3po-${updateInfo.version}.dmg")

        val request = HttpRequest.newBuilder()
            .uri(URI.create(updateInfo.downloadUrl))
            .timeout(Duration.ofMinutes(10))
            .header("User-Agent", "C3PO-UpdateDownloader/1.0")
            .GET()
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())

            when (response.statusCode()) {
                200 -> {
                    val contentLength = response.headers().firstValue("content-length")
                        .map { it.toLongOrNull() ?: 0L }
                        .orElse(0L)

                    downloadFile.outputStream().use { output ->
                        response.body().use { input ->
                            val buffer = ByteArray(8192)
                            var totalBytesRead = 0L
                            var bytesRead: Int

                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                // Check for cancellation before processing data
                                coroutineContext.ensureActive()
                                
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                val progress = if (contentLength > 0) {
                                    ((totalBytesRead.toDouble() / contentLength) * 100).toInt()
                                } else {
                                    0
                                }

                                emit(DownloadProgress(progress, totalBytesRead, contentLength))
                            }
                        }
                    }

                    emit(DownloadProgress(100, downloadFile.length(), contentLength))
                }

                in 300..399 -> {
                    // Handle redirect cases that weren't automatically followed
                    val location = response.headers().firstValue("location").orElse(null)
                    throw IOException("Redirect not handled automatically. Location: $location, Status: ${response.statusCode()}")
                }

                404 -> throw IOException("Download file not found: ${updateInfo.downloadUrl}")
                403 -> throw IOException("Access denied to download file: ${updateInfo.downloadUrl}")
                429 -> throw IOException("Rate limited by download server. Please try again later.")
                in 500..599 -> throw IOException("Download server error (${response.statusCode()}). Please try again later.")
                else -> throw IOException("Unexpected response from download server: ${response.statusCode()}")
            }

        } catch (e: IOException) {
            fileManager.cleanupFile(downloadFile)
            throw e
        } catch (e: InterruptedException) {
            fileManager.cleanupFile(downloadFile)
            throw e
        } catch (e: CancellationException) {
            fileManager.cleanupFile(downloadFile)
            throw e
        } catch (e: Exception) {
            fileManager.cleanupFile(downloadFile)
            throw IOException("Download failed", e)
        }
    }

    fun validateDownloadedFile(file: File, expectedChecksum: String?): ValidationResult {
        val fileValidation = fileManager.validateFile(file)

        return when (fileValidation) {
            is UpdateFileManager.ValidationResult.NotFound ->
                ValidationResult.Failed("Downloaded file not found: ${file.absolutePath}")

            is UpdateFileManager.ValidationResult.Empty ->
                ValidationResult.Failed("Downloaded file is empty")

            is UpdateFileManager.ValidationResult.NotReadable ->
                ValidationResult.Failed("Downloaded file is not readable")

            is UpdateFileManager.ValidationResult.Valid -> {
                try {
                    if (!validator.validateChecksum(file, expectedChecksum)) {
                        ValidationResult.Failed("Downloaded file failed checksum verification. The file may be corrupted.")
                    } else {
                        ValidationResult.Success
                    }
                } catch (e: Exception) {
                    ValidationResult.Failed("Checksum verification failed: ${e.message}")
                }
            }
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = URI(url)
            uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https") && uri.host != null
        } catch (e: Exception) {
            false
        }
    }

    data class DownloadProgress(
        val progress: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    )

    sealed class ValidationResult {
        data object Success : ValidationResult()
        data class Failed(val message: String) : ValidationResult()
    }
}