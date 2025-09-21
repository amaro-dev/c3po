package core.facade.update

import core.facade.update.UpdateUtils.getUpdateFileName
import core.util.AppPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Robust auxiliary class that handles all update-related path construction,
 * validation, and disk space checking. Designed to eliminate the common issues
 * with path handling and space detection across different macOS configurations.
 */
class UpdatePathManager {

    data class UpdatePaths(
        val downloadDirectory: File,
        val updateFile: File,
        val targetInstallDirectory: File,
        val backupDirectory: File? = null
    ) {
        fun validate(): PathValidationResult {
            return when {
                !downloadDirectory.exists() && !downloadDirectory.mkdirs() ->
                    PathValidationResult.Failed("Cannot create download directory: ${downloadDirectory.absolutePath}")

                !downloadDirectory.canWrite() ->
                    PathValidationResult.Failed("Download directory is not writable: ${downloadDirectory.absolutePath}")

                !targetInstallDirectory.exists() ->
                    PathValidationResult.Failed("Target installation directory does not exist: ${targetInstallDirectory.absolutePath}")

                !targetInstallDirectory.canWrite() ->
                    PathValidationResult.Failed("Cannot write to installation directory: ${targetInstallDirectory.absolutePath}")

                else -> PathValidationResult.Valid
            }
        }

        fun getDebugInfo(): String {
            return """
            UpdatePaths Debug Info:
            - Download Directory: ${downloadDirectory.absolutePath} (exists: ${downloadDirectory.exists()}, writable: ${downloadDirectory.canWrite()})
            - DMG File: ${updateFile.absolutePath} (exists: ${updateFile.exists()}, size: ${if (updateFile.exists()) "${updateFile.length() / 1024 / 1024}MB" else "N/A"})
            - Target Install: ${targetInstallDirectory.absolutePath} (exists: ${targetInstallDirectory.exists()}, writable: ${targetInstallDirectory.canWrite()})
            - Backup Directory: ${backupDirectory?.absolutePath ?: "None"}
            """.trimIndent()
        }
    }

    sealed class PathValidationResult {
        object Valid : PathValidationResult()
        data class Failed(val reason: String) : PathValidationResult()
    }

    sealed class SpaceCheckResult {
        data class Sufficient(
            val availableSpace: Long,
            val requiredSpace: Long,
            val location: String,
            val method: String
        ) : SpaceCheckResult()

        data class Insufficient(
            val availableSpace: Long,
            val requiredSpace: Long,
            val location: String,
            val suggestions: List<String> = emptyList()
        ) : SpaceCheckResult() {

            fun formatUserMessage(): String {
                val availableMB = availableSpace / 1024 / 1024
                val requiredMB = requiredSpace / 1024 / 1024

                val baseMessage = """
                Insufficient disk space for installation.
                
                Location: $location
                Required: ${requiredMB}MB
                Available: ${availableMB}MB
                Need additional: ${requiredMB - availableMB}MB
                """.trimIndent()

                return if (suggestions.isNotEmpty()) {
                    baseMessage + "\n\nSuggestions:\n" + suggestions.joinToString("\n") { "• $it" }
                } else {
                    baseMessage
                }
            }
        }

        data class ValidationFailed(
            val reason: String,
            val canProceedWithWarning: Boolean = false
        ) : SpaceCheckResult()
    }

    /**
     * Creates and validates all necessary paths for an update installation
     */
    fun createUpdatePaths(version: String, appName: String = "C3PO"): UpdatePaths {
        // Unified download directory resolution
        val downloadDir = AppPaths.getUpdateDownloadDirectory()

        // Construct DMG file path with validation
        val updateFile = File(downloadDir, getUpdateFileName(version))

        // Target installation directory (macOS Applications)
        val targetInstallDir = File("/Applications")

        // Optional backup directory for existing installations
        val backupDir = File(targetInstallDir, ".c3po-backups").takeIf {
            targetInstallDir.canWrite()
        }

        return UpdatePaths(downloadDir, updateFile, targetInstallDir, backupDir)
    }

