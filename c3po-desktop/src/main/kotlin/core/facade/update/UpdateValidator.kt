package core.facade.update

import java.io.File
import java.security.MessageDigest

class UpdateValidator {

    fun compareVersions(newVersion: String, currentVersion: String): Boolean {
        return try {
            val newParts = parseVersion(newVersion)
            val currentParts = parseVersion(currentVersion)

            for (i in 0 until maxOf(newParts.size, currentParts.size)) {
                val newPart = newParts.getOrElse(i) { 0 }
                val currentPart = currentParts.getOrElse(i) { 0 }

                when {
                    newPart > currentPart -> return true
                    newPart < currentPart -> return false
                }
            }
            false
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid version format: $newVersion or $currentVersion", e)
        }
    }

    private fun parseVersion(version: String): List<Int> {
        val cleanVersion = version.removePrefix("v").trim()
        if (!cleanVersion.matches(Regex("""^\d+(\.\d+)*(-[a-zA-Z0-9]+)?$"""))) {
            throw IllegalArgumentException("Invalid version format: $version")
        }

        return cleanVersion.split("-")[0].split(".").map { it.toInt() }
    }

    fun calculateChecksum(file: File): String {
        if (!file.exists()) {
            throw IllegalArgumentException("File does not exist: ${file.absolutePath}")
        }

        if (file.length() == 0L) {
            throw IllegalArgumentException("File is empty: ${file.absolutePath}")
        }

        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            throw RuntimeException("Failed to calculate checksum for ${file.name}", e)
        }
    }

    fun validateChecksum(file: File, expectedChecksum: String?): Boolean {
        if (expectedChecksum.isNullOrBlank()) {
            return true
        }

        val actualChecksum = calculateChecksum(file)
        val normalizedExpected = expectedChecksum.lowercase().trim()
        val normalizedActual = actualChecksum.lowercase().trim()

        return normalizedExpected == normalizedActual
    }
}