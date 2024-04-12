package commands

import models.AdbDevice

class UninstallAppCommand(
    device: AdbDevice,
    packageName: String
):AdbCommand<Unit> {
    override val command: String = "adb -s ${device.id} uninstall $packageName"

    override fun parse(result: CommandResult) = Unit
}