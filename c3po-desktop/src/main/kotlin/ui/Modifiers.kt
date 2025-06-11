package ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ui.definitions.Dimens

fun Modifier.baselinePadding(bottom: Int = Dimens.ROW_VERTICAL_MARGIN_BOTTOM) = padding(
    top = (bottom * 0.6f).dp,
    bottom = bottom.dp
)

fun Modifier.horizontalPadding() = padding(
    start = Dimens.ROW_HORIZONTAL_MARGIN.dp,
    end = Dimens.ROW_HORIZONTAL_MARGIN.dp
)

fun Modifier.verticalPadding() = padding(
    top = Dimens.ROW_VERTICAL_MARGIN.dp,
    bottom = Dimens.ROW_VERTICAL_MARGIN.dp
)

fun Modifier.allPaddings() = horizontalPadding().verticalPadding()

@Composable
fun Modifier.withIconStyle(): Modifier =
    background(color = MaterialTheme.colors.background)
        .padding(Dimens.HORIZONTAL_SPACER.dp)
        .size(16.dp)

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.onHover(event: (Boolean) -> Unit) =
    this
        .onPointerEvent(
            eventType = PointerEventType.Enter,
            pass = PointerEventPass.Main,
            onEvent = { event(true) },
        ).onPointerEvent(
            eventType = PointerEventType.Exit,
            pass = PointerEventPass.Main,
            onEvent = { event(false) },
        ).onPointerEvent(
            eventType = PointerEventType.Move,
            pass = PointerEventPass.Main,
            onEvent = { event(true) },
        )

@Composable
fun Modifier.popTransition(visible: Boolean): Modifier {
    val offsetAnim by animateIntOffsetAsState(
        targetValue = if (visible) {
            IntOffset(0, Dimens.SQUARE_BUTTON_SIZE_REGULAR - 2)
        } else {
            IntOffset.Zero
        },
        label = "offset"
    )
    val transparency = animateFloatAsState(
        targetValue = if (!visible) 0f else 1f
    )
    return offset { offsetAnim }.alpha(transparency.value)
}
