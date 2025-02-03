package commands

class DeviceInfoCommand : AdbCommand<Map<String, String>> {
    override val command: String = "shell getprop"

    override fun parse(result: String): Map<String, String> {
        val lineSplitRule = Regex("\\r?\\n")
        return lineSplitRule
            .split(result)
            .map { it.trim().subSequence(1, it.length - 1) }
            .map { it.split("]: [") }
            .filter { it.size == 2 }
            .associate { Pair(it[0], it[1]) }
    }
}
