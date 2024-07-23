package ui.plugins

import core.AppState
import dev.amaro.sonic.IMiddleware

abstract class PluginMiddleware(private val name: String): IMiddleware<AppState>
