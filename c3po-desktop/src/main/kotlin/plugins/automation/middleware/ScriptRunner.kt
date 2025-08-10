package plugins.automation.middleware

import Settings
import commands.CheckAppInstalledCommand
import commands.ClearDataCommand
import commands.InstallApkCommand
import commands.StartActivityCommand
import commands.UninstallAppCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IProcessor
import models.ActivityInfo
import models.AppPackage
import plugins.automation.data.AutomationState
import plugins.automation.data.Script
import plugins.automation.data.ScriptStep

class ScriptRunner(
    private val pluginName: String,
) {
    suspend fun run(
        script: Script,
        scriptFolder: String,
        state: AppState,
        processor: IProcessor<AppState>,
        executor: commands.CommandExecutor,
    ) {
        var currentState = (state.windows[pluginName]?.result?.firstOrNull() as? AutomationState) ?: AutomationState()
        fun update(block: (AutomationState) -> AutomationState) {
            currentState = block(currentState)
            processor.reduce(Action.DeliverPluginResult(pluginName, listOf(currentState)))
        }

        update { it.copy(isRunning = true, runningStepIndex = 0, runLogs = emptyList()) }

        script.steps.forEachIndexed { index, step ->
            update {
                it.copy(
                    runningStepIndex = index,
                    runLogs = it.runLogs + "Running step ${index + 1}: ${step.type}"
                )
            }

            val result = when (step) {
                is ScriptStep.InstallApk -> runInstall(step, scriptFolder, state, executor)
                is ScriptStep.RemovePackage -> runUninstall(step, state, executor).also {
                    if (it.isSuccess) update { s -> s.copy(runLogs = s.runLogs + "Uninstall checked/executed for ${step.packageName}") }
                }

                is ScriptStep.ClearData -> runClearData(step, state, executor).also {
                    if (it.isSuccess) update { s -> s.copy(runLogs = s.runLogs + "Clear data checked/executed for ${step.packageName}") }
                }

                is ScriptStep.StartActivity -> runStartActivity(step, state, executor)
            }

            if (result.isFailure) {
                val message = result.exceptionOrNull()?.message ?: "Unknown error"
                update { it.copy(isRunning = false, runLogs = it.runLogs + "Error: $message") }
                return
            } else {
                update { it.copy(runLogs = it.runLogs + "Step ${index + 1} completed") }
            }
        }

        update { it.copy(isRunning = false, runningStepIndex = -1, runLogs = it.runLogs + "Run completed") }
    }

    private suspend fun runInstall(
        step: ScriptStep.InstallApk,
        scriptFolder: String,
        state: AppState,
        executor: commands.CommandExecutor,
    ): Result<Unit> {
        val apkPath = java.io.File(scriptFolder, step.apkPath.removePrefix("./")).absolutePath
        if (!java.io.File(apkPath)
                .exists()
        ) return Result.failure(IllegalArgumentException("APK not found: ${step.apkPath}"))
        val cmd = InstallApkCommand("-r $apkPath")
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { Unit }
    }

    private suspend fun runUninstall(
        step: ScriptStep.RemovePackage,
        state: AppState,
        executor: commands.CommandExecutor,
    ): Result<Unit> {
        val installed = executor.go(
            CheckAppInstalledCommand(step.packageName),
            state.settings.getProperty(Settings.ADB_PATH_PROP),
            state.currentDevice
        ).getOrDefault(false)
        if (!installed) {
            return Result.success(Unit) // warn via logs handled by caller
        }
        val cmd = UninstallAppCommand(AppPackage(step.packageName))
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { Unit }
    }

    private suspend fun runClearData(
        step: ScriptStep.ClearData,
        state: AppState,
        executor: commands.CommandExecutor,
    ): Result<Unit> {
        val installed = executor.go(
            CheckAppInstalledCommand(step.packageName),
            state.settings.getProperty(Settings.ADB_PATH_PROP),
            state.currentDevice
        ).getOrDefault(false)
        if (!installed) {
            return Result.success(Unit)
        }
        val cmd = ClearDataCommand(AppPackage(step.packageName))
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { Unit }
    }

    private suspend fun runStartActivity(
        step: ScriptStep.StartActivity,
        state: AppState,
        executor: commands.CommandExecutor,
    ): Result<Unit> {
        val info = ActivityInfo(step.packageName, step.activityName)
        val cmd = StartActivityCommand(info, forDebug = false)
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { Unit }
    }
}
