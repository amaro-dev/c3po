package ui.plugins.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import commands.CommandExecutor
import commands.PermissionFlag
import core.Action
import core.AppState
import core.WindowResult
import darkenedBy
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.DeclaredPermissions
import ui.ContentBox
import ui.CopyButton
import ui.baselinePadding
import ui.definitions.Dimens
import ui.onHover
import ui.plugins.Plugin
import ui.rows.HeaderRow

class PermissionsPlugin(
    executor: CommandExecutor,
) : Plugin<DeclaredPermissions> {
    sealed interface Actions : IAction {
        data object List : Actions, Action.CommandAction
    }

    override val id: String = "PERMISSIONS"
    override val name: String = "Declared permissions"

    override val middleware: IMiddleware<AppState> = PermissionsPluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<DeclaredPermissions>,
        onAction: (IAction) -> Unit,
    ) {
        val items: List<DeclaredPermissions> = result.result
        val filter = result.searchTerm
        ContentBox(filter, { onAction(Action.ChangeFilter(id, it)) }) {
            items(
                items.filter {
                    filter.length < 3 || it.permissions.any { p -> p.key.contains(filter) }
                },
            ) { owner ->
                HeaderRow(owner.ownerApp)
                owner.permissions.filter { filter.length < 3 || it.key.contains(filter) }.map {
                    PermissionRow(it, onAction)
                }
            }
        }
    }
}

@Composable
fun PermissionRow(permission: Map.Entry<String, List<PermissionFlag>>, onAction: (IAction) -> Unit) {
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
            Modifier.align(Alignment.CenterStart)
        )
    }
}


@Composable
fun PermissionStamp(permissionFlag: PermissionFlag) {
    val borderColor =
        if (permissionFlag.isBase) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.secondaryVariant
    Text(
        text = permissionFlag.name,
        style = MaterialTheme.typography.overline.copy(fontSize = TextUnit(8f, TextUnitType.Sp)),
        color = permissionFlag.getPermissionForegroundColor(),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, borderColor)
            .background(permissionFlag.getPermissionBackgroundColor())
            .baselinePadding(2)
            .padding(start = 6.dp, end = 6.dp)
    )
}

@Composable
fun PermissionFlag.getPermissionForegroundColor(): Color {
    return if (isBase) {
        MaterialTheme.colors.onPrimary
    } else {
        if (this == PermissionFlag.PRIVILEGED)
            MaterialTheme.colors.onError
        else
            MaterialTheme.colors.onSecondary
    }
}

@Composable
fun PermissionFlag.getPermissionBackgroundColor(): Color {
    return if (isBase) {
        MaterialTheme.colors.primary
    } else {
        if (this == PermissionFlag.PRIVILEGED)
            MaterialTheme.colors.error
        else
            MaterialTheme.colors.secondary
    }
}

@Composable
fun PermissionFlag.getPermissionBorderColor(): Color {
    return if (isBase) {
        MaterialTheme.colors.primaryVariant
    } else {
        if (this == PermissionFlag.PRIVILEGED)
            MaterialTheme.colors.error.darkenedBy(0.3f)
        else
            MaterialTheme.colors.secondaryVariant
    }
}
