package core.middleware

import core.logging.StructuredLogger
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import java.io.File
import java.net.URLDecoder
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
            val appBundle = detectCurrentMacAppBundle()

            if (appBundle == null) {
                // Development environment detected: do not auto-restart
                logger.log(
                    "restart",
                    "RestartSkippedInDev",
                    "Packaged app bundle not detected; skipping restart in dev"
                )
                processor.reduce(Action.SetSuccess("Instalação concluída. Reinicie o C3PO manualmente."))
                return
            }

            // Production: relaunch the same bundle
            val restartCommand = arrayOf("/usr/bin/open", "-n", appBundle.absolutePath)
            logger.log("restart", "RestartCommand", "${restartCommand.joinToString(" ")}")

            val process = ProcessBuilder(*restartCommand)
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            logger.log("restart", "RestartResult", "exitCode=$exitCode")

            if (exitCode == 0) {
                exitProcess(0)
            } else {
                processor.reduce(Action.UpdateError("Instalação concluída. Reinicie o C3PO manualmente."))
            }

        } catch (e: Exception) {
            processor.reduce(Action.UpdateError("Instalação concluída. Reinicie o C3PO manualmente."))
            logger.log("restart", "Failed", "Restart failed: ${e.message}")
        }
    }

    private fun detectCurrentMacAppBundle(): File? {
        return try {
            val url = this::class.java.protectionDomain.codeSource.location
            val decoded = URLDecoder.decode(url.path, Charsets.UTF_8.name())
            var current = File(decoded)
            var steps = 0
            while (current.parentFile != null && steps < 12) {
                if (current.name.endsWith(".app")) return current
                current = current.parentFile
                steps++
            }
            null
        } catch (e: Exception) {
            logger.log("restart", "BundleDetectError", "${e.message}")
            null
        }
    }
}
