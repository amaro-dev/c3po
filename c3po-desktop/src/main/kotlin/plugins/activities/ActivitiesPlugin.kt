package plugins.activities

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import core.command.CommandExecutor
import core.model.Action
import core.model.Action.CommandAction
import core.model.ActivityInfo
import core.model.AppState
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.activities.structure.ActivitiesPluginMiddleware
import ui.ContentBox
import ui.OnAction
import ui.definitions.Dimens
import ui.definitions.Icons
import ui.rows.ActionableRow
import ui.rows.HeaderRow
import ui.rows.RowAction
import ui.rows.RowType

class ActivitiesPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<ActivityInfo> {
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
    override val icon: ImageVector = androidx.compose.material.icons.Icons.Filled.Android
    override val middleware: IMiddleware<AppState> = ActivitiesPluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean =
        action is Actions || (action is Action.DeliverSocketResponse && action.reference.command == LIST_ACTIVITY_SOCKET_COMMAND)

    @Composable
    override fun present(
        result: WindowResult<ActivityInfo>,
        onAction: OnAction,
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
                        ) {
                            Text(
                                text = it.activityPath,
                                style = MaterialTheme.typography.body2,
                            )
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
