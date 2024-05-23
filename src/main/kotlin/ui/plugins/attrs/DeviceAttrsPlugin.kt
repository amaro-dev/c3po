package ui.plugins.attrs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import ui.MySearchField
import ui.plugins.Plugin
import ui.plugins.activities.ActivitiesPlugin.Actions


class DeviceAttrsPlugin() : Plugin<List<Pair<String, String>>> {
    sealed class Actions : IAction {
        data object List : Actions()
    }

    override val name: String = "DEVICE_ATTRS"
    override val mainAction: IAction = Actions.List
    override val middleware: IMiddleware<AppState> = DeviceAttrsMiddleware(name)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(items: List<Pair<String, String>>, onAction: (IAction) -> Unit) {
        var filter by remember { mutableStateOf("") }
        Box(Modifier.fillMaxSize()) {
            Column {
                MySearchField { filter = it }
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .border(width = 1.dp, color = MaterialTheme.colors.onBackground)
                ) {
                    val listState = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state = listState) {
                        items(items.filter {
                            filter.length < 3 || it.first.contains(filter, ignoreCase = true)
                        }) {
                            DeviceAttrRow("${it.first}:", it.second)
                            Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onBackground))
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
