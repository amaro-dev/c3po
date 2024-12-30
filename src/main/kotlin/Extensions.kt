import Settings.ACCEPT_COMPANION
import java.util.Properties

import core.Action
import core.AppState
import dev.amaro.sonic.IProcessor

fun Boolean.ifTrue(value: String): String = if (this) value else ""

fun Any?.toBool(): Boolean = this?.let { it == "true" } ?: false

fun Properties.hasSelectedCompanionOption() = this[ACCEPT_COMPANION] != null

fun <T> Result<T>.handle(processor: IProcessor<AppState>): T? {
    return if (isSuccess) {
        getOrNull()
    } else {
        processor.reduce(Action.SetCommandError(exceptionOrNull()?.message ?: "Unknown error"))
        null
    }
}
