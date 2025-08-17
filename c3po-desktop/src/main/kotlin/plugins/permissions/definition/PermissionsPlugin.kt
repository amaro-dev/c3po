package plugins.permissions.definition

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import core.command.CommandExecutor
import core.model.Action
import core.model.AppState
import core.model.DeclaredPermissions
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.permissions.PermissionRow
import plugins.permissions.structure.PermissionsPluginMiddleware
import ui.ContentBox
import ui.rows.HeaderRow

class PermissionsPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<DeclaredPermissions> {
    sealed interface Actions : IAction {
        data object List : Actions, Action.CommandAction
    }

    override val id: String = "PERMISSIONS"
    override val name: String = "Permissions"
    override val icon: ImageVector = Icons.Filled.Security

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
