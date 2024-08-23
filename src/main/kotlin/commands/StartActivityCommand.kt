package commands

import models.ActivityInfo

class StartActivityCommand(activityInfo: ActivityInfo) : AdbCommand<Unit> {
    override val command: String = "shell am start -n ${activityInfo.fullPath.replace("\$", "\\\$")}"

    override fun parse(result: CommandResult) = Unit

}
