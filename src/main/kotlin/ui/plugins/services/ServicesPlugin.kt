package ui.plugins.services

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.Action.CommandAction
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.ActivityInfo
import ui.plugins.Plugin
import ui.plugins.activities.ActivitiesPlugin.Actions
import ui.plugins.activities.ActivityRow
import ui.plugins.packages.PackageHeader
import ui.plugins.packages.RowType
import ui.useDebounce

class ServicesPlugin() : Plugin<List<ActivityInfo>> {
    sealed class Actions : IAction {
        data object LIST : Actions(), CommandAction
    }

    override val name: String = "SERVICES"
    override val mainAction: IAction = Actions.LIST
    override val middleware: IMiddleware<AppState> = ServicesPluginMiddleware(name)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(items: List<ActivityInfo>, onAction: (IAction) -> Unit) {
        var searchTerm by remember { mutableStateOf("") }
        var filter by remember { mutableStateOf("") }
        searchTerm.useDebounce {
            filter = it
        }
        Box(Modifier.fillMaxSize().padding(10.dp)) {
            Column {
                OutlinedTextField(
                    searchTerm,
                    onValueChange = { searchTerm = it },
                    leadingIcon = { Icon(Icons.Filled.Search, "") },
                    placeholder = { Text("Type to search") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(Modifier.fillMaxSize().padding(10.dp)) {
                    val listState = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state = listState) {
                        items(items.filter {
                            filter.length < 3 || it.packageName.contains((filter))
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
