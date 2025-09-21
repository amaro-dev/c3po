package core.facade.update

import core.util.AppPaths
import java.io.File
import java.io.IOException

class UpdateFileManager {

    fun createDownloadDirectory(): File {
        val downloadDir = AppPaths.getUpdateDownloadDirectory()

        try {
            if (!downloadDir.canWrite()) {
                throw IOException("Download directory is not writable: ${downloadDir.absolutePath}")
            }

            return downloadDir
        } catch (e: SecurityException) {
            throw IOException("Permission denied creating download directory: ${downloadDir.absolutePath}", e)
        }
    }

    fun validateFile(file: File): ValidationResult {
        return when {
            !file.exists() -> ValidationResult.NotFound
            file.length() == 0L -> ValidationResult.Empty
            !file.canRead() -> ValidationResult.NotReadable
            else -> ValidationResult.Valid
        }
    }

    fun checkDiskSpace(file: File, multiplier: Int = 3): DiskSpaceResult {
        val requiredSpace = file.length() * multiplier
        val availableSpace = file.parentFile?.usableSpace ?: 0L

        return if (availableSpace >= requiredSpace) {
            DiskSpaceResult.Sufficient(availableSpace, requiredSpace)
        } else {
            DiskSpaceResult.Insufficient(availableSpace, requiredSpace)
        }
    }

    fun cleanupFile(file: File) {
        try {
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Log but don't throw - this is cleanup
        }
    }

    fun ensureParentDirectoryExists(file: File) {
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists()) {
            val created = parentDir.mkdirs()
            if (!created) {
                throw IOException("Failed to create parent directory: ${parentDir.absolutePath}")
            }
        }
    }

    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data object NotFound : ValidationResult()
        data object Empty : ValidationResult()
        data object NotReadable : ValidationResult()
    }

    sealed class DiskSpaceResult {
        data class Sufficient(val available: Long, val required: Long) : DiskSpaceResult()
        data class Insufficient(val available: Long, val required: Long) : DiskSpaceResult() {
            fun formatMessage(): String {
                val availableMB = available / 1024 / 1024
                val requiredMB = required / 1024 / 1024
                return "Insufficient disk space for installation. Required: ${requiredMB}MB, Available: ${availableMB}MB"
            }
        }
    }
}
