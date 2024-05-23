package ui.plugins.activities

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.ActivityInfo
import ui.MySearchField
import ui.plugins.Plugin
import ui.plugins.packages.PackageHeader
import ui.plugins.packages.RowType

class ActivitiesPlugin() : Plugin<List<ActivityInfo>> {
    sealed class Actions : IAction {
        data object List : Actions()
        data class Launch(val activityInfo: ActivityInfo) : Actions()
    }

    override val name: String = "ACTIVITIES"
    override val mainAction: IAction = Actions.List
    override val middleware: IMiddleware<AppState> = ActivitiesPluginMiddleware(name)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(items: List<ActivityInfo>, onAction: (IAction) -> Unit) {
        var filter by remember { mutableStateOf("") }
        Box(Modifier.fillMaxSize()) {
            Column {
                MySearchField { filter = it }
                Box(Modifier.fillMaxSize().padding(20.dp)) {
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
                                PackageHeader(activity.second as String)
                            else if (activity.first == RowType.Regular)
                                ActivityRow(activity.second as ActivityInfo, onAction)

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
