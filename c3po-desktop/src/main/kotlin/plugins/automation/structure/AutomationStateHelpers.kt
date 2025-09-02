package plugins.automation.structure

import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IProcessor

fun IProcessor<AppState>.deliver(pluginName: String, state: AutomationState) {
    reduce(Action.DeliverPluginResult(pluginName, listOf(state)))
}

