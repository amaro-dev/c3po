package scripting

import java.nio.file.Path

interface Instruction {
    val args: List<String>
    val token: String
    val resources: List<Path>
    fun validate(path: Path) = Unit
}
