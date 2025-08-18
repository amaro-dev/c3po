package core.command

import core.model.AppPackage
import java.io.File

/**
 * Command to extract APK file from Android device to local temporary folder
 */
class ExtractApkCommand(
    private val packageInfo: AppPackage,
    private val tempDir: File
) : EnhancedAdbCommand<String> {

    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "pull ${getSourceApkPath()} ${getLocalApkPath()}",
            executionType = CommandExecutionType.ADB_DIRECT,
            timeoutMs = 30000L, // 30 seconds for APK download
            requiresShell = false,
        )

    private fun getSourceApkPath(): String {
        val installPath = packageInfo.installPath
        return when {
            // If install path is provided and looks like a specific APK file
            installPath != null && installPath.endsWith(".apk") -> installPath

            // If install path is provided but is a directory, look for APK inside
            installPath != null -> {
                // Try the first common pattern - most system apps use directory name + .apk
                val directoryName = installPath.substringAfterLast("/")
                "$installPath/$directoryName.apk"
            }

            // Fallback to standard locations
            else -> "/system/app/${packageInfo.packageName}/${packageInfo.packageName}.apk"
        }
    }

    private fun getLocalApkPath(): String {
        // Ensure temp directory exists
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        // Create APK filename based on package name and version
        val filename = "${packageInfo.packageName}_${packageInfo.versionName ?: "unknown"}.apk"
        return File(tempDir, filename).absolutePath
    }

    override fun parse(result: String): String {
        val localPath = getLocalApkPath()
        val localFile = File(localPath)

        if (!localFile.exists()) {
            throw RuntimeException("APK extraction failed: File not found at $localPath")
        }

        if (localFile.length() == 0L) {
            throw RuntimeException("APK extraction failed: Empty file at $localPath")
        }

        return localPath
    }
}