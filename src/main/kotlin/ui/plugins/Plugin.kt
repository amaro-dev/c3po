package ui.plugins

import androidx.compose.runtime.Composable
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware


interface Plugin<T> {
    // As a good practice actions should be declared in here

    val name: String

    val mainAction: IAction

    val middleware: IMiddleware<AppState>

    fun isResponsibleFor(action: IAction): Boolean

    @Composable
    fun present(state: Map<String, Any>, onAction: (IAction) -> Unit) {
        val data: T? = state[name] as? T
        data?.run { present(this, onAction) }
    }

    @Composable
    fun present(items: T, onAction: (IAction) -> Unit)
}
