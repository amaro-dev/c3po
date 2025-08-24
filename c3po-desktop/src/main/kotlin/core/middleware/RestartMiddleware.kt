package core.middleware

import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlin.system.exitProcess

class RestartMiddleware : IMiddleware<AppState> {

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.RestartApplication -> {
                handleRestartApplication()
            }
        }
    }

    private fun handleRestartApplication() {
        // Launch restart in a separate thread to avoid blocking the middleware
        Thread {
            try {
                Thread.sleep(1000) // Give UI time to show completion message

                // Get current JAR path
                val jarPath = System.getProperty("java.class.path")

                // Create restart command
                val javaCommand = System.getProperty("java.home") + "/bin/java"
                val command = listOf(javaCommand, "-jar", jarPath)

                // Start new instance
                ProcessBuilder(command)
                    .directory(java.io.File(System.getProperty("user.dir")))
                    .start()

                // Exit current instance
                exitProcess(0)

            } catch (e: Exception) {
                // Fallback: just exit and let user manually restart
                println("Failed to restart automatically: ${e.message}")
                println("Please restart the application manually.")
                exitProcess(0)
            }
        }.start()
    }
}