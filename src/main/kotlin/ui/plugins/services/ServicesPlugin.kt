package ui.plugins.services

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import commands.CommandExecutor
import core.Action
import core.Action.CommandAction
import core.AppState
import core.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.ActivityInfo
import ui.ContentBox
import ui.Dimens
import ui.PackageHeader
import ui.RegularRow
import ui.RowType
import ui.plugins.Plugin

class ServicesPlugin(
    executor: CommandExecutor,
) : Plugin<Pair<String, List<ActivityInfo>>> {
    sealed interface Actions : IAction {
        data object LIST : Actions, CommandAction
    }

    companion object {
        const val LIST_SERVICE_SOCKET_COMMAND = "list-services"
    }

    override val name: String = "Services / Action"
    override val id: String = "SERVICES"
    override val mainAction: IAction = Actions.LIST
    override val middleware: IMiddleware<AppState> = ServicesPluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean =
        action is Actions || (action is Action.DeliverSocketResponse && action.reference.command == LIST_SERVICE_SOCKET_COMMAND)

    @Composable
    override fun present(
        result: WindowResult<Pair<String, List<ActivityInfo>>>,
        onAction: (IAction) -> Unit,
    ) {
        val items: List<Pair<String, List<ActivityInfo>>> = result.result
        val filter = result.searchTerm
        ContentBox(filter, { onAction(Action.ChangeFilter(id, it)) }) {
            items(
                items
                    .filter {
                        filter.length < 3 || it.first.contains((filter))
                    }.flatMap {
                        listOf(Pair(RowType.Header, it.first)).plus(it.second.map { Pair(RowType.Regular, it) })
                    },
            ) { activity ->
                if (activity.first == RowType.Header) {
                    PackageHeader(activity.second as String)
                } else if (activity.first == RowType.Regular) {
                    RegularRow((activity.second as ActivityInfo).activityPath)
                }
                Divider(
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier.height(Dimens.BORDER_REGULAR.dp).fillMaxWidth(),
                )
            }
        }
    }
}
