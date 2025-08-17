package core.command

import core.model.ActivityInfo
import core.model.AdbDevice

class StartServiceCommand(
    packageName: ActivityInfo,
    adbDevice: AdbDevice,
) : AdbCommand<Unit> {
    private val instruction = if (adbDevice.sdk >= 26) "start-foreground-service" else "startservice"

    override val command: String = "shell am $instruction ${packageName.fullPath}"

    override fun parse(result: String) = Unit
}
