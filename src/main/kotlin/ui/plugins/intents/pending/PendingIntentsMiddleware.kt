package ui.plugins.intents.pending

import Settings
import commands.ListPendingActivityIntentsCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.plugins.PluginMiddleware

class PendingIntentsMiddleware(private val pluginName: String) : PluginMiddleware(pluginName) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            is Action.StartPlugin,
            PendingIntentsPlugin.Actions.List -> {
                coroutineScope.launch {
                    val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                    state.currentDevice?.run {
                        val results = ListPendingActivityIntentsCommand(this).run(adbPath)
                        processor.reduce(
                            Action.DeliverPluginResult(
                                pluginName,
                                results.groupBy { it.packageName }.toList(),
                                searchTerm
                            )
                        )
                    }
                }
            }
        }
    }
}
