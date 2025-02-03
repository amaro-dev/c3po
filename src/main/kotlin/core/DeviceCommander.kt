package core

import Settings.NAME_SYSTEM_PROP
import Settings.SDK_LEVEL_PROP
import commands.CommandExecutor
import commands.DeviceInfoCommand
import commands.ListDevicesCommand
import exceptionOrUnknownError
import models.AdbDevice

interface DeviceCommander {
    suspend fun listDevices(adbPath: String): Result<List<AdbDevice>>
}

class DeviceCommanderImpl(
    private val executor: CommandExecutor
) : DeviceCommander {
    override suspend fun listDevices(adbPath: String): Result<List<AdbDevice>> {
        val devices = executor.go(ListDevicesCommand, adbPath, null)
        return if (devices.isSuccess) {
            Result.success(devices.getOrDefault(emptyList()).mapNotNull {
                executor.go(DeviceInfoCommand(), adbPath, it)
                    .getOrNull()
                    ?.run {
                        AdbDevice(
                            it.id,
                            filterKeys { it in arrayOf(NAME_SYSTEM_PROP, SDK_LEVEL_PROP) }
                        )
                    }
            })
        } else {
            Result.failure(devices.exceptionOrUnknownError())
        }
    }

}
