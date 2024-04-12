package commands

import models.AdbDevice

class StopAppCommand(
    device: AdbDevice,
    packageName: String
): AdbCommand<Unit> {
    override val command: String = "adb -s ${device.id} shell am force-stop $packageName"

    override fun parse(result: CommandResult) = Unit
}