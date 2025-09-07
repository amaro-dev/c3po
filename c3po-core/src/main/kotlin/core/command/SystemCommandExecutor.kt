package core.command

import core.debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Executor for generic system commands (non-ADB).
 * Uses CommandRunner directly without ADB-specific logic.
 */
class SystemCommandExecutor {

    suspend fun executeCommand(command: String, workingDir: File? = null): Result<String> {
        debug("System command: $command")
        return withContext(Dispatchers.IO) {
            try {
                CommandRunner.run(
                    adbPath = workingDir ?: File(System.getProperty("user.dir")),
                    args = arrayOf("/bin/bash", "-c", command)
                )
            } catch (e: Exception) {
                debug("System command failed: ${e.message}")
                Result.failure(e)
            }
        }
    }
}