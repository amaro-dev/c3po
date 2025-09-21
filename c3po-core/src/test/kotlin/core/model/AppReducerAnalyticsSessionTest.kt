package core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppReducerAnalyticsSessionTest {
    private val reducer = AppReducer()

    @Test
    fun `StartAnalyticsSession sets session id`() {
        val initial = AppState()
        val next = reducer.reduce(Action.StartAnalyticsSession("sid-123"), initial)
        assertEquals("sid-123", next.analyticsSessionId)
    }

    @Test
    fun `EndAnalyticsSession clears session id`() {
        val withSession = AppState(analyticsSessionId = "sid-123")
        val next = reducer.reduce(Action.EndAnalyticsSession, withSession)
        assertEquals(null, next.analyticsSessionId)
    }
}

