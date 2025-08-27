import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class SettingsTest {

    @Test
    fun `Settings should contain all expected property constants`() {
        // Verify both existing and new constants are available
        assertThat(Settings.ADB_PATH_PROP).isEqualTo("command.adb.path")
        assertThat(Settings.UPDATES_URL_PROP).isEqualTo("updates.url")
    }

    @Test
    fun `Settings should have proper file name`() {
        assertThat(Settings.FILE_NAME).isEqualTo("c3po.cfg")
    }
}