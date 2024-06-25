package ui.plugins.packages

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
import core.Action.CommandAction
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.AppPackage
import ui.MySearchField
import ui.plugins.Plugin

class PackagesPlugin : Plugin<List<AppPackage>> {
    sealed class Actions : IAction {
        data object List : Actions(), CommandAction
        data class Stop(val packageInfo: AppPackage) : Actions(), CommandAction
        data class Uninstall(val packageInfo: AppPackage) : Actions(), CommandAction
        data class ClearData(val packageInfo: AppPackage) : Actions(), CommandAction
    }

    override val name: String = "Installed packages"

    override val id: String = "PACKAGES"

    override val mainAction: IAction = Actions.List

    override val middleware: IMiddleware<AppState> = PackagesPluginMiddleware(id)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

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
