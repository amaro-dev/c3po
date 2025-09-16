package core.command

/**
 * ADB command to remove a file from the Android device.
 *
 * @param devicePath The path of the file on the device to remove
 */
class RemoveDeviceFileCommand(
    private val devicePath: String
) : AdbCommand<String> {

    override val command: String = "shell rm $devicePath"

    override fun parse(result: String): String {
        // rm command typically doesn't output anything on success
        // Only report errors if they occur
        if (result.contains("Permission denied") ||
            result.contains("failed") ||
            result.contains("No such file")
        ) {
            throw RuntimeException("Remove failed: $result")
        }

        return "File removed: $devicePath"
    }
}