package ui.component

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.junit.jupiter.api.Test

/**
 * Unit tests for SettingsDialog component.
 * Since this is a Compose Desktop application without UI testing framework,
 * we test the integration points and verify our Settings constants are available.
 */
class SettingsDialogTest {


    @Test
    fun `FilePickerMode enum should be accessible for FilePickerDialog`() {
        // Test that our FilePickerMode enum values are available
        val fileMode = FilePickerMode.FILE
        val dirMode = FilePickerMode.DIRECTORY

        assertThat(fileMode.name).isEqualTo("FILE")
        assertThat(dirMode.name).isEqualTo("DIRECTORY")
    }

    @Test
    fun `FilePickerFilter should be constructable with expected parameters`() {
        // Test that our FilePickerFilter data class works as expected
        val filter = FilePickerFilter("Executable Files", listOf("exe", ""))
        assertThat(filter).all {
            prop(FilePickerFilter::description).isEqualTo("Executable Files")
            prop(FilePickerFilter::extensions).hasSize(2)
        }
    }
}