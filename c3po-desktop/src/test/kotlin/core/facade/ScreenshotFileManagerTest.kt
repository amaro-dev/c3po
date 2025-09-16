package core.facade

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

class ScreenshotFileManagerTest {

    @Test
    fun `getBestScreenshotDirectory returns valid directory`() {
        val directory = ScreenshotFileManager.getBestScreenshotDirectory()

        assertThat(directory).isNotNull()
        assertThat(directory.exists() || directory.mkdirs()).isTrue() // Should exist or be creatable
        assertThat(directory.isDirectory).isTrue()
    }

    @Test
    fun `createScreenshotCommand returns valid command`() {
        val command = ScreenshotFileManager.createScreenshotCommand()

        assertThat(command).isNotNull()
        // Should have proper ADB command
        assertThat(command.command).contains("screencap")
    }

    @Test
    @EnabledOnOs(OS.MAC)
    fun `openScreenshotInViewer handles non-existent file gracefully on macOS`() {
        val result = ScreenshotFileManager.openScreenshotInViewer("/tmp/non-existent-file.png")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message ?: "").contains("Failed to open screenshot")
    }

    @Test
    fun `getScreenshotLocationDescription returns helpful message`() {
        val description = ScreenshotFileManager.getScreenshotLocationDescription()

        assertThat(description).all {
            isNotNull()
            contains("Screenshots saved to")
        }
        assertThat(
            description.contains("Pictures") ||
                    description.contains("Desktop") ||
                    description.contains("Downloads") ||
                    description.contains("temporary")
        ).isTrue()
    }

    @Test
    @EnabledOnOs(OS.MAC)
    fun `checkPicturesFolderAccess returns boolean on macOS`() {
        // This test just verifies the method doesn't throw
        val hasAccess = ScreenshotFileManager.checkPicturesFolderAccess()
        assertThat(hasAccess || !hasAccess).isTrue() // Either true or false is fine
    }
}