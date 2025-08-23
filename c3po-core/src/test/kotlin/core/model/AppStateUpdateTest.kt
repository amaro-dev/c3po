import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import core.model.AppState
import core.model.UpdateInfo
import core.model.UpdateState
import org.junit.jupiter.api.Test

class AppStateUpdateTest {
    @Test
    fun `AppState should contain update fields`() {
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://example.com/app.dmg"
        )

        val appState = AppState(
            updateState = UpdateState.UpdateAvailable,
            updateInfo = updateInfo
        )

        assertThat(appState.updateState).isEqualTo(UpdateState.UpdateAvailable)
        assertThat(appState.updateInfo).isEqualTo(updateInfo)
    }

    @Test
    fun `AppState should handle null update info`() {
        val appState = AppState(
            updateState = UpdateState.NoUpdate,
            updateInfo = null
        )

        assertThat(appState.updateState).isEqualTo(UpdateState.NoUpdate)
        assertThat(appState.updateInfo).isNull()
    }
}