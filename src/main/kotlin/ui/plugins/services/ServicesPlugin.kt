package ui.plugins.services

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import core.Action.CommandAction
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.ActivityInfo
import ui.ContentBox
import ui.RegularRow
import ui.RowType
import ui.Texts
import ui.plugins.Plugin
import ui.plugins.packages.PackageHeader

class ServicesPlugin : Plugin<List<Pair<String, List<ActivityInfo>>>> {
    sealed class Actions : IAction {
        data object LIST : Actions(), CommandAction
    }

    override val name: String = "Services / Action"
    override val id: String = "SERVICES"
    override val mainAction: IAction = Actions.LIST
    override val middleware: IMiddleware<AppState> = ServicesPluginMiddleware(id)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(items: List<Pair<String, List<ActivityInfo>>>, onAction: (IAction) -> Unit) {
        var filter by remember { mutableStateOf(Texts.EMPTY) }
        ContentBox({ filter = it }) {
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
