package plugins.automation

import Settings
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import models.Script
import models.ScriptStep
import models.ScriptStepType
import plugins.PluginMiddleware
import java.io.File

class AutomationMiddleware(
    pluginName: String,
    private val scriptStorage: ScriptStorage,
) : PluginMiddleware(pluginName) {
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
}
