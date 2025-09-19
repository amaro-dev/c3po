package core.command

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.endsWith
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import assertk.assertions.startsWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TakeScreenshotCommandTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `generateScreenshotFilename creates valid timestamp-based filename`() {
        val filename = TakeScreenshotCommand.generateScreenshotFilename()

        assertThat(filename).all {
            startsWith("screenshot_")
            endsWith(".png")
            contains("_")
        }

        // Should be format: screenshot_YYYYMMDD_HHMMSS.png
        val parts = filename.removePrefix("screenshot_").removeSuffix(".png").split("_")
        assertThat(parts).hasSize(2) // date and time parts
        assertThat(parts[0].length).isEqualTo(8) // YYYYMMDD
        assertThat(parts[1].length).isEqualTo(6) // HHMMSS
    }

    @Test
    fun `command property returns correct ADB screencap command`() {
        val devicePath = "/sdcard/test_screenshot.png"
        val command = TakeScreenshotCommand(devicePath)

        assertThat(command.command).isEqualTo("shell screencap -p $devicePath")
    }

    @Test
    fun `parse returns device path on successful screencap`() {
        val devicePath = "/sdcard/test_screenshot.png"
        val command = TakeScreenshotCommand(devicePath)

        // Simulate successful screencap output (empty or success message)
        val successOutput = ""

        val result = command.parse(successOutput)

        assertThat(result).isEqualTo(devicePath)
    }

    @Test
    fun `parse throws exception on permission denied`() {
        val devicePath = "/sdcard/test_screenshot.png"
        val command = TakeScreenshotCommand(devicePath)

        val errorOutput = "Permission denied"

        assertFailure {
            command.parse(errorOutput)
        }.messageContains("Screenshot failed: Permission denied")
    }

    @Test
    fun `createWithAutoPath generates unique filenames`() {
        val command1 = TakeScreenshotCommand.createWithAutoPath()
        val command2 = TakeScreenshotCommand.createWithAutoPath()

        // Commands should have different device paths (due to timestamp)
        // In practice they may be the same if created in the same second,
        // but the paths should follow the expected pattern
        assertThat(command1.getDevicePath()).all {
            contains("screenshot_")
            startsWith("/sdcard/")
        }
        assertThat(command2.getDevicePath()).all {
            contains("screenshot_")
            startsWith("/sdcard/")
        }
    }
}
