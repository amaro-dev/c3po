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
import commands.ListPackagesCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import models.AppPackage
import ui.MySearchField
import ui.PackageRow

class PackagesPlugin : Plugin<List<AppPackage>> {
    sealed class Actions : IAction {
        data object LIST : Actions()
    }

    override val name: String = "PACKAGES"

    override val mainAction: IAction = Actions.LIST

    override val middleware: IMiddleware<AppState> = PackagesPluginMiddleware(name)

    @Composable
    override fun present(items: List<AppPackage>, onAction: (IAction) -> Unit) {
        var filter by remember { mutableStateOf("") }
        Box(Modifier.fillMaxSize()) {
            Column {
                MySearchField { filter = it }
                Box(Modifier.padding(20.dp)) {
                    val listState = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state = listState) {
                        items(items.filter {
                            filter.length < 3 || it.packageName.contains((filter))
                        }) { pkg ->
                            PackageRow(pkg, onAction)
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

class PackagesPluginMiddleware(private val name: String) : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            PackagesPlugin.Actions.LIST -> {
                state.currentDevice?.run {
                    ListPackagesCommand(this).run()
                }
            }

            else -> null
        }?.run { processor.reduce(Action.DeliverPluginResult(name, this)) }
    }

}
