package ui.plugins.permissions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import commands.CommandExecutor
import core.Action
import core.AppState
import core.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.DeclaredPermissions
import ui.ContentBox
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
                        it.value.map {
                            Surface(
                                color = it.color?.run { Color(this) } ?: MaterialTheme.colors.primary,
                                contentColor = Color.White,
                                shape = androidx.compose.foundation.shape.CircleShape,
                                modifier = Modifier.padding(end = Dimens.HORIZONTAL_SPACER.dp),
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier =
                                        Modifier
                                            .size(36.dp),
                                ) {
                                    Text(
                                        text = it.code,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.overline,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                            }
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
