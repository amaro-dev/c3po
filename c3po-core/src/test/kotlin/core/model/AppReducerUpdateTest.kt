import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import core.model.Action
import core.model.AppReducer
import core.model.AppState
import core.model.UpdateInfo
import core.model.UpdateState
import org.junit.jupiter.api.Test

class AppReducerUpdateTest {

    private val reducer = AppReducer()

    @Test
    fun `should handle UpdateCheckComplete with UpdateInfo`() {
        val initialState = AppState()
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg"
        )
        val action = Action.UpdateCheckComplete(updateInfo)

        val newState = reducer.reduce(action, initialState)

        assertThat(newState).all {
            prop(AppState::updateState).isEqualTo(UpdateState.UpdateAvailable)
            prop(AppState::updateInfo).isEqualTo(updateInfo)
        }
    }

    @Test
    fun `should handle UpdateCheckComplete with null UpdateInfo`() {
        val initialState = AppState()
        val action = Action.UpdateCheckComplete(null)

        val newState = reducer.reduce(action, initialState)

        assertThat(newState).all {
            prop(AppState::updateState).isEqualTo(UpdateState.NoUpdate)
            prop(AppState::updateInfo).isNull()
        }
    }

    @Test
    fun `should handle CheckForUpdate action by setting CheckingForUpdate state`() {
        val initialState = AppState()
        val action = Action.CheckForUpdate

        val newState = reducer.reduce(action, initialState)

        assertThat(newState.updateState).isEqualTo(UpdateState.CheckingForUpdate)
    }

    @Test
    fun `should handle DownloadUpdate action by setting Downloading state`() {
        val initialState = AppState()
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg"
        )
        val action = Action.DownloadUpdate(updateInfo)

        val newState = reducer.reduce(action, initialState)

        assertThat(newState).all {
            prop(AppState::updateState).isEqualTo(UpdateState.Downloading)
            prop(AppState::updateInfo).isEqualTo(updateInfo)
        }
    }

    @Test
    fun `should handle UpdateError by setting Error state`() {
        val initialState = AppState(updateState = UpdateState.CheckingForUpdate)
        val action = Action.UpdateError("Network error")

        val newState = reducer.reduce(action, initialState)

        assertThat(newState).all {
            prop(AppState::updateState).isEqualTo(UpdateState.Error)
            prop(AppState::errorMessage).isEqualTo("Network error")
        }
    }

    @Test
    fun `should handle UpdateDownloadProgress during download`() {
        val initialState = AppState(updateState = UpdateState.Downloading)
        val action = Action.UpdateDownloadProgress(75)

        val newState = reducer.reduce(action, initialState)

        // For now, just ensure state is maintained during progress updates
        assertThat(newState.updateState).isEqualTo(UpdateState.Downloading)
    }
}