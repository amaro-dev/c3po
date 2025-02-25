package plugins

import androidx.compose.runtime.Composable
import core.AppState
import core.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import ui.OnAction

interface Plugin<in T> {
    // As a good practice actions should be declared in here

    val id: String

    val name: String

    val middleware: IMiddleware<AppState>

    fun isResponsibleFor(action: IAction): Boolean

    @Composable
    fun render(
        state: Map<String, WindowResult<*>>,
        onAction: OnAction,
    ) {
        state[id]?.run { present(this as WindowResult<T>, onAction) }
    }

    @Composable
    fun present(
        result: WindowResult<T>,
        onAction: OnAction,
    )
}
