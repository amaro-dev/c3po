package core.facade

import Settings
import core.model.Action
import dev.amaro.sonic.IAction
import java.util.Properties

class SettingsAnalyticsFacade(
    private val runtimeConfig: AnalyticsDefaults? = null
) {

    fun prepareOnLoad(props: Properties): List<IAction> {
        val actions = mutableListOf<IAction>()

        val enabled = props.getProperty(Settings.ANALYTICS_ENABLED_PROP, "false").toBoolean()
        if (!enabled) return actions

        val serverExisting = props.getProperty(Settings.ANALYTICS_SERVER_URL_PROP, "").trim()
        val appKeyExisting = props.getProperty(Settings.ANALYTICS_APP_KEY_PROP, "").trim()
        val server = serverExisting.ifBlank { (runtimeConfig?.serverUrl ?: "") }
        val appKey = appKeyExisting.ifBlank { (runtimeConfig?.appKey ?: "") }
        if (server.isBlank() || appKey.isBlank()) return actions

        val instanceId = props.getProperty(Settings.ANALYTICS_INSTANCE_ID_PROP)
        if (instanceId.isNullOrBlank()) {
            val newId = java.util.UUID.randomUUID().toString()
            actions += Action.ChangeSettingsProperty(Settings.ANALYTICS_INSTANCE_ID_PROP, newId)
        }

        actions += Action.InitializeAnalytics(server, appKey)
        actions += Action.StartAnalyticsSession(java.util.UUID.randomUUID().toString())
        return actions
    }

    fun prepareOnToggle(enabled: Boolean, currentProps: Properties): List<IAction> {
        val actions = mutableListOf<IAction>()
        if (enabled) {
            val serverExisting = currentProps.getProperty(Settings.ANALYTICS_SERVER_URL_PROP, "").trim()
            val appKeyExisting = currentProps.getProperty(Settings.ANALYTICS_APP_KEY_PROP, "").trim()
            val server = serverExisting.ifBlank { (runtimeConfig?.serverUrl ?: "") }
            val appKey = appKeyExisting.ifBlank { (runtimeConfig?.appKey ?: "") }
            val instanceId = currentProps.getProperty(Settings.ANALYTICS_INSTANCE_ID_PROP)

            if (instanceId.isNullOrBlank()) {
                val newId = java.util.UUID.randomUUID().toString()
                actions += Action.ChangeSettingsProperty(Settings.ANALYTICS_INSTANCE_ID_PROP, newId)
            }

            if (server.isNotBlank() && appKey.isNotBlank()) {
                actions += Action.InitializeAnalytics(server, appKey)
                actions += Action.StartAnalyticsSession(java.util.UUID.randomUUID().toString())
            }
        } else {
            actions += Action.EndAnalyticsSession
            actions += Action.ShutdownAnalytics
        }
        return actions
    }
}
