package commands

import models.ActivityInfo

class StartServiceCommand(packageName: ActivityInfo) : AdbCommand<Boolean> {
    override val command: String = "shell am startservice ${packageName.fullPath}"

    override fun parse(result: CommandResult): Boolean = result.error == null
}
