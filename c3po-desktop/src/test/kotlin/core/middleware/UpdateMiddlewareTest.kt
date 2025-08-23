import core.facade.UpdateService
import core.middleware.UpdateMiddleware
import core.model.Action
import core.model.AppState
import core.model.UpdateInfo
import dev.amaro.sonic.IProcessor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.Properties

class UpdateMiddlewareTest {

    private val updateService: UpdateService = mockk()
    private val processor: IProcessor<AppState> = mockk(relaxed = true)
    private val updateMiddleware = UpdateMiddleware(updateService)

    @Test
    fun `should handle CheckForUpdate action`() = runTest {
        val appState = AppState(
            settings = Properties().apply {
                setProperty("update.check.url", "https://api.github.com/repos/test/repo/releases/latest")
                setProperty("update.auto.enabled", "true")
            }
        )

        val updateInfo = UpdateInfo(
            version = "2.1.0",
            downloadUrl = "https://github.com/test/repo/releases/download/v2.1.0/app.dmg"
        )

        coEvery { updateService.checkForUpdates(any(), any()) } returns updateInfo

        updateMiddleware.asyncProcess(Action.CheckForUpdate, appState, processor)

        coVerify { updateService.checkForUpdates("2.0.1", "https://api.github.com/repos/test/repo/releases/latest") }
        verify { processor.reduce(Action.UpdateCheckComplete(updateInfo)) }
    }

    @Test
    fun `should dispatch UpdateCheckComplete with null when no update available`() = runTest {
        val appState = AppState(
            settings = Properties().apply {
                setProperty("update.auto.enabled", "true")
            }
        )

        coEvery { updateService.checkForUpdates(any(), any()) } returns null

        updateMiddleware.asyncProcess(Action.CheckForUpdate, appState, processor)

        verify { processor.reduce(Action.UpdateCheckComplete(null)) }
    }

    @Test
    fun `should handle errors gracefully and dispatch UpdateError`() = runTest {
        val appState = AppState(
            settings = Properties().apply {
                setProperty("update.auto.enabled", "true")
            }
        )

        coEvery { updateService.checkForUpdates(any(), any()) } throws RuntimeException("Network error")

        updateMiddleware.asyncProcess(Action.CheckForUpdate, appState, processor)

        verify { processor.reduce(Action.UpdateError("Failed to check for updates: Network error")) }
    }

    @Test
    fun `should use default URL when not configured`() = runTest {
        val appState = AppState(
            settings = Properties().apply {
                setProperty("update.auto.enabled", "true")
            }
        )

        coEvery { updateService.checkForUpdates(any(), any()) } returns null

        updateMiddleware.asyncProcess(Action.CheckForUpdate, appState, processor)

        coVerify {
            updateService.checkForUpdates(
                "2.0.1",
                "https://api.github.com/repos/amaro-dev/c3po/releases/latest"
            )
        }
    }

    @Test
    fun `should skip update check when disabled in settings`() = runTest {
        val appState = AppState(
            settings = Properties().apply {
                setProperty("update.auto.enabled", "false")
            }
        )

        updateMiddleware.asyncProcess(Action.CheckForUpdate, appState, processor)

        coVerify(exactly = 0) { updateService.checkForUpdates(any(), any()) }
        verify { processor.reduce(Action.UpdateError("Auto-updates are disabled")) }
    }
}