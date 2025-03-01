package plugins.permissions

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import commands.CommandExecutor
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.DeclaredPermissions
import models.WindowResult
import ui.ContentBox
import ui.rows.HeaderRow

class PermissionsPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<DeclaredPermissions> {
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
