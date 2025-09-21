package core.middleware

import Settings
import core.facade.AdbFinder
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

class SettingsMiddlewareAnalyticsTest {
    private val repo = mockk<SettingsRepository>()
    private val adbFinder = mockk<AdbFinder>()
    private val analyticsFacade = core.facade.SettingsAnalyticsFacade()
    private val processor = mockk<IProcessor<AppState>>(relaxed = true)

    @Test
    fun `loads settings and bootstraps analytics when enabled`() = runTest {
        val props = Properties().apply {
            setProperty(Settings.ANALYTICS_ENABLED_PROP, "true")
            setProperty(Settings.ANALYTICS_SERVER_URL_PROP, "https://example")
            setProperty(Settings.ANALYTICS_APP_KEY_PROP, "app-key")
        }

        every { repo.load() } returns Result.success(props)

        val mw = SettingsMiddleware(repo, adbFinder, analyticsFacade, this)
        mw.process(Action.LoadSettings, AppState(), processor)
        advanceUntilIdle()

        verify { processor.reduce(Action.LoadSettingsIntoState(props)) }
        verify { processor.perform(match { it is Action.InitializeAnalytics }) }
        verify { processor.perform(match { it is Action.StartAnalyticsSession }) }
        // StartAnalyticsSession will trigger app_started inside AnalyticsMiddleware
    }

    @Test
    fun `does not bootstrap analytics when disabled`() = runTest {
        val props = Properties().apply {
            setProperty(Settings.ANALYTICS_ENABLED_PROP, "false")
            setProperty(Settings.ANALYTICS_SERVER_URL_PROP, "https://example")
            setProperty(Settings.ANALYTICS_APP_KEY_PROP, "app-key")
        }

        every { repo.load() } returns Result.success(props)

        val mw = SettingsMiddleware(repo, adbFinder, analyticsFacade, this)
        mw.process(Action.LoadSettings, AppState(), processor)
        advanceUntilIdle()

        verify { processor.reduce(Action.LoadSettingsIntoState(props)) }
        verify(exactly = 0) { processor.perform(match { it is Action.InitializeAnalytics }) }
        verify(exactly = 0) { processor.perform(match { it is Action.StartAnalyticsSession }) }
        // No analytics init/tracking when disabled
    }
}
