package ui.plugins.activities

import commands.ListActivitiesCommand
import commands.StartActivityCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivitiesPluginMiddleware(
    private val pluginName: String
) : IMiddleware<AppState> {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            ActivitiesPlugin.Actions.List -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        val activities = ListActivitiesCommand(this).run()
                        processor.reduce(Action.DeliverPluginResult(pluginName, activities))
                    }
                }
            }

            is ActivitiesPlugin.Actions.Launch -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        StartActivityCommand(this, action.activityInfo).run()
                    }
                }
            }
        }
    }
}
