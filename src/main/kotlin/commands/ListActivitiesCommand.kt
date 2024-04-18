package commands

import models.AdbDevice

class ListActivitiesCommand(device: AdbDevice) : AdbCommand<List<String>> {
    override val command: String = "adb -s ${device.id} shell dumpsys package"

    override fun parse(result: CommandResult): List<String> {
        val lineSplitRule = Regex("\\r?\\n")
        var content = result.content.trim()
        content = content.substring(content.indexOf("Activity Resolver Table"))
        content = content.substring(content.indexOf("Non-Data Actions:"))
        content = content.substring(content.indexOf("android.intent.action.MAIN:"))
        val removePrefix = Regex("\\s+\\w{5,}\\s")
        return lineSplitRule.split(content)
            .drop(1)
            .takeWhile { it.startsWith("        ") }
            .map { removePrefix.replace(it, "") }
            .sorted()
    }
}
