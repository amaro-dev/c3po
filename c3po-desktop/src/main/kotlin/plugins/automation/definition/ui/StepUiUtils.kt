package plugins.automation.definition.ui

import plugins.automation.structure.ScriptStep

// Step execution state for visual progress
data class StepExecutionState(
    val isScriptRunning: Boolean,
    val currentStepIndex: Int,
    val failedStepIndex: Int,
    val completedSteps: Set<Int>,
)

enum class StepStatus {
    PENDING,  // Not yet executed
    RUNNING,  // Currently executing
    SUCCESS,  // Completed successfully
    FAILED    // Failed during execution
}

fun getStepStatus(stepIndex: Int, executionState: StepExecutionState): StepStatus =
    when {
        executionState.failedStepIndex == stepIndex -> StepStatus.FAILED
        stepIndex in executionState.completedSteps -> StepStatus.SUCCESS
        stepIndex == executionState.currentStepIndex && executionState.isScriptRunning -> StepStatus.RUNNING
        else -> StepStatus.PENDING
    }

fun getStepDisplayName(step: ScriptStep): String =
    when (step) {
        is ScriptStep.InstallApk -> "Install APK"
        is ScriptStep.RemovePackage -> "Remove Package"
        is ScriptStep.StartActivity -> "Start Activity"
        is ScriptStep.ClearData -> "Clear Data"
        is ScriptStep.StopPackage -> "Stop Package"
    }

fun getStepDescription(step: ScriptStep): String =
    when (step) {
        is ScriptStep.InstallApk -> if (step.apkPath.isNotBlank()) "APK: ${step.apkPath}" else ""
        is ScriptStep.RemovePackage -> if (step.packageName.isNotBlank()) "Package: ${step.packageName}" else ""
        is ScriptStep.StartActivity -> if (step.packageName.isNotBlank() && step.activityName.isNotBlank()) {
            "Package: ${step.packageName}, Activity: ${step.activityName}"
        } else ""

        is ScriptStep.ClearData -> if (step.packageName.isNotBlank()) "Package: ${step.packageName}" else ""
        is ScriptStep.StopPackage -> if (step.packageName.isNotBlank()) "Package: ${step.packageName}" else ""
    }

