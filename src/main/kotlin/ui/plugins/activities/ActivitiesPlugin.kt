package ui.plugins.activities

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.Action.CommandAction
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.ActivityInfo
import ui.HeaderRow
import ui.MySearchField
import ui.RegularRow
import ui.plugins.Plugin
import ui.plugins.packages.RowType

class ActivitiesPlugin : Plugin<List<ActivityInfo>> {
    sealed class Actions : IAction {
        data object List : Actions(), CommandAction
        data class Launch(val activityInfo: ActivityInfo) : Actions(), CommandAction
    }

    override val name: String = "Activities"
    override val id: String = "ACTIVITIES"
    override val mainAction: IAction = Actions.List
    override val middleware: IMiddleware<AppState> = ActivitiesPluginMiddleware(id)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(items: List<ActivityInfo>, onAction: (IAction) -> Unit) {
        var filter by remember { mutableStateOf("") }
        Box(Modifier.fillMaxSize()) {
            Column {
                MySearchField { filter = it }
                Box {
                    val listState = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state = listState) {
                        items(items.filter {
                            filter.length < 3 || it.packageName.contains(filter, ignoreCase = true)
                        }.groupBy { it.packageName }
                            .flatMap {
                                listOf(Pair(RowType.Header, it.key)).plus(it.value.map { Pair(RowType.Regular, it) })
                            }
                        ) { activity ->
                            if (activity.first == RowType.Header)
                                HeaderRow(activity.second as String)
                            else if (activity.first == RowType.Regular) {
                                (activity.second as ActivityInfo).let {
                                    RegularRow(
                                        it.activityPath,
                                        Modifier.clickable { onAction(Actions.Launch(it)) }
                                    )
                                }
                                Divider(
                                    color = MaterialTheme.colors.onBackground,
                                    modifier = Modifier.height(1.dp).fillMaxWidth()
                                )
                            }
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(scrollState = listState)
                    )
                }
            }
        }
    }
}
