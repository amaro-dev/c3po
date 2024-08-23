import commands.CommandResult
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

object CommandRunner {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run(command: String): CommandResult {
//        println("Command: '$command'")
        return withContext(Dispatchers.IO) {
            val process = ProcessBuilder().command(command.split(' ')).start()
            val response = async { process.inputReader().readText().trim() }
            val error = async { process.errorReader().readText().trim() }
            val exited = process.waitFor(10L, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                response.cancel()
                error.cancel()
                throw InterruptedException("Command execution timeout!")
            }
            response.join()
            error.join()
            CommandResult(
                response.getCompleted(),
                process.exitValue(),
                error.getCompleted().takeIf { it.isNotEmpty() }
            )
        }
    }
}
