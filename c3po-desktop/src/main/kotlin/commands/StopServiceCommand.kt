package commands

import models.ActivityInfo
import models.AdbDevice

class StopServiceCommand(
    packageName: ActivityInfo,
    adbDevice: AdbDevice,
) : AdbCommand<Unit> {
    override val command: String = "shell am force-stop ${packageName.packageName}"

    override fun parse(result: String) = Unit
}
