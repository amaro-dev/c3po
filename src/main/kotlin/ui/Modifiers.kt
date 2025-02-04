package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
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
            onEvent = { event(true) },
        ).onPointerEvent(
            eventType = PointerEventType.Exit,
            onEvent = { event(false) },
        )
