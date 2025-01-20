import commands.CommandResult
import core.debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object CommandRunner {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run(command: String): CommandResult {
//        println("Command: '$command'")
        return withContext(Dispatchers.IO) {
            debug("Will start command")
            val process = ProcessBuilder().command(command.split(' ')).start()
            debug("Command started")
            val response = async { process.inputReader().readText().trim() }
            val error = async { process.errorReader().readText().trim() }
            val exited = process.waitFor(15L, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                response.cancel()
                error.cancel()
                debug("Command timed out!")
                throw InterruptedException("Command execution timeout!")
            }
            response.join()
            error.join()
            val exitCode = process.exitValue()
            val errorMessage = error.getCompleted()
            val content = response.getCompleted()
            debug("Command exit ($exitCode): $errorMessage")
            debug("Content: $content")
            CommandResult(
                content,
                exitCode,
                errorMessage.takeIf { it.isNotEmpty() },
            )
        }
    }
}
