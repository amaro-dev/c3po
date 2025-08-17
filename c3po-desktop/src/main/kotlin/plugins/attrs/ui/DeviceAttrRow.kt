package plugins.attrs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import core.model.Action
import ui.CopyButton
import ui.OnAction
import ui.definitions.Texts
import ui.onHover
import ui.slideInHorizontallyFromRight
import ui.slideOutHorizontallyToRight

@Composable
fun DeviceAttrRow(
    label: String,
    value: String?,
    onAction: OnAction,
) {
    var isHoveringAttr: Boolean by remember { mutableStateOf(false) }
    var isHoveringValue: Boolean by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(5f)
                    .onHover { isHoveringAttr = it },
                textAlign = TextAlign.End,
            )
            Text(
                text = value ?: Texts.NO_VALUE,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .onHover { isHoveringValue = it }
                    .weight(7f)
                    .padding(start = 16.dp),
            )
        }
        CopyButton(
            isHoveringValue,
            { isHoveringValue = it },
            { onAction(Action.CopyText(value ?: Texts.EMPTY)) },
            Modifier.align(Alignment.CenterEnd),
            slideInHorizontallyFromRight(),
            slideOutHorizontallyToRight(),
        )
        CopyButton(
            isHoveringAttr,
            { isHoveringAttr = it },
            { onAction(Action.CopyText(label.removeSuffix(Texts.PROP_SUFFIX))) },
            Modifier.align(Alignment.CenterStart),
        )
    }
}

@Composable
fun EnhancedDeviceAttrRow(
    label: String,
    value: String?,
    onAction: OnAction,
    showBottomBorder: Boolean = true
) {
    var isHoveringAttr: Boolean by remember { mutableStateOf(false) }
    var isHoveringValue: Boolean by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${label}:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(5f)
                        .onHover { isHoveringAttr = it },
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = value ?: Texts.NO_VALUE,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .onHover { isHoveringValue = it }
                        .weight(7f)
                        .padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Preserve the original copy button animations
            CopyButton(
                isHoveringValue,
                { isHoveringValue = it },
                { onAction(Action.CopyText(value ?: Texts.EMPTY)) },
                Modifier.align(Alignment.CenterEnd),
                slideInHorizontallyFromRight(),
                slideOutHorizontallyToRight(),
            )
            CopyButton(
                isHoveringAttr,
                { isHoveringAttr = it },
                { onAction(Action.CopyText(label.removeSuffix(Texts.PROP_SUFFIX))) },
                Modifier.align(Alignment.CenterStart),
            )
        }
    }

    if (showBottomBorder) {
        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
