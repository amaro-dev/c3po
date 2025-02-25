package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor

class PluginSelectorMiddleware(
    plugins: List<plugins.Plugin<*>>,
) : IMiddleware<AppState> {
    private val registeredPlugins = plugins.associateBy { it.id }

    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        if (action is Action.StartPlugin) {
            val plugin = registeredPlugins[action.pluginName] ?: return
            processor.reduce(Action.SetCommandRunning)
            processor.reduce(Action.SelectPlugin(action.pluginName))
            plugin.middleware.process(action, state, processor)
        } else {
            registeredPlugins.forEach {
                if (it.value.isResponsibleFor(action)) {
                    it.value.middleware.process(action, state, processor)
                }
            }
        }
    }
}
