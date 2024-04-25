package commands

import models.ActivityInfo
import models.AdbDevice

class StartActivityCommand(
    device: AdbDevice,
    activityInfo: ActivityInfo
) : AdbCommand<Unit> {
    override val command: String = "adb -s ${device.id} shell am start -n ${activityInfo.fullPath}"

    override fun parse(result: CommandResult) {

    }

}
