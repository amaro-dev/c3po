package core

import core.model.Action
import core.model.AppState
import core.model.CommandStatus
import core.model.SettingsState
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
                // Check if system is initializing - queue during:
                // 1. Device operations in progress (Running)
                // 2. Initial startup phase (before settings loaded or before device is set)
                val isInitializing = state.commandStatus == CommandStatus.Running || 
                                   state.settingsState != SettingsState.Initialized ||
                                   state.currentDevice == null
                
                if (isInitializing) {
                    queuedPluginActions.add(action)
                    return
                }
                
                processStartPlugin(action, state, processor)
            }
            
            is Action.UpdatedState -> {
                // Check if we just finished initialization
                val oldStatus = action.old.commandStatus
                val newStatus = action.new.commandStatus
                val hasDevice = action.new.currentDevice != null
                val hasPlugin = action.new.currentPlugin != null
                val settingsReady = action.new.settingsState == SettingsState.Initialized
                
                // Trigger processing when:
                // 1. CommandStatus transitions from Running to Completed/Idle
                // 2. We have a device and settings are ready
                // 3. No plugin is currently selected
                val initializationComplete = (oldStatus == CommandStatus.Running && 
                                            (newStatus == CommandStatus.Completed || newStatus == CommandStatus.Idle)) ||
                                           (oldStatus != CommandStatus.Idle && newStatus == CommandStatus.Idle && hasDevice && settingsReady)
                
                if (initializationComplete && hasDevice && !hasPlugin) {
                    // Process queued plugin actions when initialization is complete
                    if (queuedPluginActions.isNotEmpty()) {
                        val actionsToProcess = queuedPluginActions.toList()
                        queuedPluginActions.clear()
                        
                        // Process the most recent plugin action (user's latest intent)
                        actionsToProcess.lastOrNull()?.let { queuedAction ->
                            processStartPlugin(queuedAction, action.new, processor)
                        }
                    }
                    // Note: Device plugin auto-selection is now handled by DeviceMiddleware
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