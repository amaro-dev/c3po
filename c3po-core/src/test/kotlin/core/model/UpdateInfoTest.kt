import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import core.model.UpdateInfo
import org.junit.jupiter.api.Test

class UpdateInfoTest {
    @Test
    fun `should contain all required fields`() {
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg",
            checksum = "sha256:abcd1234...",
            releaseNotesUrl = "https://github.com/amaro-dev/c3po/releases/tag/v2.1.0"
        )

        assertThat(updateInfo.version).isEqualTo("2.1.0")
        assertThat(updateInfo.downloadUrl).isEqualTo("https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg")
        assertThat(updateInfo.checksum).isEqualTo("sha256:abcd1234...")
        assertThat(updateInfo.releaseNotesUrl).isEqualTo("https://github.com/amaro-dev/c3po/releases/tag/v2.1.0")
    }

    @Test
    fun `should handle optional fields properly`() {
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg",
            checksum = null,
            releaseNotesUrl = null
        )

        assertThat(updateInfo.version).isEqualTo("2.1.0")
        assertThat(updateInfo.downloadUrl).isEqualTo("https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg")
        assertThat(updateInfo.checksum).isNull()
        assertThat(updateInfo.releaseNotesUrl).isNull()
    }
}