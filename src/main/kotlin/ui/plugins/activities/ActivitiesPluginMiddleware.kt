package ui.plugins.activities

import commands.ListActivitiesCommand
import commands.StartActivityCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor

class ActivitiesPluginMiddleware(
    private val pluginName: String
) : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            ActivitiesPlugin.Actions.List -> {
                state.currentDevice?.run {
                    val activities = ListActivitiesCommand(this).run()
                    processor.reduce(Action.DeliverPluginResult(pluginName, activities))
                }
            }

            is ActivitiesPlugin.Actions.Launch -> {
                state.currentDevice?.run {
                    StartActivityCommand(this, action.activityInfo).run()
                }
            }
        }
    }

}
