package commands

import models.AppPackage

class UninstallAppCommand(
    appPackage: AppPackage,
) : AdbCommand<ParsedResult> {
    override val command: String = "uninstall ${appPackage.packageName}"

    override fun parse(result: CommandResult) =
        when (result.content) {
            "Success" -> Success
            else -> Error(result.content)
        }
}
