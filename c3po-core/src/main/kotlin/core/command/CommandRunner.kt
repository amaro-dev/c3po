package core.command

import core.debug
import core.model.DeviceNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object CommandRunner {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run(
        adbPath: File,
        args: Array<String>,
    ): Result<String> {
        debug("Command: (PATH: $adbPath) '${args.joinToString(" ")}'")
        return withContext(Dispatchers.IO) {
            debug("Will start command")
            val process =
                ProcessBuilder()
                    .command(*args)
//                .directory(adbPath)
                    .start()
            debug("Command started")
            val response = async { process.inputReader().readText().trim() }
            val error = async { process.errorReader().readText().trim() }
            val exited = process.waitFor(60L, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                response.cancel()
                error.cancel()
                debug("Command timed out!")
                Result.failure(TimeoutException())
            } else {
                response.join()
                error.join()
                val exitCode = process.exitValue()
                val errorMessage = error.getCompleted().takeIf { it.isNotEmpty() } ?: response.getCompleted()
                val content = response.getCompleted()
                debug("Command exit ($exitCode): $errorMessage")
                debug("Content: $content")
                if (exitCode != 0) {
                    if (deviceNotFoundMessage.containsMatchIn(errorMessage)) {
                        Result.failure(DeviceNotFoundException())
                    } else {
                        Result.failure(Exception("Code: $exitCode - $errorMessage"))
                    }
                } else {
                    Result.success(content)
                }
            }
        }
    }

    private val deviceNotFoundMessage = Regex("device .* not found")
}
