package ui.plugins.activities

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import ui.ActionableRow
import ui.ContentBox
import ui.Dimens
import ui.HeaderRow
import ui.Icons
import ui.RowAction
import ui.RowType
import ui.plugins.Plugin

class ActivitiesPlugin(
    executor: CommandExecutor,
) : Plugin<ActivityInfo> {
    sealed interface Actions : IAction {
        data object List : Actions, CommandAction

        data class Launch(
            val activityInfo: ActivityInfo,
            val forDebug: Boolean = false,
        ) : Actions,
            CommandAction
    }

    companion object {
        const val LIST_ACTIVITY_SOCKET_COMMAND = "list-activities"
    }

    override val name: String = "Activities"
    override val id: String = "ACTIVITIES"
    override val mainAction: IAction = Actions.List
    override val middleware: IMiddleware<AppState> = ActivitiesPluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean =
        action is Actions || (action is Action.DeliverSocketResponse && action.reference.command == LIST_ACTIVITY_SOCKET_COMMAND)

    @Composable
    override fun present(
        result: WindowResult<ActivityInfo>,
        onAction: (IAction) -> Unit,
    ) {
        val items: List<ActivityInfo> = result.result
        val filter = result.searchTerm
        ContentBox(filter, { onAction(Action.ChangeFilter(id, it)) }) {
            items(
                items
                    .filter {
                        filter.length < 3 ||
                                it.packageName.contains(filter, ignoreCase = true) ||
                                it.fullPath.contains(filter, ignoreCase = true)
                    }.groupBy { it.packageName }
                    .flatMap {
                        listOf(Pair(RowType.Header, it.key)).plus(it.value.map { Pair(RowType.Regular, it) })
                    },
            ) { activity ->
                if (activity.first == RowType.Header) {
                    HeaderRow(activity.second as String)
                } else if (activity.first == RowType.Regular) {
                    (activity.second as ActivityInfo).let {
                        ActionableRow(
                            listOf(
                                RowAction(Icons.LAUNCH, "Start activity", Actions.Launch(it)),
                                RowAction(Icons.DEBUG, "Start activity for debug", Actions.Launch(it, true)),
                            ),
                            onAction,
                        ) { Text(it.activityPath) }
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
