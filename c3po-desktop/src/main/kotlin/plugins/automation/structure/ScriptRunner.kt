package plugins.automation.structure

import Settings
import core.command.CheckAppInstalledCommand
import core.command.ClearDataCommand
import core.command.CommandExecutor
import core.command.InstallApkCommand
import core.command.StartActivityCommand
import core.command.StopAppCommand
import core.command.UninstallAppCommand
import core.logging.StructuredLogger
import core.model.Action
import core.model.ActivityInfo
import core.model.AppPackage
import core.model.AppState
import dev.amaro.sonic.IProcessor

class ScriptRunner(
    private val pluginName: String,
) {
    private val logger = StructuredLogger.getInstance()
    suspend fun run(
        script: Script,
        scriptFolder: String,
        state: AppState,
        processor: IProcessor<AppState>,
        executor: CommandExecutor,
    ) {
        var currentState = (state.windows[pluginName]?.result?.firstOrNull() as? AutomationState) ?: AutomationState()
        fun update(block: (AutomationState) -> AutomationState) {
            currentState = block(currentState)
            processor.reduce(Action.DeliverPluginResult(pluginName, listOf(currentState)))
        }

        update {
            it.copy(
                isRunning = true,
                runningStepIndex = 0,
                runLogs = emptyList(),
                failedStepIndex = -1,
                completedSteps = emptySet()
            )
        }

        logger.log("automation", "script_start", "Started script: ${script.name}")

        script.steps.forEachIndexed { index, step ->
            update {
                it.copy(runningStepIndex = index)
            }

            logger.log("automation", "step_start", "Step ${index + 1}: ${step.type}")

            val result = when (step) {
                is ScriptStep.InstallApk -> runInstall(step, scriptFolder, state, executor)
                is ScriptStep.RemovePackage -> runUninstall(step, state, executor)
                is ScriptStep.ClearData -> runClearData(step, state, executor)
                is ScriptStep.StartActivity -> runStartActivity(step, state, executor)
                is ScriptStep.StopPackage -> runStopPackage(step, state, executor)
            }

            if (result.isFailure) {
                val message = result.exceptionOrNull()?.message ?: "Unknown error"
                logger.log("automation", "step_failed", "Step ${index + 1} failed: $message")
                update {
                    it.copy(
                        isRunning = false,
                        failedStepIndex = index,
                        runLogs = it.runLogs + "Step ${index + 1} failed: $message"
                    )
                }
                return
            } else {
                logger.log("automation", "step_completed", "Step ${index + 1} completed successfully")
                update { it.copy(completedSteps = it.completedSteps + index) }
            }
        }

        logger.log("automation", "script_completed", "Script completed successfully")
        update { it.copy(isRunning = false, runningStepIndex = -1) }

        // Trigger global success message
        processor.reduce(Action.SetSuccess("Script '${script.name}' executed successfully"))
    }

    private suspend fun runInstall(
        step: ScriptStep.InstallApk,
        scriptFolder: String,
        state: AppState,
        executor: CommandExecutor,
    ): Result<Unit> {
        val apkPath = java.io.File(scriptFolder, step.apkPath.removePrefix("./")).absolutePath
        if (!java.io.File(apkPath).exists()) {
            return Result.failure(IllegalArgumentException("APK not found: ${step.apkPath}"))
        }
        val cmd = InstallApkCommand("-r $apkPath")
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { }
    }

    private suspend fun runUninstall(
        step: ScriptStep.RemovePackage,
        state: AppState,
        executor: CommandExecutor,
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
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { }
    }

    private suspend fun runClearData(
        step: ScriptStep.ClearData,
        state: AppState,
        executor: CommandExecutor,
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
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { }
    }

    private suspend fun runStartActivity(
        step: ScriptStep.StartActivity,
        state: AppState,
        executor: CommandExecutor,
    ): Result<Unit> {
        val info = ActivityInfo(step.packageName, step.activityName)
        val cmd = StartActivityCommand(info, forDebug = false)
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { }
    }

    private suspend fun runStopPackage(
        step: ScriptStep.StopPackage,
        state: AppState,
        executor: CommandExecutor,
    ): Result<Unit> {
        val cmd = StopAppCommand(AppPackage(step.packageName))
        return executor.go(cmd, state.settings.getProperty(Settings.ADB_PATH_PROP), state.currentDevice).map { }
    }
}