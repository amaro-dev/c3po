package core

import core.model.Action
import core.model.AppState
import core.model.CommandStatus
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor

class PluginSelectorMiddleware(
    plugins: List<plugins.Plugin<*>>,
) : IMiddleware<AppState> {
    private val registeredPlugins = plugins.associateBy { it.id }
    private val queuedPluginActions = mutableListOf<Action.StartPlugin>()

    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.StartPlugin -> {
                // Check if system is initializing (device operations in progress)
                if (state.commandStatus == CommandStatus.Running) {
                    queuedPluginActions.add(action)
                    return
                }
                
                processStartPlugin(action, state, processor)
            }
            
            is Action.UpdatedState -> {
                // Check if we just transitioned from Running to Completed/Idle with a device but no plugin
                val oldStatus = action.old.commandStatus
                val newStatus = action.new.commandStatus
                val hasDevice = action.new.currentDevice != null
                val hasPlugin = action.new.currentPlugin != null
                
                if (oldStatus == CommandStatus.Running && 
                    (newStatus == CommandStatus.Completed || newStatus == CommandStatus.Idle) &&
                    hasDevice && !hasPlugin) {
                    
                    // Process queued plugin actions first
                    if (queuedPluginActions.isNotEmpty()) {
                        val actionsToProcess = queuedPluginActions.toList()
                        queuedPluginActions.clear()
                        
                        // Process the most recent plugin action (user's latest intent)
                        actionsToProcess.lastOrNull()?.let { queuedAction ->
                            processStartPlugin(queuedAction, action.new, processor)
                        }
                    } else {
                        // Auto-select Device plugin if no plugin is selected and device is available
                        processStartPlugin(Action.StartPlugin("DEVICE"), action.new, processor)
                    }
                }
            }
            
            else -> {
                // Handle other plugin-responsible actions
                registeredPlugins.forEach {
                    if (it.value.isResponsibleFor(action)) {
                        it.value.middleware.process(action, state, processor)
                    }
                }
            }
        }
    }
    
    private fun processStartPlugin(
        action: Action.StartPlugin,
        state: AppState,
        processor: IProcessor<AppState>
    ) {
        val plugin = registeredPlugins[action.pluginName] ?: return
        processor.reduce(Action.SetCommandRunning)
        processor.reduce(Action.SelectPlugin(action.pluginName))
        plugin.middleware.process(action, state, processor)
    }
}