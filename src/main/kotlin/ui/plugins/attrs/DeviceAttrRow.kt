package ui.plugins.attrs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import core.Action
import ui.CopyButton
import ui.OnAction
import ui.definitions.Dimens
import ui.definitions.Texts
import ui.onHover
import ui.rows.BaseRow
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
        BaseRow(Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(5f)
                        .onHover { isHoveringAttr = it },
                textAlign = TextAlign.End,
            )
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            Text(
                text = value ?: Texts.NO_VALUE,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.onHover { isHoveringValue = it }.weight(7f),
            )
        }
        CopyButton(
            isHoveringValue,
            { isHoveringValue = it },
            { onAction(Action.CopyText(value ?: Texts.EMPTY)) },
            Modifier.align(Alignment.CenterEnd)
        )
        CopyButton(
            isHoveringAttr,
            { isHoveringAttr = it },
            { onAction(Action.CopyText(label.removeSuffix(Texts.PROP_SUFFIX))) },
            Modifier.align(Alignment.CenterStart),
            slideInHorizontallyFromRight(),
            slideOutHorizontallyToRight()
        )
    }
}
