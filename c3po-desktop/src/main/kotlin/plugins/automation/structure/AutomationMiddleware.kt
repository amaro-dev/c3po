package plugins.automation.structure

import core.command.CommandExecutor
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
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
            is AutomationPlugin.Actions.OpenNameDialog -> {
                val current = getCurrentState(state)
                val updated = current.copy(
                    showNameDialog = true,
                    isRenameDialog = action.isRename
                )
                processor.deliver(pluginName, updated)
            }

            is AutomationPlugin.Actions.CloseNameDialog -> {
                val current = getCurrentState(state)
                processor.deliver(
                    pluginName,
                    current.copy(showNameDialog = false, isRenameDialog = false)
                )
            }

            is AutomationPlugin.Actions.ConfirmScriptName -> {
                val current = getCurrentState(state)
                val name = action.name.trim()
                if (name.isBlank()) {
                    // ignore blank names, just close dialog
                    processor.deliver(pluginName, current.copy(showNameDialog = false, isRenameDialog = false))
                    return
                }

                if (current.isRenameDialog && current.currentScript != null) {
                    val oldName = current.currentScript.name
                    // If name didn't change, just close dialog
                    if (oldName == name) {
                        processor.deliver(pluginName, current.copy(showNameDialog = false, isRenameDialog = false))
                        return
                    }

                    // If we have a folder already, rename the folder to avoid duplicates
                    try {
                        if (current.currentScriptFolder != null) {
                            val newFolder = scriptStorage.renameScriptFolder(oldName, name)
                            val updated = current.copy(
                                currentScript = current.currentScript.copy(name = name),
                                currentScriptFolder = newFolder.absolutePath,
                                showNameDialog = false,
                                isRenameDialog = false,
                                isDirty = true,
                            )
                            processor.deliver(pluginName, updated)
                        } else {
                            // No folder yet (unsaved). Just update name in memory
                            val updated = current.copy(
                                currentScript = current.currentScript.copy(name = name),
                                showNameDialog = false,
                                isRenameDialog = false,
                                isDirty = true,
                            )
                            processor.deliver(pluginName, updated)
                        }
                    } catch (e: Exception) {
                        // Keep dialog open and report error
                        processor.reduce(Action.SetCommandError("Failed to rename script: ${e.message}"))
                        val keepDialog = current.copy(showNameDialog = true, isRenameDialog = true)
                        processor.deliver(pluginName, keepDialog)
                    }
                } else {
                    val newState = current.copy(
                        isCreatingScript = true,
                        currentScript = Script(name = name, version = "1.0", formatVersion = "1.0"),
                        showNameDialog = false,
                        isRenameDialog = false
                    )
                    processor.deliver(pluginName, newState)
                }
            }

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
                            isDirty = false,
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
                        // Keep script open: isCreatingScript remains true, currentScript preserved
                        currentScriptFolder = scriptFolder,
                        isDirty = false,
                    )
                    processor.deliver(pluginName, newState)

                    // Show success message for script save
                    processor.reduce(Action.SetSuccess("Script '${currentScript.name}' saved successfully"))
                } catch (e: Exception) {
                    // Show error message for script save failure
                    processor.reduce(Action.SetCommandError("Failed to save script: ${e.message}"))
                }
            }

            is AutomationPlugin.Actions.RunScript -> {
                val current = getCurrentState(state)
                val folder = current.currentScriptFolder ?: return
                if (!state.hasDeviceSet) return

                try {
                    // Load script from folder if not currently loaded
                    val script = current.currentScript ?: scriptStorage.loadScriptFromFolder(folder)
                    runner.run(script, folder, state, processor, executor)
                } catch (e: Exception) {
                    // Handle script loading error
                    val newState = current.copy(
                        openScriptError = "Cannot run script: ${e.message}"
                    )
                    processor.deliver(pluginName, newState)
                }
            }

            is Action.StartPlugin -> {
                val currentState = getCurrentState(state)

                // Always reset execution state when plugin starts, but preserve other state
                val resetState = currentState.copy(
                    isRunning = false,
                    runningStepIndex = -1,
                    failedStepIndex = -1,
                    completedSteps = emptySet(),
                    runLogs = emptyList(),
                    // Clear any open dialogs
                    editingStepIndex = null,
                    showPackageSelector = false,
                    showActivitySelector = false,
                    showApkPicker = false,
                    showOpenScriptPicker = false
                )

                // Load initial data only if we don't have packages and activities already
                if (currentState.availablePackages.isEmpty() && currentState.availableActivities.isEmpty()) {
                    processor.deliver(pluginName, resetState)

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
                } else {
                    // If we already have packages/activities, just deliver the reset state
                    processor.deliver(pluginName, resetState)
                }
            }

            is AutomationPlugin.Actions.CreateNewScript -> {
                val currentState = getCurrentState(state)
                val newState =
                    currentState.copy(
                        isCreatingScript = true,
                        currentScript = Script(name = "", version = "1.0", formatVersion = "1.0"),
                        isDirty = true,
                    )
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.SetScriptName -> {
                val currentState = getCurrentState(state)
                val updatedScript = currentState.currentScript?.copy(name = action.name)
                val newState = currentState.copy(currentScript = updatedScript, isDirty = true)
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.AddStep -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return

                val newStep = createDefaultStep(action.stepType)
                val updatedSteps = currentScript.steps + newStep
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(currentScript = updatedScript, isDirty = true)

                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.RemoveStep -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return

                val updatedSteps = currentScript.steps.filterIndexed { index, _ -> index != action.index }
                val updatedScript = currentScript.copy(steps = updatedSteps)

                // Adjust execution state indices after step removal
                val stateWithAdjustedIndices = adjustExecutionStateAfterStepRemoval(currentState, action.index)
                val newState = stateWithAdjustedIndices.copy(currentScript = updatedScript, isDirty = true)
                
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.EditStep -> {
                // Debounce mechanism to prevent multiple rapid clicks
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastEditStepTime < 500 && lastEditStepIndex == action.index) {
                    return
                }
                lastEditStepTime = currentTime
                lastEditStepIndex = action.index

                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val step = currentScript.steps.getOrNull(action.index) ?: return

                // Determine which dialog to show based on step type
                val newState = when (step) {
                    is ScriptStep.InstallApk -> {
                        currentState.copy(
                            editingStepIndex = action.index,
                            showApkPicker = true
                        )
                    }

                    is ScriptStep.RemovePackage, is ScriptStep.ClearData, is ScriptStep.StopPackage -> {
                        currentState.copy(
                            editingStepIndex = action.index,
                            showPackageSelector = true
                        )
                    }

                    is ScriptStep.StartActivity -> {
                        currentState.copy(
                            editingStepIndex = action.index,
                            showActivitySelector = true
                        )
                    }
                }

                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.CancelStepEdit -> {
                val currentState = getCurrentState(state)
                val newState = currentState.copy(
                    editingStepIndex = null,
                    showPackageSelector = false,
                    showActivitySelector = false,
                    showApkPicker = false,
                    showOpenScriptPicker = false
                )
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.ConfigureInstallApk -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return

                try {
                    // Copy APK to script folder and get relative path
                    val relativePath = scriptStorage.copyApkToScriptFolder(action.apkPath, currentScript.name)

                    val updatedSteps = currentScript.steps.mapIndexed { index, step ->
                        if (index == action.index) {
                            ScriptStep.InstallApk(apkPath = relativePath)
                        } else {
                            step
                        }
                    }
                    val updatedScript = currentScript.copy(steps = updatedSteps)
                    val newState = currentState.copy(
                        currentScript = updatedScript,
                        editingStepIndex = null,
                        showApkPicker = false,
                        isDirty = true,
                    )
                    processor.deliver(pluginName, newState)

                    // Show success message for APK copy
                    processor.reduce(Action.SetSuccess("APK copied to script folder successfully"))
                } catch (e: Exception) {
                    // Show error if copying fails
                    processor.reduce(Action.SetCommandError("Failed to copy APK: ${e.message}"))
                }
            }

            is AutomationPlugin.Actions.ConfigureRemovePackage -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val updatedSteps = currentScript.steps.mapIndexed { index, step ->
                    if (index == action.index) {
                        ScriptStep.RemovePackage(packageName = action.packageName)
                    } else {
                        step
                    }
                }
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(
                    currentScript = updatedScript,
                    editingStepIndex = null,
                    showPackageSelector = false,
                    isDirty = true,
                )
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.ConfigureStartActivity -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val updatedSteps = currentScript.steps.mapIndexed { index, step ->
                    if (index == action.index) {
                        ScriptStep.StartActivity(packageName = action.packageName, activityName = action.activityName)
                    } else {
                        step
                    }
                }
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(
                    currentScript = updatedScript,
                    editingStepIndex = null,
                    showActivitySelector = false,
                    isDirty = true,
                )
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.ConfigureClearData -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val updatedSteps = currentScript.steps.mapIndexed { index, step ->
                    if (index == action.index) {
                        ScriptStep.ClearData(packageName = action.packageName)
                    } else {
                        step
                    }
                }
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(
                    currentScript = updatedScript,
                    editingStepIndex = null,
                    showPackageSelector = false,
                    isDirty = true,
                )
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.ConfigureStopPackage -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val updatedSteps = currentScript.steps.mapIndexed { index, step ->
                    if (index == action.index) {
                        ScriptStep.StopPackage(packageName = action.packageName)
                    } else {
                        step
                    }
                }
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(
                    currentScript = updatedScript,
                    editingStepIndex = null,
                    showPackageSelector = false,
                    isDirty = true,
                )
                processor.deliver(pluginName, newState)
            }

            is AutomationPlugin.Actions.CancelScript -> {
                val currentState = getCurrentState(state)
                val newState =
                    currentState.copy(
                        isCreatingScript = false,
                        currentScript = null,
                        isDirty = false,
                    )
                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.LoadScripts -> {
                val currentState = getCurrentState(state)
                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(currentState)))
            }
        }
    }

    private fun getCurrentState(appState: AppState): AutomationState =
        appState.windows[pluginName]?.result?.firstOrNull() as? AutomationState
            ?: AutomationState()

    /**
     * Adjusts execution state indices after a step is removed.
     * Handles proper adjustment of runningStepIndex, failedStepIndex, and completedSteps.
     */
    private fun adjustExecutionStateAfterStepRemoval(
        currentState: AutomationState,
        removedStepIndex: Int
    ): AutomationState {
        val adjustedRunningStepIndex = when {
            currentState.runningStepIndex > removedStepIndex -> currentState.runningStepIndex - 1
            else -> currentState.runningStepIndex
        }

        val adjustedFailedStepIndex = when {
            currentState.failedStepIndex == removedStepIndex -> -1 // Clear failure if deleted step was the failed one
            currentState.failedStepIndex > removedStepIndex -> currentState.failedStepIndex - 1
            else -> currentState.failedStepIndex
        }

        val adjustedCompletedSteps = currentState.completedSteps
            .filter { it != removedStepIndex } // Remove the deleted step if it was completed
            .map { completedIndex ->
                if (completedIndex > removedStepIndex) {
                    completedIndex - 1 // Decrement indices that come after the deleted step
                } else {
                    completedIndex
                }
            }
            .toSet()

        return currentState.copy(
            runningStepIndex = adjustedRunningStepIndex,
            failedStepIndex = adjustedFailedStepIndex,
            completedSteps = adjustedCompletedSteps
        )
    }

    private fun createDefaultStep(stepType: ScriptStepType): ScriptStep =
        when (stepType) {
            ScriptStepType.INSTALL_APK -> ScriptStep.InstallApk(apkPath = "")
            ScriptStepType.REMOVE_PACKAGE -> ScriptStep.RemovePackage(packageName = "")
            ScriptStepType.START_ACTIVITY -> ScriptStep.StartActivity(packageName = "", activityName = "")
            ScriptStepType.CLEAR_DATA -> ScriptStep.ClearData(packageName = "")
            ScriptStepType.STOP_PACKAGE -> ScriptStep.StopPackage(packageName = "")
        }
}
