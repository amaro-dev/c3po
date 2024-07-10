import commands.CommandResult
import java.io.InputStreamReader

object CommandRunner {
    fun run(command: String): CommandResult {
        //println("Command: '$command'")
        val process = ProcessBuilder().command(command.split(' ')).start()
        return CommandResult(InputStreamReader(process.inputStream.buffered()).readText().trim())
    }
}
