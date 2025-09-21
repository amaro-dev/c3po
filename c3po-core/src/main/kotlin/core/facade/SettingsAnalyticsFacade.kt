package core.facade

import Settings
import core.model.Action
import dev.amaro.sonic.IAction
import java.util.Properties

class SettingsAnalyticsFacade {

    fun prepareOnLoad(props: Properties): List<IAction> {
        val enabled = props.getProperty(Settings.ANALYTICS_ENABLED_PROP, "false").toBoolean()
        if (!enabled) return emptyList()

        val server = props.getProperty(Settings.ANALYTICS_SERVER_URL_PROP, "").trim()
        val appKey = props.getProperty(Settings.ANALYTICS_APP_KEY_PROP, "").trim()
        if (server.isBlank() || appKey.isBlank()) return emptyList()

        val actions = mutableListOf<IAction>()
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
            val server = currentProps.getProperty(Settings.ANALYTICS_SERVER_URL_PROP, "").trim()
            val appKey = currentProps.getProperty(Settings.ANALYTICS_APP_KEY_PROP, "").trim()
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

