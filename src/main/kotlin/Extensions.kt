import core.Action
import core.AppState
import dev.amaro.sonic.IProcessor

fun Boolean.ifTrue(value: String): String = if (this) value else ""

fun <T> Result<T>.handle(processor: IProcessor<AppState>): T? {
    return if (isSuccess) {
        getOrNull()
    } else {
        processor.reduce(Action.SetCommandError(exceptionOrNull()?.message ?: "Unknown error"))
        null
    }
}
