package ui.plugins.services

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
import ui.plugins.packages.PackageHeader
import ui.plugins.packages.RowType
import ui.useDebounce

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
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(scrollState = listState)
                    )
                }
            }
        }
    }
}

@Composable
fun RegularRow(content: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(6.dp)
            .fillMaxWidth()
    ) {
        Text(
            content,
            style = MaterialTheme.typography.body2,
        )
    }
}
