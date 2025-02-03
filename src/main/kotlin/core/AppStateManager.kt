package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IReducer
import dev.amaro.sonic.ISideEffectAction
import dev.amaro.sonic.StateManager
import java.util.concurrent.ConcurrentLinkedQueue

class AppStateManager(
    vararg middleware: IMiddleware<AppState>,
) : StateManager<AppState>(AppState(), middleware.toList()), IActionScheduler {
    override val reducer: IReducer<AppState> = AppReducer()

    override fun reduce(action: IAction) {
//        println("$action called by: ${Thread.currentThread().stackTrace[2].className}")
        val oldState = state.value
        state.value = reducer.reduce(action, state.value)
        if (action is ISideEffectAction) {
            perform(action.sideEffect)
        }
        perform(Action.UpdatedState(oldState, state.value))
        while (scheduledActions.isNotEmpty()) {
            perform(scheduledActions.poll())
        }
    }

    override fun perform(action: IAction) {
        println("Perform: $action")
        super.perform(action)
    }

    private val scheduledActions = ConcurrentLinkedQueue<IAction>()

    override fun schedule(action: IAction) {
        println("Scheduled: $action")
        scheduledActions.add(action)
    }
}

interface IActionScheduler {
    fun schedule(action: IAction)
}
