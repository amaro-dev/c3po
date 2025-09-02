package core.facade

import core.command.CommandExecutor
import core.command.ExtractApkCommand
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
            // Step 1: Extract APK from device to temp folder
            val extractResult = commandExecutor.go(ExtractApkCommand(packageInfo, tempDir), adbPath, device)

            extractResult.fold(
                onSuccess = { apkPath ->
                    // Step 2: Analyze signature of the extracted APK
                    val analysisResult = signatureExtractor.getCertificateFingerprint(apkPath)

                    // Step 3: Clean up the APK file after analysis
                    cleanupApkFile(apkPath)

                    analysisResult
                },
                onFailure = { exception ->
                    Result.failure(RuntimeException("APK extraction failed: ${exception.message}", exception))
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
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