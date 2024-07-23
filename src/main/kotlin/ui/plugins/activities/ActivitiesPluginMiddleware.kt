package ui.plugins.activities

import Settings
import commands.ListActivitiesCommand
import commands.StartActivityCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.plugins.PluginMiddleware

class ActivitiesPluginMiddleware(
    private val pluginName: String
) : PluginMiddleware(pluginName) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)

        when (action) {
            is Action.StartPlugin,
            ActivitiesPlugin.Actions.List -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        val activities = ListActivitiesCommand(this).run(adbPath)
                        processor.reduce(Action.DeliverPluginResult(pluginName, activities))
                    }
                }
            }

            is ActivitiesPlugin.Actions.Launch -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        StartActivityCommand(this, action.activityInfo).run(adbPath)
                    }
                    processor.reduce(Action.SetCommandCompleted)
                }
            }
        }
    }
}
