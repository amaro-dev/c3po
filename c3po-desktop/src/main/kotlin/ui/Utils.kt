package ui

import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.IntOffset
import dev.amaro.sonic.IAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T> T.useDebounce(
    delayMillis: Long = 300L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit,
): T {
    // 2. updating state
    val state by rememberUpdatedState(this)

    // 3. launching the side-effect handler
    DisposableEffect(state) {
        val job =
            coroutineScope.launch {
                delay(delayMillis)
                onChange(state)
            }
        onDispose {
            job.cancel()
        }
    }
    return state
}

typealias OnAction = (IAction) -> Unit

fun slideInHorizontallyFromRight() = slideIn(initialOffset = { IntOffset(it.width, 0) })

fun slideOutHorizontallyToRight() = slideOut(targetOffset = { IntOffset(it.width, 0) })
