package core.command

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import org.junit.jupiter.api.Test

class OpenSettingsCommandTest {

    @Test
    fun `command property returns correct ADB command to open settings`() {
        val command = OpenSettingsCommand()

        assertThat(command.command).isEqualTo("shell am start -a android.settings.SETTINGS")
    }

    @Test
    fun `parse succeeds on successful settings launch`() {
        val command = OpenSettingsCommand()

        // Simulate successful activity manager output (usually empty or contains "Starting")
        val successOutput = ""

        // Should not throw exception
        command.parse(successOutput)
    }

    @Test
    fun `parse succeeds on activity manager success message`() {
        val command = OpenSettingsCommand()

        // Activity manager sometimes returns this when successful
        val successOutput = "Starting: Intent { act=android.settings.SETTINGS }"

        // Should not throw exception
        command.parse(successOutput)
    }

    @Test
    fun `parse throws exception on permission denied`() {
        val command = OpenSettingsCommand()

        val errorOutput = "Permission denied"
        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Failed to open Settings: Permission denied")
    }

    @Test
    fun `parse throws exception on device not found`() {
        val command = OpenSettingsCommand()

        val errorOutput = "device not found"
        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Failed to open Settings: No device connected")
    }

    @Test
    fun `parse throws exception on no devices found`() {
        val command = OpenSettingsCommand()

        val errorOutput = "no devices/emulators found"
        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Failed to open Settings: No device connected")
    }

    @Test
    fun `parse throws exception on activity not found`() {
        val command = OpenSettingsCommand()

        val errorOutput = "Activity not found to handle Intent"
        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Failed to open Settings: Settings app not available on device")
    }

    @Test
    fun `parse throws exception on generic error`() {
        val command = OpenSettingsCommand()

        val errorOutput = "Error: Unable to start activity"
        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Failed to open Settings: Error: Unable to start activity")
    }
}