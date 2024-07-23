package ui.plugins.services

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import core.Action
import core.Action.CommandAction
import core.AppState
import core.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.ActivityInfo
import ui.ContentBox
import ui.RegularRow
import ui.RowType
import ui.plugins.Plugin
import ui.plugins.packages.PackageHeader

class ServicesPlugin : Plugin<Pair<String, List<ActivityInfo>>> {
    sealed interface Actions : IAction {
        data object LIST : Actions, CommandAction
    }

    override val name: String = "Services / Action"
    override val id: String = "SERVICES"
    override val mainAction: IAction = Actions.LIST
    override val middleware: IMiddleware<AppState> = ServicesPluginMiddleware(id)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(result: WindowResult<Pair<String, List<ActivityInfo>>>, onAction: (IAction) -> Unit) {
        val items: List<Pair<String, List<ActivityInfo>>> = result.result
        val filter = result.searchTerm
        ContentBox(filter, { onAction(Action.ChangeFilter(id, it)) }) {
            items(items.filter {
                filter.length < 3 || it.first.contains((filter))
            }.flatMap {
                listOf(Pair(RowType.Header, it.first)).plus(it.second.map { Pair(RowType.Regular, it) })
            }
            ) { activity ->
                if (activity.first == RowType.Header)
                    PackageHeader(activity.second as String)
                else if (activity.first == RowType.Regular)
                    RegularRow((activity.second as ActivityInfo).fullPath)

            }
        }
    }
}
