package core.model

import Settings
import assertk.assertThat
import assertk.assertions.isEqualTo
import core.command.SystemCommandExecutor
import core.facade.SettingsRepository
import core.middleware.SettingsMiddleware
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
    val commandExecutor: SystemCommandExecutor = mockk(relaxed = true)

    @Test
    fun `Handle LoadSettings action`() = runTest {
        val properties: Properties = mockk(relaxed = true)
        val settingsRepository: SettingsRepository =
            mockk(relaxed = true) {
                every { load() } returns Result.success(properties)
            }
        val middleware = SettingsMiddleware(settingsRepository, commandExecutor, this)
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
        val middleware = SettingsMiddleware(settingsRepository, commandExecutor, this)
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
        val middleware = SettingsMiddleware(settingsRepository, commandExecutor, this)
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
        val middleware = SettingsMiddleware(settingsRepository, commandExecutor, this)
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
        coEvery { commandExecutor.executeCommand(any(), any()) } returns Result.success("")
        val middleware = SettingsMiddleware(settingsRepository, commandExecutor, this)
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
        val middleware = SettingsMiddleware(settingsRepository, commandExecutor, this)
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
        val middleware = SettingsMiddleware(settingsRepository, commandExecutor, this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(Action.ChangeSettingsProperty(Settings.DARK_MODE_PROP, "true"), state, processor)

        val slot = CapturingSlot<Action.LoadSettingsIntoState>()
        advanceUntilIdle()

        verify {
            processor.reduce(capture(slot))
        }
        assertThat(slot.captured.props[Settings.DARK_MODE_PROP]).isEqualTo("true")
    }
}