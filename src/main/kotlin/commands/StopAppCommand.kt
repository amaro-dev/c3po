package commands

import models.AdbDevice
import models.AppPackage

class StopAppCommand(
    device: AdbDevice,
    appPackage: AppPackage
): AdbCommand<Unit> {
    override val command: String = "adb -s ${device.id} shell am force-stop ${appPackage.packageName}"

    override fun parse(result: CommandResult) = Unit
}
