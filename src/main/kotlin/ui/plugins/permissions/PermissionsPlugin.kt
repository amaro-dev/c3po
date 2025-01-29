package ui.plugins.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import commands.CommandExecutor
import commands.PermissionFlag
import core.Action
import core.AppState
import core.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.DeclaredPermissions
import ui.ContentBox
import ui.baselinePadding
import ui.definitions.Dimens
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

    override val mainAction: IAction = Actions.List
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
                    Row(
                        Modifier.padding(Dimens.ROW_HORIZONTAL_MARGIN.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = it.key,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.weight(1f),
                        )
                        it.value.sortedByDescending { it.isBase }.map {
                            PermissionStamp(it)
                            Spacer(Modifier.width(2.dp))
                        }
                    }
                    Divider(
                        color = MaterialTheme.colors.onBackground,
                        modifier = Modifier.height(Dimens.BORDER_REGULAR.dp).fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionStamp(permissionFlag: PermissionFlag) {
    val borderColor =
        if (permissionFlag.isBase) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.secondaryVariant
    val backgroundColor = if (permissionFlag.isBase) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
    val textColor = if (permissionFlag.isBase) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSecondary
    Text(
        text = permissionFlag.name,
        style = MaterialTheme.typography.overline.copy(fontSize = TextUnit(8f, TextUnitType.Sp)),
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, borderColor)
            .background(backgroundColor)
            .baselinePadding(2)
            .padding(start = 6.dp, end = 6.dp)
    )
}
