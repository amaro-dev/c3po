package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IReducer
import dev.amaro.sonic.ISideEffectAction
import dev.amaro.sonic.StateManager

class AppStateManager(
    vararg middleware: IMiddleware<AppState>,
) : StateManager<AppState>(AppState(), middleware.toList()) {
    override val reducer: IReducer<AppState> = AppReducer()

    override fun reduce(action: IAction) {
        println("$action called by: ${Thread.currentThread().stackTrace[2].className}")
        val oldState = state.value
        state.value = reducer.reduce(action, state.value)
        if (action is ISideEffectAction) {
            perform(action.sideEffect)
        }
        perform(Action.UpdatedState(oldState, state.value))
    }
}
