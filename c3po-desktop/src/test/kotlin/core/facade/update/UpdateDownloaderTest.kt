package core.facade.update

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import core.model.UpdateInfo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UpdateDownloaderTest {

    private val mockFileManager = mockk<UpdateFileManager>()
    private val mockValidator = mockk<UpdateValidator>()
    private val downloader = UpdateDownloader(mockFileManager, mockValidator)

    @Test
    fun `downloadUpdate - invalid URL throws exception`() = runBlocking {
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "invalid-url",
            checksum = null
        )

        every { mockFileManager.createDownloadDirectory() } returns File("/tmp/test")

        assertThrows<IllegalArgumentException> {
            downloader.downloadUpdate(updateInfo).toList()
        }
    }

    @Test
    fun `validateDownloadedFile - valid file returns Success`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.dmg")
        testFile.writeText("mock dmg content")

        every { mockFileManager.validateFile(testFile) } returns UpdateFileManager.ValidationResult.Valid
        every { mockValidator.validateChecksum(testFile, "abc123") } returns true

        val result = downloader.validateDownloadedFile(testFile, "abc123")

        assertThat(result is UpdateDownloader.ValidationResult.Success).isTrue()
    }

    @Test
    fun `validateDownloadedFile - missing file returns Failed`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "missing.dmg")

        every { mockFileManager.validateFile(testFile) } returns UpdateFileManager.ValidationResult.NotFound

        val result = downloader.validateDownloadedFile(testFile, "abc123")

        assertThat(result is UpdateDownloader.ValidationResult.Failed).isTrue()
        val failed = result as UpdateDownloader.ValidationResult.Failed
        assertThat(failed.message.contains("not found")).isTrue()
    }

    @Test
    fun `validateDownloadedFile - empty file returns Failed`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "empty.dmg")
        testFile.createNewFile()

        every { mockFileManager.validateFile(testFile) } returns UpdateFileManager.ValidationResult.Empty

        val result = downloader.validateDownloadedFile(testFile, "abc123")

        assertThat(result is UpdateDownloader.ValidationResult.Failed).isTrue()
        val failed = result as UpdateDownloader.ValidationResult.Failed
        assertThat(failed.message.contains("empty")).isTrue()
    }

    @Test
    fun `validateDownloadedFile - unreadable file returns Failed`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "unreadable.dmg")
        testFile.writeText("content")

        every { mockFileManager.validateFile(testFile) } returns UpdateFileManager.ValidationResult.NotReadable

        val result = downloader.validateDownloadedFile(testFile, "abc123")

        assertThat(result is UpdateDownloader.ValidationResult.Failed).isTrue()
        val failed = result as UpdateDownloader.ValidationResult.Failed
        assertThat(failed.message.contains("not readable")).isTrue()
    }

    @Test
    fun `validateDownloadedFile - checksum mismatch returns Failed`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.dmg")
        testFile.writeText("content")

        every { mockFileManager.validateFile(testFile) } returns UpdateFileManager.ValidationResult.Valid
        every { mockValidator.validateChecksum(testFile, "expected") } returns false

        val result = downloader.validateDownloadedFile(testFile, "expected")

        assertThat(result is UpdateDownloader.ValidationResult.Failed).isTrue()
        val failed = result as UpdateDownloader.ValidationResult.Failed
        assertThat(failed.message.contains("checksum verification")).isTrue()
    }

    @Test
    fun `validateDownloadedFile - checksum validation error returns Failed`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.dmg")
        testFile.writeText("content")

        every { mockFileManager.validateFile(testFile) } returns UpdateFileManager.ValidationResult.Valid
        every { mockValidator.validateChecksum(testFile, "expected") } throws RuntimeException("Checksum error")

        val result = downloader.validateDownloadedFile(testFile, "expected")

        assertThat(result is UpdateDownloader.ValidationResult.Failed).isTrue()
        val failed = result as UpdateDownloader.ValidationResult.Failed
        assertThat(failed.message.contains("Checksum verification failed")).isTrue()
        assertThat(failed.message.contains("Checksum error")).isTrue()
    }

    @Test
    fun `validateDownloadedFile - null checksum validates successfully`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.dmg")
        testFile.writeText("content")

        every { mockFileManager.validateFile(testFile) } returns UpdateFileManager.ValidationResult.Valid
        every { mockValidator.validateChecksum(testFile, null) } returns true

        val result = downloader.validateDownloadedFile(testFile, null)

        assertThat(result is UpdateDownloader.ValidationResult.Success).isTrue()
    }

    @Test
    fun `DownloadProgress - calculates progress correctly`() {
        val progress = UpdateDownloader.DownloadProgress(
            progress = 50,
            bytesDownloaded = 1024L,
            totalBytes = 2048L
        )

        assertThat(progress.progress).isEqualTo(50)
        assertThat(progress.bytesDownloaded).isEqualTo(1024L)
        assertThat(progress.totalBytes).isEqualTo(2048L)
    }

    // Note: Testing actual HTTP download would require mocking HttpClient
    // which is complex. These tests focus on validation logic and error handling.
    // Integration tests should cover the full download flow.

    @Test
    fun `downloadUpdate - valid URL structure`() {
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://github.com/user/repo/releases/download/v2.1.0/app.dmg",
            checksum = "abc123"
        )

        // Test that the URL would be considered valid
        val uri = java.net.URI(updateInfo.downloadUrl)
        assertThat(uri.scheme).isNotNull()
        assertThat(uri.scheme).isEqualTo("https")
        assertThat(uri.host).isNotNull()
    }
}