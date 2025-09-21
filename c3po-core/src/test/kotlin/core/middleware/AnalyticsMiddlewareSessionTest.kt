package core.middleware

import Settings
import core.analytics.AnalyticsService
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IProcessor
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AnalyticsMiddlewareSessionTest {
    @Test
    fun `StartAnalyticsSession emits app_started with base props`() = runTest {
        val svc = mockk<AnalyticsService>()
        val mw = AnalyticsMiddleware(svc)
        val processor = mockk<IProcessor<AppState>>(relaxed = true)
        val state = AppState().copy(appVersion = "1.2.3").also {
            it.settings.setProperty(Settings.ANALYTICS_ENABLED_PROP, "true")
            it.settings.setProperty(Settings.ANALYTICS_INSTANCE_ID_PROP, "uid-1")
        }
        every { svc.trackEvent("app_started", any()) } just runs

        mw.process(Action.StartAnalyticsSession("sid-1"), state.copy(analyticsSessionId = "sid-1"), processor)

        verify { svc.trackEvent("app_started", match { it["client_id"] == "uid-1" && it["app_version"] == "1.2.3" }) }
    }
}

