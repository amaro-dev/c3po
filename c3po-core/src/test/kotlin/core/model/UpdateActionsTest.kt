import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import core.model.Action
import core.model.UpdateInfo
import dev.amaro.sonic.IAction
import org.junit.jupiter.api.Test

class UpdateActionsTest {
    @Test
    fun `CheckForUpdate should be an Action`() {
        val action = Action.CheckForUpdate

        assertThat(action).isInstanceOf(Action::class)
        assertThat(action).isInstanceOf(IAction::class)
    }

    @Test
    fun `DownloadUpdate should contain UpdateInfo`() {
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://example.com/app.dmg"
        )
        val action = Action.DownloadUpdate(updateInfo)

        assertThat(action.updateInfo).isEqualTo(updateInfo)
    }

    @Test
    fun `CancelDownload should be an Action`() {
        val action = Action.CancelDownload

        assertThat(action).isInstanceOf(Action::class)
        assertThat(action).isInstanceOf(IAction::class)
    }

    @Test
    fun `UpdateCheckComplete should handle both success and no-update cases`() {
        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://example.com/app.dmg"
        )

        val successAction = Action.UpdateCheckComplete(updateInfo)
        val noUpdateAction = Action.UpdateCheckComplete(null)

        assertThat(successAction.updateInfo).isEqualTo(updateInfo)
        assertThat(noUpdateAction.updateInfo).isEqualTo(null)
    }

    @Test
    fun `UpdateDownloadProgress should track progress percentage`() {
        val action = Action.UpdateDownloadProgress(75)

        assertThat(action.progress).isEqualTo(75)
    }

    @Test
    fun `UpdateError should contain error message`() {
        val errorMessage = "Network connection failed"
        val action = Action.UpdateError(errorMessage)

        assertThat(action.message).isEqualTo(errorMessage)
    }
}