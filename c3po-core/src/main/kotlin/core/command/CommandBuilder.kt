package core.command

import core.model.AdbDevice

object CommandBuilder {
    fun build(
        command: AdbCommand<*>,
        adbPath: String,
        device: AdbDevice?,
    ): Array<String> {
        val deviceDirective = device?.id?.let { " -s $it" } ?: ""
        return if (command is PlaceholderAdb) {
            command.command
                .trim()
                .replace(Regex("\\[ADB\\]"), "${adbPath.trim()}$deviceDirective")
                .split("¡")
                .toTypedArray()
        } else {
            "$adbPath$deviceDirective ${command.command.trim()}".split(" ").toTypedArray()
        }
    }
}
