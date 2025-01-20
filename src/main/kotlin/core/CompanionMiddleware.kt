package core

import Settings
import commands.CheckAppInstalledCommand
import commands.CheckPortForwardCommand
import commands.CheckServiceRunningCommand
import commands.CommandExecutor
import commands.ForwardPortCommand
import commands.StartServiceCommand
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.ActivityInfo
import models.AdbDevice

class CompanionMiddleware(
    private val executor: CommandExecutor,
) : IMiddleware<AppState> {
    companion object {
        private const val SERVICE_NAME = "CompanionService"
        private const val PACKAGE_NAME = "dev.amaro.c3po.companion"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            is Action.CheckForCompanion -> {
                state.currentDevice?.let { device ->
                    var status = CompanionState()
                    scope.launch {
                        val isInstalled =
                            executor.go(CheckAppInstalledCommand(PACKAGE_NAME), adbPath, device)
                        if (isInstalled.isSuccess && isInstalled.getOrNull() == true) {
                            status = status.setIsInstalled()
                            if (checkCompanionServiceIsRunning(adbPath, device)) {
                                status = status.setIsRunning()
                            }
                            if (checkAdbPortsAreConfigured(adbPath, device)) {
                                status = status.setPortIsOpen()
                            }
                        }
                        processor.reduce(Action.UpdateCompanionState(status))
                    }
                }
            }

            is Action.PrepareCompanion -> {
                if (state.companionState.isOnline()) return
                scope.launch {
                    enforceCompanionServiceIsRunning(state, adbPath)
                    enforceAdbPortsAreConfigured(state, adbPath)
                    processor.perform(Action.CheckForCompanion)
                }
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
