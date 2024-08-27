package commands

import models.ActivityInfo
import ui.ifTrue

class StartActivityCommand(activityInfo: ActivityInfo, forDebug: Boolean = false) : AdbCommand<Unit> {
    override val command: String = "shell am start ${forDebug.ifTrue("-D ")}-n ${activityInfo.fullPath.replace("\$", "\\\$")}"

    override fun parse(result: CommandResult) = Unit

}
