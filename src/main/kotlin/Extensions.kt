import Settings.ACCEPT_COMPANION
import androidx.compose.ui.graphics.Color
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

fun Color.darkenedBy(amount: Float): Color {
    if (amount > 1 || amount < 0) throw IllegalArgumentException()
    return copy(
        red = red * (1f - amount),
        green = green * (1f - amount),
        blue = blue * (1f - amount),
    )
}

fun debug(message: String) {
//    println("[DEBUG] $message")
}
