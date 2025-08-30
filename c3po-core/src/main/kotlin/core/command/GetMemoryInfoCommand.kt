package core.command

class GetMemoryInfoCommand : AdbCommand<String> {
    override val command: String = "shell cat /proc/meminfo"

    override fun parse(result: String): String {
        val lines = result.lines()
        val memTotal = lines.find { it.startsWith("MemTotal:") }
            ?.substringAfter("MemTotal:")?.trim()?.substringBefore(" kB")?.trim()?.toIntOrNull()

        return memTotal?.let { kb ->
            when {
                kb >= 1024 * 1024 -> String.format("%.1f GB", kb / (1024.0 * 1024.0))
                kb >= 1024 -> String.format("%.0f MB", kb / 1024.0)
                else -> "$kb kB"
            }
        } ?: "Unknown"
    }
}