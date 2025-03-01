package plugins.attrs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import commands.CommandExecutor
import core.Action
import core.Action.CommandAction
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.WindowResult
import ui.ContentBox
import ui.OnAction
import ui.definitions.Dimens

class DeviceAttrsPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<Pair<String, String>> {
    sealed interface Actions : IAction {
        data object List : Actions, CommandAction
    }

    override val name: String = "Device attributes"
    override val id: String = "DEVICE_ATTRS"
    override val middleware: IMiddleware<AppState> = DeviceAttrsMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<Pair<String, String>>,
        onAction: OnAction,
    ) {
        val items: List<Pair<String, String>> = result.result
        val filter = result.searchTerm
        ContentBox(filter, { onAction(Action.ChangeFilter(id, it)) }) {
            items(
                items.filter {
                    filter.length < 2 ||
                            it.first.contains(filter, ignoreCase = true) ||
                            it.second.contains(filter, ignoreCase = true)
                },
            ) {
                DeviceAttrRow("${it.first}:", it.second, onAction)
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(Dimens.BORDER_REGULAR.dp)
                        .background(MaterialTheme.colors.onBackground),
                )
            }
        }
    }
}
