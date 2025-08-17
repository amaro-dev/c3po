package plugins.automation.structure

import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IProcessor

fun IProcessor<AppState>.deliver(pluginName: String, state: AutomationState) {
    reduce(Action.DeliverPluginResult(pluginName, listOf(state)))
}

fun updateScriptStep(script: Script, stepIndex: Int, newStep: ScriptStep): Script {
    val updatedSteps = script.steps.toMutableList()
    updatedSteps[stepIndex] = newStep
    return script.copy(steps = updatedSteps)
}

fun closeAllDialogs(state: AutomationState): AutomationState =
    state.copy(
        editingStepIndex = null,
        showPackageSelector = false,
        showActivitySelector = false,
        showApkPicker = false
    )