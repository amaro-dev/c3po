package ui.plugins.attrs

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import core.Action
import dev.amaro.sonic.IAction
import ui.Dimens


@Composable
fun DeviceAttrRow(label: String, value: String?, onAction: (IAction) -> Unit) {
    var isHoveringAttr: Boolean by remember { mutableStateOf(false) }
    var isHoveringValue: Boolean by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Row(
            Modifier.fillMaxWidth().height(Dimens.ROW_HEIGHT_LARGE.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(5f)
                    .padding(start = Dimens.ROW_HORIZONTAL_MARGIN.dp)
                    .onHover { isHoveringAttr = it },
                textAlign = TextAlign.End
            )
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            Box(Modifier.weight(8f), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = value ?: "-",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.onHover { isHoveringValue = it }.fillMaxWidth(),
                )
                androidx.compose.animation.AnimatedVisibility(
                    isHoveringValue,
                    enter = slideInHorizontallyFromRight(),
                    exit = slideOutHorizontallyToRight()
                ) {
                    Box(
                        Modifier.padding(end = Dimens.ROW_HORIZONTAL_MARGIN.dp)
                            .onHover { isHoveringValue = it }
                            .clip(CircleShape)
                    ) {
                        Icon(
                            painterResource("ic_copy.svg"),
                            contentDescription = "",
                            modifier = Modifier.clickable { onAction(Action.CopyText(value ?: "")) }
                                .withIconStyle()
                        )
                    }
                }
            }
        }
        AnimatedVisibility(isHoveringAttr, enter = slideInHorizontally(), exit = slideOutHorizontally()) {
            Box(
                modifier = Modifier.padding(start = Dimens.ROW_HORIZONTAL_MARGIN.dp)
                    .onHover { isHoveringAttr = it }
                    .clip(CircleShape)
            ) {
                Icon(
                    painterResource("ic_copy.svg"),
                    contentDescription = "",
                    modifier = Modifier.clickable { onAction(Action.CopyText(label.removeSuffix(":"))) }
                        .withIconStyle()

                )
            }
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.onHover(event: (Boolean) -> Unit) = this.onPointerEvent(
    eventType = PointerEventType.Enter,
    onEvent = { event(true) }
).onPointerEvent(
    eventType = PointerEventType.Exit,
    onEvent = { event(false) }
)

fun slideInHorizontallyFromRight() = slideIn(initialOffset = { IntOffset(it.width, 0) })
fun slideOutHorizontallyToRight() = slideOut(targetOffset = { IntOffset(it.width, 0) })

@Composable
fun Modifier.withIconStyle(): Modifier = this.background(
    color = MaterialTheme.colors.background,
    shape = CircleShape
)
    .padding(Dimens.HORIZONTAL_SPACER.dp)
    .clip(shape = CircleShape)
