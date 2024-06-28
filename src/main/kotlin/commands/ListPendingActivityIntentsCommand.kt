package commands

import models.AdbDevice
import models.IntentInfo
import models.PendingIntent

class ListPendingActivityIntentsCommand(device: AdbDevice) : AdbCommand<List<PendingIntent>> {
    override val command: String = "-s ${device.id} shell dumpsys activity intents"

    private val idPattern = Regex("PendingIntentRecord\\{([a-f0-9]{5,8})\\s")
    private val keyValuePattern = Regex(" ([^\\s,=]*)=([^\\s,]+)")

    override fun parse(result: CommandResult): List<PendingIntent> {
        val lineSplitRule = Regex("\\r?\\n")
        var content = result.content.trim()
        content = content.substring(content.indexOf("ACTIVITY MANAGER PENDING INTENTS (dumpsys activity intents)"))
        val removePrefix = Regex("\\s+\\*\\s")
        return lineSplitRule.split(content)
            .drop(1)
            .takeWhile { it.startsWith("  ") }
            .joinToString()
            .let { removePrefix.replaceFirst(it, "") }
            .let { Regex("\\s{2,}").replace(it, " ") }
            .split(" * ")
            .map {
                buildMap {
                    put("key", idPattern.find(it)?.groups?.get(1)?.value)
                    put("hasExtras", it.contains("(has extras)").toString())
                    keyValuePattern.findAll(it).forEach {
                        put(it.groups[1]!!.value, it.groups[2]!!.value)
                    }
                }
            }
            .map {
                PendingIntent(
                    it["key"] ?: "",
                    it["packageName"] ?: "",
                    (it["flags"] ?: "0x0").removePrefix("0x").toLong(16),
                    it["type"] ?: "",
                    IntentInfo(
                        it["requestIntent"] ?: "",
                        it["hasExtras"]?.toBoolean() ?: false,
                        it["flg"]?.removePrefix("0x")?.toLong(16),
                        it["pkg"],
                        it["dat"],
                        it["cmp"]
                    ),

                    it["requestCode"]?.toInt()
                )
            }
    }
}
