package core.command

/**
 * ADB command to restart the connected Android device.
 *
 * This command will reboot the device and cause it to disconnect temporarily.
 * The device will reconnect automatically after the restart process completes.
 */
class RestartDeviceCommand : AdbCommand<String> {

    override val command: String = "reboot"

    override fun parse(result: String): String {
        // Check for common error patterns
        when {
            result.contains("Permission denied") -> {
                throw RuntimeException("Restart failed: Permission denied")
            }

            result.contains("device not found") || result.contains("no devices/emulators found") -> {
                throw RuntimeException("Restart failed: No device connected")
            }

            result.contains("error") || result.contains("failed") -> {
                throw RuntimeException("Restart failed: $result")
            }
        }

        // Successful restart - device will disconnect
        return "Device restart initiated"
    }
}