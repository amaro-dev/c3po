package core.model

import Settings
import assertk.assertThat
import assertk.assertions.isEqualTo
import core.facade.AdbFinder
import core.facade.SettingsAnalyticsFacade
import core.facade.SettingsRepository
import core.middleware.SettingsMiddleware
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.Properties

class SettingsMiddlewareTest {
    val adbFinder: AdbFinder = mockk(relaxed = true)

    @Test
    fun `Handle LoadSettings action`() = runTest {
        val properties: Properties = mockk(relaxed = true)
        val settingsRepository: SettingsRepository =
            mockk(relaxed = true) {
                every { load() } returns Result.success(properties)
            }
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(Action.LoadSettings, mockk(), processor)
        advanceUntilIdle()
        verify {
            processor.reduce(Action.LoadSettingsIntoState(properties))
        }
    }

    @Test
    fun `Handle LoadSettings action triggers RefreshDevices`() = runTest {
        val properties: Properties = mockk(relaxed = true)
        val settingsRepository: SettingsRepository =
            mockk(relaxed = true) {
                every { load() } returns Result.success(properties)
            }
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(Action.LoadSettings, mockk(), processor)
        advanceUntilIdle()

        verify {
            processor.perform(Action.RefreshDevices)
        }
    }

    @Test
    fun `Handle LoadSettings informs when failing to load settings`() = runTest {
        val settingsRepository: SettingsRepository =
            mockk(relaxed = true) {
                every { load() } returns Result.failure(Exception())
            }
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(Action.LoadSettings, mockk(), processor)
        advanceUntilIdle()

        verify {
            processor.reduce(Action.SettingsNotFound)
        }
    }

    @Test
    fun `Handle ChangeSettingsProperty action`() = runTest {
        val properties = Properties()
        properties["prop"] = "value"
        val state = AppState(settings = properties)
        val settingsRepository: SettingsRepository = mockk(relaxed = true)
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(Action.ChangeSettingsProperty("prop", "new-value"), state, processor)

        val slot = CapturingSlot<Action.LoadSettingsIntoState>()
        advanceUntilIdle()

        verify {
            processor.reduce(capture(slot))
        }
        assertThat(slot.captured.props["prop"]).isEqualTo("new-value")
    }

    @Test
    fun `If ADB path setting was changed, refresh available devices`() = runTest {
        val properties = Properties()
        properties[Settings.ADB_PATH_PROP] = "value"
        val state = AppState(settings = properties)
        val settingsRepository: SettingsRepository = mockk(relaxed = true)
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(Action.ChangeSettingsProperty(Settings.ADB_PATH_PROP, "new-value"), state, processor)
        advanceUntilIdle()

        verify {
            processor.perform(Action.RefreshDevices)
        }
    }

    @Test
    fun `Handle SaveSettings action`() = runTest {
        val settings: Properties = mockk(relaxed = true)
        val state = AppState(settings = settings)
        val settingsRepository: SettingsRepository = mockk(relaxed = true)
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(Action.SaveSettings, state, processor)
        advanceUntilIdle()

        verify {
            settingsRepository.save(settings)
        }
    }

    @Test
    fun `Handle dark mode setting change`() = runTest {
        val properties = Properties()
        val state = AppState(settings = properties)
        val settingsRepository: SettingsRepository = mockk(relaxed = true)
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(Action.ChangeSettingsProperty(Settings.DARK_MODE_PROP, "true"), state, processor)

        val slot = CapturingSlot<Action.LoadSettingsIntoState>()
        advanceUntilIdle()

        verify {
            processor.reduce(capture(slot))
        }
        assertThat(slot.captured.props[Settings.DARK_MODE_PROP]).isEqualTo("true")
    }

    @Test
    fun `SearchAdbPath toggles global loading and updates adb path on success`() = runTest {
        val settingsRepository: SettingsRepository = mockk(relaxed = true)
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        coEvery { adbFinder.find() } returns Result.success("/opt/homebrew/bin/adb")

        middleware.process(Action.SearchAdbPath, AppState(), processor)
        advanceUntilIdle()

        verify {
            processor.reduce(Action.SetAdbPathSearching)
            processor.reduce(Action.SetCommandRunning)
            processor.reduce(Action.SetAdbPathSearchResult("/opt/homebrew/bin/adb"))
            processor.perform(Action.ChangeSettingsProperty(Settings.ADB_PATH_PROP, "/opt/homebrew/bin/adb"))
            processor.reduce(Action.SetCommandCompleted)
        }
    }

    @Test
    fun `SearchAdbPath toggles global loading and sets inline error on failure`() = runTest {
        val settingsRepository: SettingsRepository = mockk(relaxed = true)
        val middleware = SettingsMiddleware(settingsRepository, adbFinder, SettingsAnalyticsFacade(), this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        coEvery { adbFinder.find() } returns Result.failure(IllegalStateException("Not found"))

        middleware.process(Action.SearchAdbPath, AppState(), processor)
        advanceUntilIdle()

        verify {
            processor.reduce(Action.SetAdbPathSearching)
            processor.reduce(Action.SetCommandRunning)
            processor.reduce(match<IAction> { it is Action.SetAdbPathSearchError })
            processor.reduce(Action.SetCommandCompleted)
        }
    }
}
