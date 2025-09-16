package core.command

/**
 * ADB command to capture a screenshot from the connected Android device.
 *
 * This command saves the screenshot directly to the device's external storage,
 * then the caller can use a separate pull command to retrieve it.
 *
 * @param devicePath The path on the device where to save the screenshot
 */
class TakeScreenshotCommand(
    private val devicePath: String = "/sdcard/screenshot_${System.currentTimeMillis()}.png"
) : AdbCommand<String> {

    override val command: String = "shell screencap -p $devicePath"

    override fun parse(result: String): String {
        // If there's any error output, the screenshot failed
        if (result.contains("Permission denied") || result.contains("failed") || result.contains("error")) {
            throw RuntimeException("Screenshot failed: $result")
        }
        // Return the device path where the screenshot was saved
        return devicePath
    }

    /**
     * Gets the device path where the screenshot will be saved
     */
    fun getDevicePath(): String = devicePath

    /**
     * Generates a unique filename for screenshots based on current timestamp
     */
    companion object {
        fun generateScreenshotFilename(): String {
            val timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            return "screenshot_$timestamp.png"
        }

        /**
         * Creates a screenshot command with automatic device path generation
         */
        fun createWithAutoPath(): TakeScreenshotCommand {
            val filename = generateScreenshotFilename()
            val devicePath = "/sdcard/$filename"
            return TakeScreenshotCommand(devicePath)
        }
    }
}