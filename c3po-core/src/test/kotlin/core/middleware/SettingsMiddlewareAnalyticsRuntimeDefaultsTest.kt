package core.middleware

import Settings
import core.facade.AdbFinder
import core.facade.AnalyticsDefaults
import core.facade.SettingsAnalyticsFacade
import core.facade.SettingsRepository
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IProcessor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.Properties

class SettingsMiddlewareAnalyticsRuntimeDefaultsTest {
    @Test
    fun `bootstraps analytics using runtime defaults when settings missing server and appKey`() = runTest {
        val props = Properties().apply {
            setProperty(Settings.ANALYTICS_ENABLED_PROP, "true")
            // No server/appKey in settings
        }

        val repo = mockk<SettingsRepository>()
        every { repo.load() } returns Result.success(props)
        val adbFinder = mockk<AdbFinder>()
        val processor = mockk<IProcessor<AppState>>(relaxed = true)

        val facade = SettingsAnalyticsFacade(AnalyticsDefaults(serverUrl = "https://example", appKey = "key"))
        val mw = SettingsMiddleware(repo, adbFinder, facade, this)

        mw.process(Action.LoadSettings, AppState(), processor)
        advanceUntilIdle()

        verify { processor.reduce(Action.LoadSettingsIntoState(props)) }
        verify { processor.perform(match { it is Action.InitializeAnalytics }) }
        verify { processor.perform(match { it is Action.StartAnalyticsSession }) }
    }
}

