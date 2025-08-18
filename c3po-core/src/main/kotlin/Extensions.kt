import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IProcessor

fun Boolean.ifTrue(value: String): String = if (this) value else ""


fun <T> Result<T>.exceptionOrUnknownError(): Throwable = exceptionOrNull() ?: UnknownError()

fun <T> Result<T>.handle(
    processor: IProcessor<AppState>,
    onSuccess: (T) -> Unit = {},
): T? =
    if (isSuccess) {
        processor.reduce(Action.SetCommandCompleted)
        val output = getOrNull()
        if (output != null) onSuccess(output)
        output
    } else {
        println(exceptionOrUnknownError())
        processor.reduce(Action.SetCommandError(exceptionOrUnknownError().message ?: "Unknown error"))
        null
    }

fun debug(message: String) {
    println("[DEBUG] $message")
}

