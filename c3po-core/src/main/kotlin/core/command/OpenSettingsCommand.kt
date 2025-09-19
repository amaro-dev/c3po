package core.command

/**
 * ADB command to open the Android Settings app on the connected device.
 *
 * This command uses the Android Activity Manager (am) to launch the Settings
 * activity using the standard settings intent action.
 */
class OpenSettingsCommand : AdbCommand<Unit> {

    override val command: String = "shell am start -a android.settings.SETTINGS"

    override fun parse(result: String) {
        // Check for common error patterns
        when {
            result.contains("Permission denied") -> {
                throw RuntimeException("Failed to open Settings: Permission denied")
            }

            result.contains("device not found") || result.contains("no devices/emulators found") -> {
                throw RuntimeException("Failed to open Settings: No device connected")
            }

            result.contains("Activity not found") || result.contains("No Activity found") -> {
                throw RuntimeException("Failed to open Settings: Settings app not available on device")
            }

            result.contains("Error:") || result.contains("error") || result.contains("failed") -> {
                throw RuntimeException("Failed to open Settings: $result")
            }
        }

        // Success case - activity manager should start the Settings app
        // No specific output validation needed for successful am start commands
    }
}