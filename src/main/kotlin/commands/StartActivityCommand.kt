package commands

import models.AdbDevice

class StartActivityCommand(
    device: AdbDevice,
    activityInfo: String
) : AdbCommand<Unit> {
    override val command: String = "adb -s ${device.id} shell am start -n $activityInfo"

    override fun parse(result: CommandResult) {

    }

}
