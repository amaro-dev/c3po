package core.command

import java.io.File

/**
 * ADB command to pull a file from the Android device to the local filesystem.
 *
 * @param devicePath The path of the file on the device
 * @param localFile The local file where the content will be saved
 */
class PullFileCommand(
    private val devicePath: String,
    private val localFile: File
) : AdbCommand<String> {

    override val command: String = "pull $devicePath ${localFile.absolutePath}"

    override fun parse(result: String): String {
        // Check for common error patterns
        if (result.contains("No such file or directory") ||
            result.contains("failed") ||
            result.contains("Permission denied")
        ) {
            throw RuntimeException("Pull failed: $result")
        }

        // Check if the local file was actually created
        if (!localFile.exists()) {
            throw RuntimeException("Pull failed: Local file not created at ${localFile.absolutePath}")
        }

        if (localFile.length() == 0L) {
            throw RuntimeException("Pull failed: Empty file created at ${localFile.absolutePath}")
        }

        return localFile.absolutePath
    }
}