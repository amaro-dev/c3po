package commands

import models.AdbDevice
import models.AppPackage

class UninstallAppCommand(
    device: AdbDevice,
    appPackage: AppPackage
):AdbCommand<Unit> {
    override val command: String = "adb -s ${device.id} uninstall ${appPackage.packageName}"

    override fun parse(result: CommandResult) = Unit
}
