package core.command

class GetDensityInfoCommand : AdbCommand<String> {
    override val command: String = "shell wm density"

    override fun parse(result: String): String {
        return result.lines()
            .find { it.contains("Physical density:") }
            ?.substringAfter("Physical density:")?.trim()?.let { "$it DPI" } ?: "Unknown DPI"
    }
}