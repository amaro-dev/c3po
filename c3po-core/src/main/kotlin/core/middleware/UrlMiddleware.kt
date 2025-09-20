package core.middleware

import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import java.awt.Desktop
import java.net.URI

class UrlMiddleware : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.OpenUrl -> {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(URI(action.url))
                    } else {
                        processor.reduce(Action.SetCommandError("Opening URLs is not supported on this platform."))
                    }
                } catch (e: Exception) {
                    processor.reduce(Action.SetCommandError("Failed to open link: ${e.message ?: "unknown error"}"))
                }
            }

            else -> {}
        }
    }
}

