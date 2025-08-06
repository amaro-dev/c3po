package plugins.automation

import Settings
import commands.CommandExecutor
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import models.Script
import models.ScriptStep
import models.ScriptStepType
import plugins.PluginMiddleware
import java.io.File

class AutomationMiddleware(
    pluginName: String,
    private val scriptStorage: ScriptStorage,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {

    // Debounce mechanism for EditStep actions
    private var lastEditStepTime = 0L
    private var lastEditStepIndex = -1
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.StartPlugin -> {
                // Only initialize if we don't have state already
                val currentState = getCurrentState(state)
                if (currentState == AutomationState()) {
                    // Load initial state
                    val initialState = AutomationState()
                    processor.reduce(Action.DeliverPluginResult(pluginName, listOf(initialState)))

                    // Preload packages and activities in the background
                    execute(commands.ListPackagesCommand(), state, executor)
                        .handle(processor) { packages ->
                            val updatedState = getCurrentState(state).copy(availablePackages = packages)
                            processor.reduce(Action.DeliverPluginResult(pluginName, listOf(updatedState)))
                        }

                    // Also preload activities
                    execute(commands.ListActivitiesCommand(), state, executor)
                        .handle(processor) { activities ->
                            val updatedState = getCurrentState(state).copy(availableActivities = activities)
                            processor.reduce(Action.DeliverPluginResult(pluginName, listOf(updatedState)))
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
                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.SetScriptName -> {
                val currentState = getCurrentState(state)
                val updatedScript = currentState.currentScript?.copy(name = action.name)
                val newState = currentState.copy(currentScript = updatedScript)
                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.AddStep -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return

                val newStep = createDefaultStep(action.stepType)
                val updatedSteps = currentScript.steps + newStep
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(currentScript = updatedScript)

                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.RemoveStep -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return

                val updatedSteps = currentScript.steps.filterIndexed { index, _ -> index != action.index }
                val updatedScript = currentScript.copy(steps = updatedSteps)
                val newState = currentState.copy(currentScript = updatedScript)

                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.SaveScript -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return

                if (currentScript.name.isBlank()) {
                    // TODO: Show error
                    return
                }

                try {
                    scriptStorage.saveScript(currentScript)

                    // Reset state after successful save
                    val newState =
                        currentState.copy(
                            isCreatingScript = false,
                            currentScript = null,
                        )
                    processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))

                    // TODO: Show success message
                } catch (e: Exception) {
                    // TODO: Show error message
                    println("Error saving script: ${e.message}")
                }
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

            is AutomationPlugin.Actions.EditStep -> {
                val currentTime = System.currentTimeMillis()
                val stepIndex = action.index

                // Debounce mechanism: ignore if same step clicked within 1 second
                if (stepIndex == lastEditStepIndex && currentTime - lastEditStepTime < 1000) {
                    return
                }

                lastEditStepTime = currentTime
                lastEditStepIndex = stepIndex

                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val step = currentScript.steps.getOrNull(stepIndex) ?: return

                val newState = when (step) {
                    is ScriptStep.InstallApk -> {
                        currentState.copy(
                            editingStepIndex = stepIndex,
                            showApkPicker = true
                        )
                    }

                    is ScriptStep.RemovePackage, is ScriptStep.ClearData -> {
                        currentState.copy(
                            editingStepIndex = stepIndex,
                            showPackageSelector = true
                        )
                    }

                    is ScriptStep.StartActivity -> {
                        currentState.copy(
                            editingStepIndex = stepIndex,
                            showActivitySelector = true
                        )
                    }
                }

                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.CancelStepEdit -> {
                val currentState = getCurrentState(state)
                val newState = currentState.copy(
                    editingStepIndex = null,
                    showPackageSelector = false,
                    showActivitySelector = false,
                    showApkPicker = false
                )
                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.ConfigureInstallApk -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val stepIndex = action.index

                // Copy APK file to script folder and get relative path
                val relativePath = scriptStorage.copyApkToScriptFolder(action.apkPath, currentScript.name)

                val updatedStep = ScriptStep.InstallApk(apkPath = relativePath)
                val updatedSteps = currentScript.steps.toMutableList().apply {
                    set(stepIndex, updatedStep)
                }
                val updatedScript = currentScript.copy(steps = updatedSteps)

                val newState = currentState.copy(
                    currentScript = updatedScript,
                    editingStepIndex = null,
                    showApkPicker = false
                )

                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.ConfigureRemovePackage -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val stepIndex = action.index

                val updatedStep = ScriptStep.RemovePackage(packageName = action.packageName)
                val updatedSteps = currentScript.steps.toMutableList().apply {
                    set(stepIndex, updatedStep)
                }
                val updatedScript = currentScript.copy(steps = updatedSteps)

                val newState = currentState.copy(
                    currentScript = updatedScript,
                    editingStepIndex = null,
                    showPackageSelector = false
                )

                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.ConfigureStartActivity -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val stepIndex = action.index

                val updatedStep = ScriptStep.StartActivity(
                    packageName = action.packageName,
                    activityName = action.activityName
                )
                val updatedSteps = currentScript.steps.toMutableList().apply {
                    set(stepIndex, updatedStep)
                }
                val updatedScript = currentScript.copy(steps = updatedSteps)

                val newState = currentState.copy(
                    currentScript = updatedScript,
                    editingStepIndex = null,
                    showActivitySelector = false
                )

                processor.reduce(Action.DeliverPluginResult(pluginName, listOf(newState)))
            }

            is AutomationPlugin.Actions.ConfigureClearData -> {
                val currentState = getCurrentState(state)
                val currentScript = currentState.currentScript ?: return
                val stepIndex = action.index

                val updatedStep = ScriptStep.ClearData(packageName = action.packageName)
                val updatedSteps = currentScript.steps.toMutableList().apply {
                    set(stepIndex, updatedStep)
                }
                val updatedScript = currentScript.copy(steps = updatedSteps)

                val newState = currentState.copy(
                    currentScript = updatedScript,
                    editingStepIndex = null,
                    showPackageSelector = false
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

/**
 * Handles script storage operations
 */
class ScriptStorage {
    private val scriptsFolder: File by lazy {
        val settingsFolder =
            if (Settings.isDebug()) {
                File(".")
            } else {
                File(Settings.productionSettingsFolder())
            }
        File(settingsFolder, "scripts").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    fun saveScript(script: Script) {
        val scriptFolder =
            File(scriptsFolder, script.name).apply {
                if (!exists()) {
                    mkdirs()
                }
            }

        val scriptFile = File(scriptFolder, "script.c3po")
        val yamlContent = generateYaml(script)

        scriptFile.writeText(yamlContent)
    }

    private fun generateYaml(script: Script): String {
        val sb = StringBuilder()
        sb.appendLine("name: \"${script.name}\"")
        sb.appendLine("version: \"${script.version}\"")
        sb.appendLine("format_version: \"${script.formatVersion}\"")
        sb.appendLine("steps:")

        script.steps.forEach { step ->
            sb.appendLine("  - type: \"${step.type}\"")
            when (step) {
                is ScriptStep.InstallApk -> {
                    sb.appendLine("    apk_path: \"${step.apkPath}\"")
                }

                is ScriptStep.RemovePackage -> {
                    sb.appendLine("    package_name: \"${step.packageName}\"")
                }

                is ScriptStep.StartActivity -> {
                    sb.appendLine("    package_name: \"${step.packageName}\"")
                    sb.appendLine("    activity_name: \"${step.activityName}\"")
                }

                is ScriptStep.ClearData -> {
                    sb.appendLine("    package_name: \"${step.packageName}\"")
                }
            }
        }

        return sb.toString()
    }

    fun copyApkToScriptFolder(apkPath: String, scriptName: String): String {
        val sourceFile = File(apkPath)
        if (!sourceFile.exists()) {
            throw IllegalArgumentException("APK file does not exist: $apkPath")
        }

        val scriptFolder = File(scriptsFolder, scriptName)
        if (!scriptFolder.exists()) {
            scriptFolder.mkdirs()
        }

        val fileName = sourceFile.name
        val targetFile = File(scriptFolder, fileName)

        // Copy file
        sourceFile.copyTo(targetFile, overwrite = true)

        // Return relative path
        return "./$fileName"
    }
}
