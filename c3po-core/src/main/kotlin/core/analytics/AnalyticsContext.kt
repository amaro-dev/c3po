package core.analytics

import Settings
import core.model.AppState

object AnalyticsContext {
    fun enabled(state: AppState): Boolean =
        state.settings.getProperty(Settings.ANALYTICS_ENABLED_PROP, "false").toBoolean()

    fun baseProps(state: AppState): Map<String, Any> {
        val osName = System.getProperty("os.name") ?: "unknown"
        val osVersion = System.getProperty("os.version") ?: "unknown"
        val javaVersion = System.getProperty("java.version") ?: "unknown"
        val clientId = state.settings.getProperty(Settings.ANALYTICS_INSTANCE_ID_PROP, "unknown")
        val sessionId = state.analyticsSessionId ?: "unknown"
        return mapOf(
            "client_id" to clientId,
            "session_id" to sessionId,
            "app_version" to state.appVersion,
            "os_name" to osName,
            "os_version" to osVersion,
            "java_version" to javaVersion
        )
    }
}

