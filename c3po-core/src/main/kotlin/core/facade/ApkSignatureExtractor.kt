package core.facade

import core.command.CommandExecutor
import core.command.ExtractSpecificApkCommand
import core.command.GetPackageApkPathCommand
import core.model.AndroidPackageReport
import core.model.AppPackage
import java.io.File
import java.nio.file.Files

/**
 * Facade that combines APK extraction from device with local signature analysis
 */
class ApkSignatureExtractor(
    private val commandExecutor: CommandExecutor,
    private val signatureExtractor: SignatureExtractor
) {
    companion object {
        private const val TEMP_DIR_PREFIX = "c3po_apk_"
    }

    private val tempDir: File by lazy {
        val tempPath = Files.createTempDirectory(TEMP_DIR_PREFIX)
        tempPath.toFile().apply {
            // Ensure temp directory is deleted on JVM shutdown
            deleteOnExit()
        }
    }

    /**
     * Extract APK from device and analyze its signature
     */
    suspend fun extractAndAnalyzeSignature(
        packageInfo: AppPackage,
        adbPath: String,
        device: core.model.AdbDevice?
    ): Result<AndroidPackageReport> {
        return try {
            // Step 1: Get actual APK path using pm path
            val getPathResult = commandExecutor.go(
                GetPackageApkPathCommand(packageInfo.packageName),
                adbPath,
                device
            )

            getPathResult.fold(
                onSuccess = { apkPaths ->
                    // Use the first APK path (main APK, not splits)
                    val primaryApkPath = apkPaths.first()

                    // Step 2: Extract APK to temp folder
                    val localPath = createLocalApkPath(packageInfo)
                    val extractResult = commandExecutor.go(
                        ExtractSpecificApkCommand(primaryApkPath, localPath),
                        adbPath,
                        device
                    )

                    extractResult.fold(
                        onSuccess = { extractedPath ->
                            // Step 3: Analyze signature
                            val analysisResult = signatureExtractor.getCertificateFingerprint(extractedPath)

                            // Step 4: Clean up
                            cleanupApkFile(extractedPath)

                            analysisResult
                        },
                        onFailure = { exception ->
                            Result.failure(RuntimeException("APK extraction failed: ${exception.message}", exception))
                        }
                    )
                },
                onFailure = { exception ->
                    Result.failure(RuntimeException("Failed to resolve APK path: ${exception.message}", exception))
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createLocalApkPath(packageInfo: AppPackage): String {
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        val filename = "${packageInfo.packageName}_${packageInfo.versionName ?: "unknown"}.apk"
        return File(tempDir, filename).absolutePath
    }

    /**
     * Clean up a specific APK file
     */
    private fun cleanupApkFile(apkPath: String) {
        try {
            val apkFile = File(apkPath)
            if (apkFile.exists()) {
                apkFile.delete()
            }
        } catch (e: Exception) {
            // Log but don't throw - cleanup failures shouldn't break the main flow
            println("Warning: Failed to clean up APK file at $apkPath: ${e.message}")
        }
    }

}