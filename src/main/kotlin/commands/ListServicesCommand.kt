package commands

import models.ActivityInfo
import models.AdbDevice

class ListServicesCommand(device: AdbDevice) : AdbCommand<Map<String, List<ActivityInfo>>> {
    override val command: String = "-s ${device.id} shell dumpsys package"

    override fun parse(result: CommandResult): Map<String, List<ActivityInfo>> {
        val lineSplitRule = Regex("\\r?\\n")
        var content = result.content.trim()
        content = content.substring(content.indexOf("Service Resolver Table"))
        content = content.substring(content.indexOf("Non-Data Actions:"))
        val removePrefix = Regex("\\s+\\w{5,}\\s")
        var lastKey: String? = null
        return lineSplitRule.split(content)
            .drop(1)
            .takeWhile { it.startsWith("      ") }
            .foldRight<String, MutableMap<String, List<ActivityInfo>>>(mutableMapOf()) { item, acc ->
                if (item.startsWith("        ")) {
                    if (lastKey != null)
                        acc[lastKey!!] = acc[lastKey]!!.plus(removePrefix.replace(item, "").toActivityInfo())
                } else if (item.startsWith("      ")) {
                    lastKey = removePrefix.replace(item, "").removePrefix("      ")
                    acc[lastKey!!] = listOf()
                }
                acc
            }
            .toSortedMap()
    }

    private fun String.toActivityInfo(): ActivityInfo {
        val (pkg, activity) = split('/')
        return ActivityInfo(pkg, activity)
    }
}
