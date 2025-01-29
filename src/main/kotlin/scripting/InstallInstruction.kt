package scripting

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name

class InstallInstruction(tempArgs: List<String>) : Instruction {

    override val resources: List<Path> = tempArgs.map { Path(it) }

    override val args: List<String> = resources.map { it.name }

    companion object {
        const val TOKEN = "install"
    }

    override val token: String = TOKEN

    override fun validate(path: Path) {
        if (args.isEmpty())
            throw MissingArgumentException()

        if (!File(path.toString(), args[0]).exists())
            throw ResourceNotFoundException(args[0], path.toString())
    }
}
