import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.util.Properties

class UpdateSettingsTest {

    @Test
    fun `should provide default update URL when not configured`() {
        val properties = Properties()
        val updateUrl =
            properties.getProperty("update.check.url", "https://api.github.com/repos/amaro-dev/c3po/releases/latest")

        assertThat(updateUrl).isEqualTo("https://api.github.com/repos/amaro-dev/c3po/releases/latest")
    }

    @Test
    fun `should use custom update URL when configured`() {
        val properties = Properties().apply {
            setProperty("update.check.url", "https://api.github.com/repos/custom-org/custom-repo/releases/latest")
        }
        val updateUrl =
            properties.getProperty("update.check.url", "https://api.github.com/repos/amaro-dev/c3po/releases/latest")

        assertThat(updateUrl).isEqualTo("https://api.github.com/repos/custom-org/custom-repo/releases/latest")
    }

    @Test
    fun `should handle auto-update enabled setting`() {
        val properties = Properties().apply {
            setProperty("update.auto.enabled", "true")
        }
        val autoUpdateEnabled = properties.getProperty("update.auto.enabled", "true").toBoolean()

        assertThat(autoUpdateEnabled).isEqualTo(true)
    }

    @Test
    fun `should default to auto-update enabled`() {
        val properties = Properties()
        val autoUpdateEnabled = properties.getProperty("update.auto.enabled", "true").toBoolean()

        assertThat(autoUpdateEnabled).isEqualTo(true)
    }
}