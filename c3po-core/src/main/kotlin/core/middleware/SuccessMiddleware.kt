package core.middleware

import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SuccessMiddleware(
    private val scope: CoroutineScope,
) : IMiddleware<AppState> {

    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>
    ) {
        when (action) {
            is Action.SetSuccess -> {
                // Auto-dismiss after 4 seconds
                scope.launch {
                    delay(4000)
                    // Always clear after timeout (simple implementation)
                    processor.reduce(Action.ClearSuccess)
                }
            }
        }
    }
}