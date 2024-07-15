package ui.plugins.intents.pending

import Settings
import commands.ListPendingActivityIntentsCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PendingIntentsMiddleware(private val name: String) : IMiddleware<AppState> {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            is Action.StartPlugin,
            PendingIntentsPlugin.Action.List -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        val results = ListPendingActivityIntentsCommand(this).run(adbPath)
                        processor.reduce(
                            Action.DeliverPluginResult(name, results.groupBy { it.packageName }.toList())
                        )
                    }
                }
            }
        }
    }
}
