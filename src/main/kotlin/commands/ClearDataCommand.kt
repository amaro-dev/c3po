package commands

import models.AdbDevice
import models.AppPackage

class ClearDataCommand(device:AdbDevice, appPackage: AppPackage): AdbCommand<Unit>{
    override val command: String = "adb -s ${device.id} shell pm clear ${appPackage.packageName}"

    override fun parse(result: CommandResult) = Unit
}