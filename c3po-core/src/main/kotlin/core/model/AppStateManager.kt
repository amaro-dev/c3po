package core.model

import core.debug
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IReducer
import dev.amaro.sonic.StateManager
import java.util.concurrent.ConcurrentLinkedQueue

class AppStateManager(
    initialState: AppState,
    mainReducer: IReducer<AppState>,
    vararg middleware: IMiddleware<AppState>,
) : StateManager<AppState>(initialState, middleware.toList()),
    IActionScheduler {
    override val reducer: IReducer<AppState> = mainReducer

    override fun reduce(action: IAction) {
        debug("REDUCE: $action")
        val oldState = state.value
        state.value = reducer.reduce(action, state.value)
        perform(Action.UpdatedState(oldState, state.value))
        while (scheduledActions.isNotEmpty()) {
            perform(scheduledActions.poll())
        }
    }

    override fun perform(action: IAction) {
        debug("PERFORM: $action")
        super.perform(action)
    }

    private val scheduledActions = ConcurrentLinkedQueue<IAction>()

    override fun schedule(action: IAction) {
        debug("SCHEDULE: $action")
        scheduledActions.add(action)
    }
}