    /**
     * Comprehensive disk space validation that checks multiple locations and methods
     */
    suspend fun validateDiskSpace(paths: UpdatePaths, estimatedAppSizeBytes: Long? = null): SpaceCheckResult {
        val fileSize = if (paths.updateFile.exists()) paths.updateFile.length() else 0L

        // Calculate required space more intelligently
        val requiredSpace = when {
            estimatedAppSizeBytes != null -> (estimatedAppSizeBytes * 1.2).toLong() // 20% buffer
            fileSize > 0 -> (fileSize * 0.8).toLong()
            else -> 300L * 1024 * 1024 // Default 300MB fallback
        }

        // Check the actual installation destination first (most important)
        val installSpaceResult =
            checkSpaceAtLocation(paths.targetInstallDirectory, requiredSpace, "Installation directory")
        if (installSpaceResult is SpaceCheckResult.Sufficient) {
            return installSpaceResult
        }

        // If install location check failed, try temp directory (for temporary operations)
        val tempRequiredSpace = if (fileSize > 0) fileSize + (50 * 1024 * 1024) else requiredSpace
        val tempSpaceResult = checkSpaceAtLocation(paths.downloadDirectory, tempRequiredSpace, "Download directory")

        // Return most specific failure information
        return when {
            installSpaceResult is SpaceCheckResult.Insufficient -> installSpaceResult.copy(
                suggestions = listOf(
                    "Free up space in /Applications directory",
                    "Move some applications to external storage",
                    "Empty Trash to reclaim disk space",
                    "Use disk cleanup utilities to free space"
                )
            )

            tempSpaceResult is SpaceCheckResult.Insufficient -> tempSpaceResult.copy(
                suggestions = listOf(
                    "Clear temporary files and caches",
                    "Empty Downloads folder",
                    "Restart your Mac to clear system caches"
                )
            )

            else -> SpaceCheckResult.ValidationFailed(
                reason = "Unable to validate disk space on this system",
                canProceedWithWarning = true
            )
        }
    }

    /**
     * Checks available space at a specific location using multiple methods for reliability
     */
    private suspend fun checkSpaceAtLocation(
        location: File,
        requiredSpace: Long,
        locationName: String
    ): SpaceCheckResult {
        // Method 1: Java File API (fast, but can be unreliable on some systems)
        val javaSpaceResult = tryJavaSpaceCheck(location, requiredSpace, locationName)
        if (javaSpaceResult is SpaceCheckResult.Sufficient) {
            return javaSpaceResult
        }

        // Method 2: Native df command (more reliable, especially on macOS)
        val nativeSpaceResult = tryNativeSpaceCheck(location, requiredSpace, locationName)
        if (nativeSpaceResult is SpaceCheckResult.Sufficient) {
            return nativeSpaceResult
        }

        // Method 3: Fallback to parent directories if direct check failed
        val parentSpaceResult = tryParentDirectoryCheck(location, requiredSpace, locationName)
        if (parentSpaceResult is SpaceCheckResult.Sufficient) {
            return parentSpaceResult
        }

        // Return the most informative failure result
        return when {
            javaSpaceResult is SpaceCheckResult.Insufficient -> javaSpaceResult
            nativeSpaceResult is SpaceCheckResult.Insufficient -> nativeSpaceResult
            parentSpaceResult is SpaceCheckResult.Insufficient -> parentSpaceResult
            else -> SpaceCheckResult.ValidationFailed("All space check methods failed for $locationName")
        }
    }

    private fun tryJavaSpaceCheck(location: File, requiredSpace: Long, locationName: String): SpaceCheckResult {
        return try {
            val availableSpace = location.usableSpace

            // Sanity check the result
            when {
                availableSpace < 0 -> SpaceCheckResult.ValidationFailed("Java API returned negative space: $availableSpace")
                availableSpace == 0L && location.exists() -> SpaceCheckResult.ValidationFailed("Java API returned zero space (likely API bug)")
                availableSpace > 1000L * 1024 * 1024 * 1024 * 1024 -> SpaceCheckResult.ValidationFailed("Java API returned suspiciously large space: $availableSpace")
                availableSpace >= requiredSpace -> SpaceCheckResult.Sufficient(
                    availableSpace,
                    requiredSpace,
                    locationName,
                    "Java File API"
                )

                else -> SpaceCheckResult.Insufficient(availableSpace, requiredSpace, locationName)
            }
        } catch (e: Exception) {
            SpaceCheckResult.ValidationFailed("Java API space check failed: ${e.message}")
        }
    }

