package core

import Settings
import commands.CheckAppInstalledCommand
import commands.CheckPortForwardCommand
import commands.CheckServiceRunningCommand
import commands.CommandExecutor
import commands.ForwardPortCommand
import commands.InstallApkCommand
import commands.StartServiceCommand
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import models.ActivityInfo
import models.AdbDevice
import java.io.File

class CompanionMiddleware(
    private val resourcesPath: File,
    private val executor: CommandExecutor
) : AsyncMiddlewareBase<AppState>(CoroutineScope(Dispatchers.IO)) {
    companion object {
        private const val SERVICE_NAME = "CompanionService"
        private const val PACKAGE_NAME = "dev.amaro.c3po.companion"
    }


    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            is Action.InstallCompanion -> {
                state.currentDevice?.let { device ->
                    executor.go(
                        InstallApkCommand("${resourcesPath.absolutePath}/R2D2.apk"),
                        adbPath,
                        device
                    )
                    processor.perform(Action.CheckForCompanion)
                }
            }

            is Action.SkipCompanionForDevice -> {
                processor.reduce(Action.UpdateCompanionState(state.companionState.setSkipped()))
            }

            is Action.UpdatedState -> {
//                if (!action.old.companionState.isReady() && action.new.companionState.isReady()) {
//                    processor.perform(Action.ConnectCompanion)
//                } else if (!action.new.companionState.isPre())
            }

            is Action.CheckForCompanion -> {
                state.currentDevice?.let { device ->
                    var status = CompanionState()
                    val isInstalled =
                        executor.go(CheckAppInstalledCommand(PACKAGE_NAME),  adbPath, device)
                    if (isInstalled.isSuccess) {
                        status = status.setIsInstalled().setHasAccepted()
                        if (checkCompanionServiceIsRunning(adbPath, device))
                            status = status.setIsRunning()
                        if (checkAdbPortsAreConfigured( adbPath, device))
                            status = status.setPortIsOpen()
                    }

                    if (status.isReady() && !status.isOnline()) {
                        processor.reduce(Action.UpdateCompanionState(status))

                    } else if (!status.isReady() && !status.hasPrepared()) {
                        processor.reduce(Action.UpdateCompanionState(status.setPrepared()))

                    } else {
                        processor.reduce(Action.UpdateCompanionState(status))
                    }
                }
            }

            is Action.PrepareCompanion -> {
                if (state.companionState.isOnline()) return
                enforceCompanionServiceIsRunning(state,  adbPath)
                enforceAdbPortsAreConfigured(state,  adbPath)
                processor.perform(Action.CheckForCompanion)
            }
            is Action.DeliverSocketResponse -> {
                println(action)
            }
        }
    }

    private suspend fun checkAdbPortsAreConfigured(
        adbPath: String,
        device: AdbDevice,
    ): Boolean =
        executor.go(
            CheckPortForwardCommand("9500"),
            adbPath,
            device,
        ).let { it.getOrNull() == true }

    private suspend fun checkCompanionServiceIsRunning(
        adbPath: String,
        device: AdbDevice,
    ): Boolean =
        executor.go(
            CheckServiceRunningCommand("$PACKAGE_NAME.$SERVICE_NAME"),
            adbPath,
            device,
        ).let { it.getOrNull() == true }

    private suspend fun enforceAdbPortsAreConfigured(
        state: AppState,
        adbPath: String,
    ) {
        if (!state.companionState.isPortOpen()) {
            executor.go(ForwardPortCommand("9500"), adbPath, state.currentDevice)
        }
    }

    private suspend fun enforceCompanionServiceIsRunning(
        state: AppState,
        adbPath: String,
    ) {
        if (!state.companionState.isRunning()) {
            executor.go(
                StartServiceCommand(ActivityInfo(PACKAGE_NAME, "$PACKAGE_NAME.$SERVICE_NAME")),
                adbPath,
                state.currentDevice,
            )
        }
    }
}
