package core.command

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import org.junit.jupiter.api.Test

class RestartDeviceCommandTest {

    @Test
    fun `command property returns correct ADB reboot command`() {
        val command = RestartDeviceCommand()

        assertThat(command.command).isEqualTo("reboot")
    }

    @Test
    fun `parse returns success message on successful reboot`() {
        val command = RestartDeviceCommand()

        // Simulate successful reboot output (usually empty)
        val successOutput = ""

        val result = command.parse(successOutput)

        assertThat(result).isEqualTo("Device restart initiated")
    }

    @Test
    fun `parse throws exception on permission denied`() {
        val command = RestartDeviceCommand()

        val errorOutput = "Permission denied"
        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Restart failed: Permission denied")

    }

    @Test
    fun `parse throws exception on device not found`() {
        val command = RestartDeviceCommand()

        val errorOutput = "device not found"

        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Restart failed: No device connected")
    }

    @Test
    fun `parse throws exception on no devices found`() {
        val command = RestartDeviceCommand()

        val errorOutput = "no devices/emulators found"

        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Restart failed: No device connected")
    }

    @Test
    fun `parse throws exception on generic error`() {
        val command = RestartDeviceCommand()

        val errorOutput = "failed to restart device"

        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Restart failed:")
        assertFailure {
            command.parse(errorOutput)
        }.messageContains("failed to restart device")
    }
}
