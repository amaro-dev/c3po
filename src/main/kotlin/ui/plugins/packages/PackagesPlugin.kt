package ui.plugins.packages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.Action.CommandAction
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.AppPackage
import ui.*
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
        var filter by remember { mutableStateOf(Texts.EMPTY) }
        ContentBox({ filter = it }) {
            items(items.filter {
                filter.length < 3 || it.packageName.contains((filter))
            }) { pkg ->
                ActionableRow(
                    pkg.packageName,
                    listOf(
                        RowAction(Icons.DELETE, Texts.EMPTY, Actions.Uninstall(pkg)),
                        RowAction(Icons.CLOSE, Texts.EMPTY, Actions.Stop(pkg)),
                        RowAction(Icons.WIPE, Texts.EMPTY, Actions.ClearData(pkg))
                    ),
                    onAction
                )
                Divider(
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier.height(Dimens.BORDER_REGULAR.dp).fillMaxWidth()
                )
            }
        }
    }
}
