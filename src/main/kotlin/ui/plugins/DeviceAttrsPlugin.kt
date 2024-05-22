package ui.plugins

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
import commands.DeviceInfoCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import ui.DeviceAttrRow
import ui.MySearchField


class DeviceAttrsPlugin() : Plugin<List<Pair<String, String>>> {
    sealed class Actions : IAction {
        data object LIST : Actions()
    }

    override val name: String = "DEVICE_ATTRS"
    override val mainAction: IAction = Actions.LIST
    override val middleware: IMiddleware<AppState> = DeviceAttrsMiddleware(name)

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

class DeviceAttrsMiddleware(
    private val pluginName: String
) : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            DeviceAttrsPlugin.Actions.LIST -> {
                state.currentDevice?.run {
                    val deviceInfo = DeviceInfoCommand(this).run()
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, deviceInfo.toList().sortedBy { it.first })
                    )
                }
            }
        }
    }
}
