package core.command

import core.model.AppPackage

class ClearDataCommand(
    appPackage: AppPackage,
) : AdbCommand<Unit> {
    override val command: String = "shell pm clear ${appPackage.packageName}"

    override fun parse(result: String) = Unit
}
