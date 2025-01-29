package scripting

import java.nio.file.Path

class UninstallInstruction(override val args: List<String>) : Instruction {
    companion object {
        const val TOKEN = "uninstall"
    }

    override val resources: List<Path> = emptyList()

    override val token: String = TOKEN
}
