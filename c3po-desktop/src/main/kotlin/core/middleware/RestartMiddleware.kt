package core.middleware

import core.logging.StructuredLogger
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlin.system.exitProcess

class RestartMiddleware : AsyncMiddlewareBase<AppState>() {

    private val logger = StructuredLogger.getInstance()

    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.RestartApplication -> {
                logger.log("restart", "RequestUserRestart", "Attempting to restart application")
                attemptRestart(processor)
            }
        }
    }

    private fun attemptRestart(processor: IProcessor<AppState>) {
        try {
            val userDir = System.getProperty("user.dir")
            logger.log("restart", "Debug", "Current working directory: $userDir")

            // For development: restart gradle task
            // For production: open app bundle
            val restartCommand = if (userDir?.contains("c3po") == true) {
                // Development mode - restart gradle
                val projectRoot = if (userDir.endsWith("c3po-desktop")) {
                    userDir.substringBeforeLast("/c3po-desktop")
                } else userDir

                "cd '$projectRoot/c3po-desktop' && ../gradlew run"
            } else {
                // Production mode - open app bundle
                "open -n /Applications/c3po.app"
            }

            ProcessBuilder("bash", "-c", restartCommand).start()
            exitProcess(0)

        } catch (e: Exception) {
            processor.reduce(Action.UpdateError("Installation complete. Please restart the application manually."))
            logger.log("restart", "Failed", "Restart failed: ${e.message}")
        }
    }
}