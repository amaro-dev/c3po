package scripting

import java.nio.file.Path

class ClearInstruction(override val args: List<String>) : Instruction {
    companion object {
        const val TOKEN = "clear"
    }

    override val token: String = TOKEN

    override val resources: List<Path> = emptyList()
}
