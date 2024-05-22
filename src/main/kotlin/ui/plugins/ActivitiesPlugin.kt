package ui.plugins

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
import commands.ListActivitiesCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import models.ActivityInfo
import ui.ActivityRow
import ui.MySearchField
import ui.PackageHeader
import ui.RowType

class ActivitiesPlugin() : Plugin<List<ActivityInfo>> {
    sealed class Actions : IAction {
        data object LIST : Actions()
    }

    override val name: String = "ACTIVITIES"
    override val mainAction: IAction = Actions.LIST
    override val middleware: IMiddleware<AppState> = ActivitiesPluginMiddleware(name)

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

class ActivitiesPluginMiddleware(
    private val pluginName: String
) : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            ActivitiesPlugin.Actions.LIST -> {
                state.currentDevice?.run {
                    val activities = ListActivitiesCommand(this).run()
                    processor.reduce(Action.DeliverPluginResult(pluginName, activities))
                }
            }
        }
    }

}
