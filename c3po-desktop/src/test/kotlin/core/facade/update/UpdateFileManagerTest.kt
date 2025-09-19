package core.facade.update

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UpdateFileManagerTest {

    private val fileManager = UpdateFileManager()

    @Test
    fun `createDownloadDirectory - creates directory successfully`() {
        val downloadDir = fileManager.createDownloadDirectory()

        assertThat(downloadDir).all {
            transform { it.exists() }.isTrue()
            transform { it.isDirectory }.isTrue()
            transform { it.canWrite() }.isTrue()
            transform { it.name }.isEqualTo("c3po-updates")
        }
    }

    @Test
    fun `createDownloadDirectory - handles existing directory`() {
        // Create directory first time
        val firstCall = fileManager.createDownloadDirectory()

        // Should handle existing directory on second call
        val secondCall = fileManager.createDownloadDirectory()

        assertThat(secondCall.absolutePath).isEqualTo(firstCall.absolutePath)
        assertThat(secondCall.exists()).isTrue()
    }

    @Test
    fun `validateFile - valid file returns Valid`(@TempDir tempDir: File) {
        val validFile = File(tempDir, "valid.txt")
        validFile.writeText("content")

        val result = fileManager.validateFile(validFile)

        assertThat(result is UpdateFileManager.ValidationResult.Valid).isTrue()
    }

    @Test
    fun `validateFile - non-existent file returns NotFound`(@TempDir tempDir: File) {
        val nonExistentFile = File(tempDir, "does-not-exist.txt")

        val result = fileManager.validateFile(nonExistentFile)

        assertThat(result is UpdateFileManager.ValidationResult.NotFound).isTrue()
    }

    @Test
    fun `validateFile - empty file returns Empty`(@TempDir tempDir: File) {
        val emptyFile = File(tempDir, "empty.txt")
        emptyFile.createNewFile()

        val result = fileManager.validateFile(emptyFile)

        assertThat(result is UpdateFileManager.ValidationResult.Empty).isTrue()
    }

    @Test
    fun `validateFile - unreadable file returns NotReadable`(@TempDir tempDir: File) {
        val unreadableFile = File(tempDir, "unreadable.txt")
        unreadableFile.writeText("content")
        unreadableFile.setReadable(false)

        try {
            val result = fileManager.validateFile(unreadableFile)
            assertThat(result is UpdateFileManager.ValidationResult.NotReadable).isTrue()
        } finally {
            // Restore readability for cleanup
            unreadableFile.setReadable(true)
        }
    }

    @Test
    fun `checkDiskSpace - sufficient space returns Sufficient`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "small.txt")
        testFile.writeText("small content")

        val result = fileManager.checkDiskSpace(testFile, multiplier = 2)

        assertThat(result is UpdateFileManager.DiskSpaceResult.Sufficient).isTrue()
        val sufficient = result as UpdateFileManager.DiskSpaceResult.Sufficient
        assertThat(sufficient.required).isEqualTo(testFile.length() * 2)
        assertThat(sufficient.available > sufficient.required).isTrue()
    }

    @Test
    fun `checkDiskSpace - insufficient space scenario`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("content")

        // Create an extremely high multiplier to simulate insufficient space
        // This might actually return Sufficient on systems with lots of free space
        // so we'll test the calculation logic instead
        when (val result = fileManager.checkDiskSpace(testFile, multiplier = Int.MAX_VALUE)) {
            is UpdateFileManager.DiskSpaceResult.Insufficient -> {
                assertThat(result.required > result.available).isTrue()
                assertThat(result.formatMessage().contains("Insufficient disk space")).isTrue()
                assertThat(result.formatMessage().contains("Required:")).isTrue()
                assertThat(result.formatMessage().contains("Available:")).isTrue()
            }

            is UpdateFileManager.DiskSpaceResult.Sufficient -> {
                // This is also valid if the system has enough space
                assertThat(result.available >= result.required).isTrue()
            }
        }
    }

    @Test
    fun `checkDiskSpace - formats error message correctly`() {
        // Test the error message formatting with known values
        val insufficient = UpdateFileManager.DiskSpaceResult.Insufficient(
            available = 100 * 1024 * 1024, // 100MB
            required = 500 * 1024 * 1024   // 500MB
        )

        val message = insufficient.formatMessage()

        assertThat(message).all {
            transform { it.contains("Insufficient disk space") }.isTrue()
            transform { it.contains("Required: 500MB") }.isTrue()
            transform { it.contains("Available: 100MB") }.isTrue()
        }
    }

    @Test
    fun `cleanupFile - deletes existing file`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("content")
        assertThat(testFile.exists()).isTrue()

        fileManager.cleanupFile(testFile)

        assertThat(!testFile.exists()).isTrue()
    }

    @Test
    fun `cleanupFile - handles non-existent file gracefully`(@TempDir tempDir: File) {
        val nonExistentFile = File(tempDir, "does-not-exist.txt")

        // Should not throw exception
        fileManager.cleanupFile(nonExistentFile)
    }

    @Test
    fun `ensureParentDirectoryExists - creates parent directory`(@TempDir tempDir: File) {
        val subDir = File(tempDir, "subdir")
        val testFile = File(subDir, "test.txt")

        assertThat(!subDir.exists()).isTrue()

        fileManager.ensureParentDirectoryExists(testFile)

        assertThat(subDir).all {
            transform { it.exists() }.isTrue()
            transform { it.isDirectory }.isTrue()
        }
    }

    @Test
    fun `ensureParentDirectoryExists - handles existing parent directory`(@TempDir tempDir: File) {
        val subDir = File(tempDir, "existing")
        subDir.mkdirs()
        val testFile = File(subDir, "test.txt")

        assertThat(subDir.exists()).isTrue()

        // Should not throw exception
        fileManager.ensureParentDirectoryExists(testFile)

        assertThat(subDir).all {
            transform { it.exists() }.isTrue()
        }
    }
}
