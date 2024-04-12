package core

import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IReducer
import dev.amaro.sonic.StateManager

class AppStateManager(vararg middleware: IMiddleware<AppState>) : StateManager<AppState>(AppState(), middleware.toList()) {
    override val reducer: IReducer<AppState> = AppReducer()
}