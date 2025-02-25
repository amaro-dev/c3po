package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class StatusMiddleware(
    private val scope: CoroutineScope
) : IMiddleware<AppState> {

    private val isRunning = AtomicBoolean(false)

    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>
    ) {
        if (state.commandStatus.isResult()) {
            if (!isRunning.getAndSet(true)) {
                scope.launch {
                    delay(3000)
                    processor.reduce(Action.ClearError)
                    isRunning.set(false)
                }
            }
        }
    }
}
