package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DebugMiddleware(
    private val waitFor: Long = 3000,
    vararg actions: IAction,
) : IMiddleware<AppState> {
    private val actionList = actions
    private lateinit var job: Job

    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        if (::job.isInitialized) return
        job =
            CoroutineScope(Dispatchers.Default).launch {
                var i = 0
                while (true) {
                    processor.reduce(actionList[i])
                    delay(waitFor)
                    i = ++i % actionList.size
                }
            }
    }
}
