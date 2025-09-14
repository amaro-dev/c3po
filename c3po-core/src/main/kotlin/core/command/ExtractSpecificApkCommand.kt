package core.command

import java.io.File

/**
 * Command to extract a specific APK file using its exact path
 */
class ExtractSpecificApkCommand(
    private val apkPath: String,
    private val localPath: String
) : EnhancedAdbCommand<String> {

    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "pull $apkPath $localPath",
            executionType = CommandExecutionType.ADB_DIRECT,
            timeoutMs = 30000L,
            requiresShell = false,
        )

    override fun parse(result: String): String {
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