    private suspend fun tryNativeSpaceCheck(
        location: File,
        requiredSpace: Long,
        locationName: String
    ): SpaceCheckResult {
        return withContext(Dispatchers.IO) {
            try {
                val process = ProcessBuilder("df", "-k", location.absolutePath)
                    .redirectErrorStream(true)
                    .start()

                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    val availableSpace = parseDfOutput(output)
                    if (availableSpace > 0) {
                        return@withContext if (availableSpace >= requiredSpace) {
                            SpaceCheckResult.Sufficient(
                                availableSpace,
                                requiredSpace,
                                locationName,
                                "Native df command"
                            )
                        } else {
                            SpaceCheckResult.Insufficient(availableSpace, requiredSpace, locationName)
                        }
                    }
                }

                SpaceCheckResult.ValidationFailed("df command failed: exit code $exitCode, output: $output")
            } catch (e: Exception) {
                SpaceCheckResult.ValidationFailed("Native df command failed: ${e.message}")
            }
        }
    }

    private fun tryParentDirectoryCheck(location: File, requiredSpace: Long, locationName: String): SpaceCheckResult {
        val parents = generateSequence(location.parentFile) { it.parentFile }
            .take(5) // Don't go too far up the tree
            .filter { it.exists() }

        for (parent in parents) {
            val result = tryJavaSpaceCheck(parent, requiredSpace, "$locationName (via ${parent.absolutePath})")
            if (result is SpaceCheckResult.Sufficient || result is SpaceCheckResult.Insufficient) {
                return result
            }
        }

        return SpaceCheckResult.ValidationFailed("All parent directory checks failed")
    }

    private fun parseDfOutput(output: String): Long {
        // Parse df output format: 
        // Filesystem     1K-blocks    Used Available Use% Mounted on
        // /dev/disk3s5   482242560  314572800 135045632  71% /System/Volumes/Data

        val lines = output.trim().split('\n')
        if (lines.size < 2) return -1

        // Skip header, take first data line
        val dataLine = lines.drop(1).firstOrNull() ?: return -1
        val parts = dataLine.trim().split(Regex("\\s+"))

        return if (parts.size >= 4) {
            try {
                // Available space is typically the 4th column (index 3), in KB
                parts[3].toLong() * 1024 // Convert KB to bytes
            } catch (e: NumberFormatException) {
                -1
            }
        } else {
            -1
        }
    }

    /**
     * Estimates uncompressed app size from ZIP file
     */
    fun estimateAppSizeFromZip(zipFile: File): Long {
        return if (zipFile.exists()) {
            // ZIP files typically have better compression than DMGs for app bundles
            // Compression ratio is usually 50-70% for .app bundles in ZIP format
            // So uncompressed size is roughly 140-200% of ZIP size. We'll use 150% as conservative estimate.
            (zipFile.length() * 1.5).toLong()
        } else {
            // Default fallback size
            300L * 1024 * 1024 // 300MB
        }
    }

    /**
     * Comprehensive validation before installation
     */
    suspend fun validateInstallationReadiness(
        version: String,
        appName: String = "C3PO",
        estimatedAppSize: Long? = null
    ): InstallationReadiness {

        val paths = createUpdatePaths(version, appName)
        val pathValidation = paths.validate()

        if (pathValidation is PathValidationResult.Failed) {
            return InstallationReadiness.Failed(
                reason = "Path validation failed: ${pathValidation.reason}",
                debugInfo = paths.getDebugInfo()
            )
        }

        return when (val spaceCheck = validateDiskSpace(paths, estimatedAppSize)) {
            is SpaceCheckResult.Sufficient -> InstallationReadiness.Ready(paths, spaceCheck)
            is SpaceCheckResult.Insufficient -> InstallationReadiness.InsufficientSpace(paths, spaceCheck)
            is SpaceCheckResult.ValidationFailed -> {
                if (spaceCheck.canProceedWithWarning) {
                    InstallationReadiness.ReadyWithWarning(paths, spaceCheck.reason)
                } else {
                    InstallationReadiness.Failed(
                        reason = "Space validation failed: ${spaceCheck.reason}",
                        debugInfo = paths.getDebugInfo()
                    )
                }
            }
        }
    }

    sealed class InstallationReadiness {
        data class Ready(val paths: UpdatePaths, val spaceInfo: SpaceCheckResult.Sufficient) : InstallationReadiness()
        data class ReadyWithWarning(val paths: UpdatePaths, val warning: String) : InstallationReadiness()
        data class InsufficientSpace(val paths: UpdatePaths, val spaceInfo: SpaceCheckResult.Insufficient) :
            InstallationReadiness()

        data class Failed(val reason: String, val debugInfo: String) : InstallationReadiness()
    }
}
