package core.command

class DeviceInfoCommand : AdbCommand<List<Pair<String, String>>> {
    override val command: String = "shell getprop"

    override fun parse(result: String): List<Pair<String, String>> {
        val lineSplitRule = Regex("\\r?\\n")
        return lineSplitRule
            .split(result)
            .map { it.trim().subSequence(1, it.length - 1) }
            .map { it.split("]: [") }
            .filter { it.size == 2 }
            .associate { Pair(it[0], it[1]) }
            .toList()
            .sortedBy { it.first }
    }
}
