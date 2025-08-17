package plugins.permissions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.command.PermissionFlag
import core.model.Action
import dev.amaro.sonic.IAction
import ui.CopyButton
import ui.definitions.Dimens
import ui.onHover

@Composable
fun PermissionRow(
    permission: Map.Entry<String, List<PermissionFlag>>,
    onAction: (IAction) -> Unit,
) {
    var isHovering: Boolean by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier.onHover { isHovering = it }.padding(Dimens.ROW_HORIZONTAL_MARGIN.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = permission.key,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(1f),
            )
            permission.value.sortedByDescending { it.isBase }.map {
                PermissionStamp(it)
                Spacer(Modifier.width(2.dp))
            }
        }
        Divider(
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.height(Dimens.BORDER_REGULAR.dp).fillMaxWidth(),
        )
        CopyButton(
            isHovering,
            { isHovering = it },
            { onAction(Action.CopyText(permission.key)) },
            Modifier.align(Alignment.CenterStart),
        )
    }
}
