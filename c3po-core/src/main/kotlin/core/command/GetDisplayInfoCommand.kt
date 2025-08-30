package core.command

import core.model.DisplayInfo

class GetDisplayInfoCommand : AdbCommand<DisplayInfo> {
    override val command: String = "shell wm size"

    override fun parse(result: String): DisplayInfo {
        val size = result.lines()
            .find { it.contains("Physical size:") }
            ?.substringAfter("Physical size:")?.trim() ?: "Unknown"

        // For density, we'll need to get it separately or from props
        // For now, return a basic display info
        return DisplayInfo(size, "Unknown DPI")
    }
}