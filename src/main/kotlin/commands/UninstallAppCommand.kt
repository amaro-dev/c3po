package commands

import models.AdbDevice
import models.AppPackage

class UninstallAppCommand(
    device: AdbDevice,
    appPackage: AppPackage
) : AdbCommand<ParsedResult> {
    override val command: String = "-s ${device.id} uninstall ${appPackage.packageName}"

    override fun parse(result: CommandResult) = when (result.content) {
        "Success" -> Success
        else -> Error(result.content)
    }
}
