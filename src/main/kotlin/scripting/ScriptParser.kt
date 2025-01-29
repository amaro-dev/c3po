package scripting

import java.nio.file.Path

class ScriptParser(
    private val identifier: InstructionIdentifier
) {
    fun parse(content: List<String>, path: Path) {
        content.map {
            identifier.detect(it)
        }.forEach {
            it.validate(path)
        }
    }
}

typealias InstructionFactory = (args: List<String>) -> Instruction
