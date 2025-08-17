package core.command

import core.model.ActivityInfo
import ifTrue

class StartActivityCommand(
    activityInfo: ActivityInfo,
    forDebug: Boolean = false,
) : AdbCommand<Unit> {
    override val command: String =
        "shell am start ${forDebug.ifTrue("-D ")}-n ${activityInfo.fullPath.replace("\$", "\\\$")}"

    override fun parse(result: String) = Unit
}
