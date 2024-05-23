package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import ui.plugins.Plugin

class PluginSelectorMiddleware(plugins: List<Plugin<*>>) : IMiddleware<AppState> {
    private val registeredPlugins = plugins.associateBy { it.name }

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        if (action is Action.StartPlugin) {
            registeredPlugins[action.pluginName]?.middleware?.process(action, state, processor)
        } else {
            registeredPlugins.forEach {
                if (it.value.isResponsibleFor(action))
                    it.value.middleware.process(action, state, processor)
            }
        }
    }
}
