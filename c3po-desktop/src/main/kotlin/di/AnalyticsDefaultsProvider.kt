package di

import Settings
import core.facade.AnalyticsDefaults
import java.io.File
import java.util.Properties

object AnalyticsDefaultsProvider {
    fun loadFromRuntime(): AnalyticsDefaults {
        // 1) System properties (e.g., -Danalytics.serverUrl=... -Danalytics.appKey=...)
        val sysServer = System.getProperty("analytics.serverUrl")
        val sysAppKey = System.getProperty("analytics.appKey")

        // 2) Environment variables (ideal for CI secrets)
        val envServer = System.getenv("C3PO_ANALYTICS_SERVER_URL")
        val envAppKey = System.getenv("C3PO_ANALYTICS_APP_KEY")

        var server = sysServer ?: envServer
        var appKey = sysAppKey ?: envAppKey

        // 3) Optional: local.properties in debug mode only (dev convenience)
        if ((server.isNullOrBlank() || appKey.isNullOrBlank()) && Settings.isDebug()) {
            try {
                val f = File("local.properties")
                if (f.exists()) {
                    val p = Properties()
                    f.inputStream().use { p.load(it) }
                    server = server ?: p.getProperty("analytics.serverUrl")
                    appKey = appKey ?: p.getProperty("analytics.appKey")
                }
            } catch (_: Exception) {
            }
        }

        return AnalyticsDefaults(serverUrl = server, appKey = appKey)
    }
}
