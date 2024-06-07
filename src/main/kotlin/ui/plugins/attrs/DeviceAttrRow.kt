package ui.plugins.attrs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import core.Action
import dev.amaro.sonic.IAction


@Composable
fun DeviceAttrRow(label: String, value: String?, onAction: (IAction) -> Unit) {
    var isHovering: Boolean by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(5f)
                    .padding(start = 10.dp)
                    .onHover { isHovering = it },
                textAlign = TextAlign.End
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = value ?: "-",
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.weight(8f)
            )
        }
        AnimatedVisibility(isHovering, enter = slideInHorizontally(), exit = slideOutHorizontally()) {
            Box(Modifier.padding(start = 10.dp).onHover { isHovering = it }) {
                IconButton(onClick = { onAction(Action.CopyText(label)) }) {
                    Icon(Icons.Default.Info, "")
                }
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
