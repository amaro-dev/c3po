package commands

import models.AppPackage

class StopAppCommand(appPackage: AppPackage) : AdbCommand<Unit> {
    override val command: String = "shell am force-stop ${appPackage.packageName}"

    override fun parse(result: CommandResult) = Unit
}
