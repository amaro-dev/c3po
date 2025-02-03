package core

import commands.CheckAppInstalledCommand
import commands.CommandExecutor
import commands.ForwardPortCommand
import commands.InstallApkCommand
import commands.StartServiceCommand
import commands.StopServiceCommand
import kotlinx.coroutines.delay
import models.ActivityInfo
import models.AdbDevice
import java.io.File

interface CompanionCommander {

    suspend fun isInstalled(adbPath: String, device: AdbDevice): Result<Boolean>

    suspend fun prepare(adbPath: String, device: AdbDevice): Result<Unit>

    suspend fun installAndPrepare(adbPath: String, device: AdbDevice): Result<Unit>
}

class CompanionCommanderImpl(
    private val executor: CommandExecutor,
    private val resourcesPath: File
) : CompanionCommander {
    companion object {
        private const val SERVICE_NAME = "CompanionService"
        private const val PACKAGE_NAME = "dev.amaro.c3po.companion"
    }

    private suspend fun install(adbPath: String, device: AdbDevice): Result<Boolean> {
        return executor.go(
            InstallApkCommand("${resourcesPath.absolutePath}/R2D2.apk"),
            adbPath,
            device
        )
    }

    override suspend fun isInstalled(adbPath: String, device: AdbDevice): Result<Boolean> {
        return executor.go(CheckAppInstalledCommand(PACKAGE_NAME), adbPath, device)
    }

    private suspend fun stopService(adbPath: String, device: AdbDevice): Result<Unit> {
        return executor.go(
            StopServiceCommand(ActivityInfo(PACKAGE_NAME, "$PACKAGE_NAME.$SERVICE_NAME"), device),
            adbPath,
            device
        )
    }

    private suspend fun startService(adbPath: String, device: AdbDevice): Result<Unit> {
        return executor.go(
            StartServiceCommand(ActivityInfo(PACKAGE_NAME, "$PACKAGE_NAME.$SERVICE_NAME"), device),
            adbPath,
            device,
        )
    }

    private suspend fun openPorts(adbPath: String, device: AdbDevice): Result<Unit> {
        return executor.go(ForwardPortCommand("9500"), adbPath, device)
    }

    override suspend fun prepare(adbPath: String, device: AdbDevice): Result<Unit> {
        println("Device SDK ${device.sdk}")
        stopService(adbPath, device).onFailure {
            println("Could not stop the Companion App service")
//            return Result.failure(IllegalStateException("Could not stop the Companion App service: $it"))
        }.onSuccess {
            println(it)
        }
        startService(adbPath, device).onFailure {
            println("Could not start the Companion App service")
            return Result.failure(IllegalStateException("Could not start the Companion App service: $it", it))
        }.onSuccess {
            println(it)
        }
        delay(2000)
        openPorts(adbPath, device).onFailure {
            println("Could not open ADB ports to establish connection")
            return Result.failure(IllegalStateException("Could not open ADB ports to establish connection"))
        }.onSuccess {
            println(it)
        }
        return Result.success(Unit)
    }

    override suspend fun installAndPrepare(adbPath: String, device: AdbDevice): Result<Unit> {
        install(adbPath, device).onFailure {
            println("Install failed $it")
            return Result.failure(IllegalStateException("Could not install"))
        }.onSuccess {
            println("Install succeeded $it")
        }
        return prepare(adbPath, device)
    }
}
