package plugins.attrs.definition

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import core.command.CommandExecutor
import core.model.Action
import core.model.Action.CommandAction
import core.model.AppState
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.attrs.structure.DeviceAttrsMiddleware
import plugins.attrs.ui.EnhancedDeviceAttrRow
import ui.OnAction
import ui.parts.EnhancedScrollableList
import ui.parts.EnhancedSearchBar

class DeviceAttrsPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<Pair<String, String>> {
    sealed interface Actions : IAction {
        data object List : Actions, CommandAction
    }

    override val name: String = "Attributes"
    override val id: String = "DEVICE_ATTRS"
    override val icon: ImageVector = Icons.Filled.Info
    override val middleware: IMiddleware<AppState> = DeviceAttrsMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<Pair<String, String>>,
        onAction: OnAction,
    ) {
        val items: List<Pair<String, String>> = result.result
        val filter = result.searchTerm

        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar with simple text field only
            EnhancedSearchBar(
                searchTerm = filter,
                onSearchChange = { onAction(Action.ChangeFilter(id, it)) },
                filterState = Unit, // No filter state needed for simple search
                onFilterChange = { }, // No filter state to change
                searchPlaceholder = "Search device attributes..."
            ) { _, _ ->
                // No additional filter controls needed for Attributes
            }

            // Attributes list
            EnhancedScrollableList(
                items = items.filter {
                    filter.length < 2 ||
                            it.first.contains(filter, ignoreCase = true) ||
                            it.second.contains(filter, ignoreCase = true)
                }
            ) { attr, showBottomBorder ->
                EnhancedDeviceAttrRow(
                    label = attr.first,
                    value = attr.second,
                    onAction = onAction,
                    showBottomBorder = showBottomBorder
                )
            }
        }
    }
}
