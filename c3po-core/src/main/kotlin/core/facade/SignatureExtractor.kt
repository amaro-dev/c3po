package core.facade

import core.command.CommandRunner
import core.model.AndroidPackageReport
import core.model.SignatureInfo
import core.model.Signer
import java.io.File

class SignatureExtractor {
    companion object {
        private const val V1_DISCLAIMER = "Verified using v1 scheme (JAR signing): "
        private const val V2_DISCLAIMER = "Verified using v2 scheme (APK Signature Scheme v2): "
        private const val V3_DISCLAIMER = "Verified using v3 scheme (APK Signature Scheme v3): "
        private const val V3_1_DISCLAIMER = "Verified using v3.1 scheme (APK Signature Scheme v3.1): "
        private const val V4_DISCLAIMER = "Verified using v4 scheme (APK Signature Scheme v4): "

        /**
         * Find apksigner tool in the Android SDK
         */
        private fun findApkSigner(): String {
            // Try common Android SDK locations
            val possiblePaths = listOfNotNull(
                System.getenv("ANDROID_HOME")?.let { "$it/build-tools" },
                System.getenv("ANDROID_SDK_ROOT")?.let { "$it/build-tools" },
                "${System.getProperty("user.home")}/Library/Android/sdk/build-tools", // macOS
                "${System.getProperty("user.home")}/Android/Sdk/build-tools", // Linux
                "${System.getProperty("user.home")}/AppData/Local/Android/Sdk/build-tools" // Windows
            )

            for (buildToolsPath in possiblePaths) {
                val buildToolsDir = File(buildToolsPath)
                if (buildToolsDir.exists() && buildToolsDir.isDirectory) {
                    // Find the latest build-tools version directory
                    val versions = buildToolsDir.listFiles()?.filter { it.isDirectory }
                        ?.sortedByDescending { it.name }

                    for (versionDir in versions ?: emptyList()) {
                        val apkSigner = File(versionDir, "apksigner")
                        if (apkSigner.exists()) {
                            return apkSigner.absolutePath
                        }
                    }
                }
            }

            throw RuntimeException("apksigner tool not found. Please ensure Android SDK is installed and ANDROID_HOME is set.")
        }
    }

    suspend fun getCertificateFingerprint(filePath: String): Result<AndroidPackageReport> {
        return try {
            println("DEBUG: Finding apksigner tool...")
            val apkSignerPath = findApkSigner()
            println("DEBUG: Using apksigner at: $apkSignerPath")
            println("DEBUG: Analyzing APK file: $filePath")
            println("DEBUG: APK file exists: ${File(filePath).exists()}")

            val command = arrayOf(
                apkSignerPath,
                "verify",
                "--verbose",
                "--print-certs",
                filePath,
            )
            println("DEBUG: Executing command: ${command.joinToString(" ")}")

            val output = CommandRunner
                .run(
                    File(""),
                    command,
                ).onFailure {
                    println("DEBUG: Command failed with error: ${it.message}")
                    return Result.failure(it)
                }.getOrNull()!!

            val signerCount =
                output
                    .takeIf { "Number of signers:" in it }
                    ?.substringAfter("Number of signers:")
                    ?.substringBefore("\n")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() && it.isNotBlank() }

            val signer =
                Signer.fromParams(
                    *output
                        .substringAfter("Signer #1 certificate DN: ")
                        .substringBefore("\n")
                        .split(",")
                        .toTypedArray(),
                )
            val sha256 = output.substringAfter("Signer #1 certificate SHA-256 digest: ").substringBefore("\n")
            val sha1 = output.substringAfter("Signer #1 certificate SHA-1 digest: ").substringBefore("\n")
            val md5 = output.substringAfter("Signer #1 certificate MD5 digest: ").substringBefore("\n")

            Result.success(
                AndroidPackageReport(
                    filePath,
                    SignatureInfo(signer, sha256, sha1, md5),
                    signerCount?.toInt() ?: 0,
                    isCompliant(output, V1_DISCLAIMER),
                    isCompliant(output, V2_DISCLAIMER),
                    isCompliant(output, V3_DISCLAIMER),
                    isCompliant(output, V3_1_DISCLAIMER),
                    isCompliant(output, V4_DISCLAIMER),
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isCompliant(
        output: String,
        message: String,
    ): BoolState =
        output
            .takeIf { message in output }
            ?.substringAfter(message)
            ?.substringBefore("\n")
            ?.let { BoolState.valueOf(it.uppercase().trim()) } ?: BoolState.NOT_FOUND
}

enum class BoolState {
    TRUE,
    FALSE,
    NOT_FOUND,
}
