package plugins.automation.structure

import core.command.CommandExecutor
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import plugins.PluginMiddleware
import plugins.automation.definition.AutomationPlugin

class AutomationMiddleware(
    pluginName: String,
    private val scriptStorage: ScriptStorage,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    private val runner = ScriptRunner(pluginName)

    // Debounce mechanism for EditStep actions
    private var lastEditStepTime = 0L
    private var lastEditStepIndex = -1
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is AutomationPlugin.Actions.DismissOpenScriptError -> {
                val current = getCurrentState(state)
                processor.deliver(
                    pluginName,
                    current.copy(openScriptError = null, malformedScriptFolderPath = null)
                )
            }

            is AutomationPlugin.Actions.OpenScript -> {
                val current = getCurrentState(state)
                processor.deliver(pluginName, current.copy(showOpenScriptPicker = true))
            }

            is AutomationPlugin.Actions.ScriptFolderChosen -> {
                val current = getCurrentState(state)
                try {
                    val script = scriptStorage.loadScriptFromFolder(action.folderPath)
                    processor.deliver(
                        pluginName,
                        current.copy(
                            isCreatingScript = true,
                            currentScript = script,
                            showOpenScriptPicker = false,
                            openScriptError = null,
                            malformedScriptFolderPath = null,
                            currentScriptFolder = action.folderPath,
                        )
                    )
                } catch (e: Exception) {
                    processor.deliver(
                        pluginName,
                        current.copy(
                            showOpenScriptPicker = false,
                            openScriptError = "Cannot open script: \${e.message}",
                            malformedScriptFolderPath = action.folderPath,
                        )
                    )
                }
            }

            is AutomationPlugin.Actions.SaveScript -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                if (currentScript.name.isBlank()) return
                try {
                    scriptStorage.saveScript(currentScript)
                    val scriptFolder = scriptStorage.getScriptFolder(currentScript.name).absolutePath
                    val newState = currentState.copy(
                        isCreatingScript = false,
                        currentScript = null,
                        currentScriptFolder = scriptFolder,
                    )
                    processor.deliver(pluginName, newState)
                } catch (_: Exception) {
                }
            }

            is AutomationPlugin.Actions.RunScript -> {
                val current = getCurrentState(state)
                val script = current.currentScript ?: return
                val folder = current.currentScriptFolder ?: return
                if (!state.hasDeviceSet) return
                runner.run(script, folder, state, processor, executor)
            }

            is Action.StartPlugin -> {
                // Only initialize if we don't have state already
                val currentState = getCurrentState(state)
                if (currentState.availablePackages.isEmpty() && currentState.availableActivities.isEmpty()) {
                    // Load initial state
                    val initialState = AutomationState()
                    processor.deliver(pluginName, initialState)

                    // Preload packages and activities together to avoid state conflicts
                    var packagesLoaded: List<core.model.AppPackage>? = null
                    var activitiesLoaded: List<core.model.ActivityInfo>? = null

                    // Load packages
                    execute(core.command.ListPackagesCommand(), state, executor)
                        .handle(processor) { packages ->
                            packagesLoaded = packages

                            // Check if both are loaded, then update state once
                            if (activitiesLoaded != null) {
                                val currentState = getCurrentState(state)
                                val updatedState = currentState.copy(
                                    availablePackages = packages,
                                    availableActivities = activitiesLoaded!!
                                )
                                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(updatedState)))
                            }
                        }

                    // Load activities
                    execute(core.command.ListActivitiesCommand(), state, executor)
                        .handle(processor) { activities ->
                            activitiesLoaded = activities

                            // Check if both are loaded, then update state once
                            if (packagesLoaded != null) {
                                val currentState = getCurrentState(state)
                                val updatedState = currentState.copy(
                                    availablePackages = packagesLoaded,
                                    availableActivities = activities
                                )
                                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(updatedState)))
                            }
                        }
                }
                // If we already have state, don't overwrite it
            }

            is AutomationPlugin.Actions.CreateNewScript -> {
                val currentState = getCurrentState(state)
                val newState =
                    currentState.copy(
                        isCreatingScript = true,
                        currentScript = Script(name = "", version = "1.0", formatVersion = "1.0"),
                    )
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.SetScriptName -> {
                val currentState = getCurrentState(state)
                val updatedScript = currentState.currentScript?.copy(name = action.name)
                val newState = currentState.copy(currentScript = updatedScript)
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.AddStep -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return

                val newStep = createDefaultStep(action.stepType)
                val updatedSteps = currentScript.steps + newStep
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(currentScript = updatedScript)

                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.RemoveStep -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return

                val updatedSteps = currentScript.steps.filterIndexed { index, _ -> index != action.index }
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(currentScript = updatedScript)

                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.CancelScript -> {
                val currentState = getCurrentState(state)
                val newState =
                    currentState.copy(
                        isCreatingScript = false,
                        currentScript = null,
                    )
                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.LoadScripts -> {
                // TODO: Implement script loading (Use Case 2)
                val currentState = getCurrentState(state)
                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(currentState)))
            }
        }
    }

    private fun getCurrentState(appState: AppState): AutomationState =
        appState.windows[pluginName]?.result?.firstOrNull() as? AutomationState
            ?: AutomationState()

    private fun createDefaultStep(stepType: ScriptStepType): ScriptStep =
        when (stepType) {
            ScriptStepType.INSTALL_APK -> ScriptStep.InstallApk(apkPath = "")
            ScriptStepType.REMOVE_PACKAGE -> ScriptStep.RemovePackage(packageName = "")
            ScriptStepType.START_ACTIVITY -> ScriptStep.StartActivity(packageName = "", activityName = "")
            ScriptStepType.CLEAR_DATA -> ScriptStep.ClearData(packageName = "")
        }
}