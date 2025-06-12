import Settings.ACCEPT_COMPANION
import core.Action
import core.AppState
import dev.amaro.sonic.IProcessor
import java.util.Properties

fun Boolean.ifTrue(value: String): String = if (this) value else ""

fun Any?.toBool(): Boolean = this?.let { it == "true" } ?: false

fun Properties.hasSelectedCompanionOption() = this[ACCEPT_COMPANION] != null

fun <T> Result<T>.exceptionOrUnknownError(): Throwable {
    return exceptionOrNull() ?: UnknownError()
}

fun <T> Result<T>.handle(processor: IProcessor<AppState>, onSuccess: (T) -> Unit = {}): T? {
    return if (isSuccess) {
        processor.reduce(Action.SetCommandCompleted)
        val output = getOrNull()
        if (output != null) onSuccess(output)
        output
    } else {
        println(exceptionOrUnknownError())
        processor.reduce(Action.SetCommandError(exceptionOrUnknownError().message ?: "Unknown error"))
        null
    }
}

inline fun <R> R.transformIf(condition: Boolean, block: (R) -> R): R {
    return if (condition) block(this) else this
}

fun debug(message: String) {
    //println("[DEBUG] $message")
}

inline fun <reified T> Collection<T>.update(condition: (T) -> Boolean, change: (T) -> T): List<T> {
    return map { if (condition(it)) change(it) else it }
}

inline fun <reified T, reified R> Map<T, R>.update(key: T, transform: (R) -> R): Map<T, R> {
    return if (containsKey(key)) plus(Pair(key, transform(this[key]!!))) else this
}
