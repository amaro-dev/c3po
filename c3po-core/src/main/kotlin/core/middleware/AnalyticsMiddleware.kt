package core.middleware

import core.analytics.AnalyticsContext
import core.analytics.AnalyticsError
import core.analytics.AnalyticsSanitizer
import core.analytics.AnalyticsService
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor

class AnalyticsMiddleware(
    private val analyticsService: AnalyticsService
) : AsyncMiddlewareBase<AppState>() {
    private val pluginStartTimes = mutableMapOf<String, Long>()
    private val opStartTimes = mutableMapOf<String, Long>()
    private val lastEventTs = mutableMapOf<String, Long>()

    private fun now() = System.currentTimeMillis()
    private fun rateLimited(key: String, windowMs: Long): Boolean {
        val t = now()
        val last = lastEventTs[key]
        return if (last != null && t - last < windowMs) {
            true
        } else {
            lastEventTs[key] = t
            false
        }
    }

    private fun analyticsEnabled(state: AppState): Boolean = AnalyticsContext.enabled(state)

    private fun baseProps(state: AppState): Map<String, Any> = AnalyticsContext.baseProps(state)

    private fun track(state: AppState, name: String, props: Map<String, Any> = emptyMap()) {
        if (!analyticsEnabled(state)) return
        val merged = baseProps(state) + props
        analyticsService.trackEvent(name, merged)
    }

    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.InitializeAnalytics -> {
                if (analyticsEnabled(state)) analyticsService.initialize(action.serverUrl, action.appKey)
            }

            is Action.StartAnalyticsSession -> {
                track(state, "app_started")
            }

            is Action.EndAnalyticsSession -> {
                if (!rateLimited("app_closed", 1000)) track(state, "app_closed")
            }

            is Action.StartPlugin -> {
                if (!analyticsEnabled(state)) return
                if (!rateLimited("plugin_started:${action.pluginName}", 2000)) {
                    pluginStartTimes[action.pluginName] = now()
                    track(state, "plugin_started", mapOf("plugin_id" to action.pluginName))
                }
            }

            is Action.SelectPlugin -> {
                track(state, "plugin_selected", mapOf("plugin_id" to action.pluginName))
            }

            is Action.DeliverPluginResult -> {
                val started = pluginStartTimes.remove(action.plugin) ?: now()
                val duration = (now() - started).coerceAtLeast(0)
                track(
                    state,
                    "plugin_result",
                    mapOf(
                        "plugin_id" to action.plugin,
                        "item_count" to (action.items.size),
                        "duration_ms" to duration
                    )
                )
            }

            is Action.CheckForUpdate -> {
                opStartTimes["update_check"] = now()
            }

            is Action.UpdateCheckComplete -> {
                val started = opStartTimes.remove("update_check") ?: now()
                val duration = (now() - started).coerceAtLeast(0)
                track(
                    state,
                    "update_check_completed",
                    mapOf(
                        "has_update" to (action.updateInfo != null),
                        "duration_ms" to duration
                    )
                )
            }

            is Action.DownloadUpdate -> {
                opStartTimes["update_download"] = now()
            }

            is Action.UpdateDownloadComplete -> {
                val started = opStartTimes.remove("update_download") ?: now()
                val duration = (now() - started).coerceAtLeast(0)
                val bytes = try {
                    java.io.File(action.filePath).length()
                } catch (_: Exception) {
                    -1L
                }
                val version = state.updateInfo?.version ?: "unknown"
                track(
                    state,
                    "update_download_completed",
                    mapOf(
                        "version" to version,
                        "bytes" to bytes,
                        "duration_ms" to duration
                    )
                )
            }

            is Action.InstallUpdate -> {
                opStartTimes["update_install"] = now()
            }

            is Action.UpdateInstallComplete -> {
                val started = opStartTimes.remove("update_install") ?: now()
                val duration = (now() - started).coerceAtLeast(0)
                val version = state.updateInfo?.version ?: "unknown"
                track(
                    state,
                    "update_install_completed",
                    mapOf(
                        "version" to version,
                        "duration_ms" to duration
                    )
                )
            }

            is Action.UpdateError -> {
                track(state, "update_error", mapOf("reason" to AnalyticsError.categorize(action.message)))
            }

            is Action.RestartDevice -> {
                track(state, "device_restart_attempted")
            }

            is Action.SetCommandError -> {
                val key = (action::class.simpleName ?: "error") + ":" + AnalyticsSanitizer.sanitize(action.message)
                if (!rateLimited("cmd_err:$key", 1000)) {
                    track(
                        state,
                        "command_error",
                        mapOf(
                            "action_type" to (action::class.simpleName ?: "unknown"),
                            "error_kind" to AnalyticsError.categorize(action.message),
                            "message_trunc" to AnalyticsSanitizer.sanitize(action.message)
                        )
                    )
                }
            }

            is Action.ShutdownAnalytics -> {
                if (analyticsEnabled(state)) {
                    if (!rateLimited("app_closed", 1000)) track(state, "app_closed")
                    analyticsService.shutdown()
                }
            }
        }
    }

    // Error classify/sanitize implemented in core.analytics helpers
}
