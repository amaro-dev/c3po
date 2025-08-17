package core.facade

import core.command.CheckAppInstalledCommand
import core.command.CommandExecutor
import core.command.ForwardPortCommand
import core.command.InstallApkCommand
import core.command.StartServiceCommand
import core.command.StopServiceCommand
import core.model.ActivityInfo
import core.model.AdbDevice
import kotlinx.coroutines.delay
import java.io.File

interface CompanionCommander {
    suspend fun isInstalled(
        adbPath: String,
        device: AdbDevice,
    ): Result<Boolean>

    suspend fun prepare(
        adbPath: String,
        device: AdbDevice,
    ): Result<Unit>

    suspend fun installAndPrepare(
        adbPath: String,
        device: AdbDevice,
    ): Result<Unit>
}

class CompanionCommanderImpl(
    private val executor: CommandExecutor,
    private val resourcesPath: File,
) : CompanionCommander {
    companion object {
        private const val SERVICE_NAME = "CompanionService"
        private const val PACKAGE_NAME = "dev.amaro.c3po.companion"
    }

    private suspend fun install(
        adbPath: String,
        device: AdbDevice,
    ): Result<Boolean> =
        executor.go(
            InstallApkCommand("${resourcesPath.absolutePath}/R2D2.apk"),
            adbPath,
            device,
        )

    override suspend fun isInstalled(
        adbPath: String,
        device: AdbDevice,
    ): Result<Boolean> = executor.go(CheckAppInstalledCommand(PACKAGE_NAME), adbPath, device)

    private suspend fun stopService(
        adbPath: String,
        device: AdbDevice,
    ): Result<Unit> =
        executor.go(
            StopServiceCommand(ActivityInfo(PACKAGE_NAME, "$PACKAGE_NAME.$SERVICE_NAME"), device),
            adbPath,
            device,
        )

    private suspend fun startService(
        adbPath: String,
        device: AdbDevice,
    ): Result<Unit> =
        executor.go(
            StartServiceCommand(ActivityInfo(PACKAGE_NAME, "$PACKAGE_NAME.$SERVICE_NAME"), device),
            adbPath,
            device,
        )

    private suspend fun openPorts(
        adbPath: String,
        device: AdbDevice,
    ): Result<Unit> = executor.go(ForwardPortCommand("9500"), adbPath, device)

    override suspend fun prepare(
        adbPath: String,
        device: AdbDevice,
    ): Result<Unit> {
        stopService(adbPath, device)
        startService(adbPath, device).onFailure {
            return Result.failure(IllegalStateException("Could not start the Companion App service: $it", it))
        }
        delay(2000)
        openPorts(adbPath, device).onFailure {
            return Result.failure(IllegalStateException("Could not open ADB ports to establish connection"))
        }
        return Result.success(Unit)
    }

    override suspend fun installAndPrepare(
        adbPath: String,
        device: AdbDevice,
    ): Result<Unit> {
        install(adbPath, device).onFailure {
            return Result.failure(IllegalStateException("Could not install"))
        }
        return prepare(adbPath, device)
    }
}
