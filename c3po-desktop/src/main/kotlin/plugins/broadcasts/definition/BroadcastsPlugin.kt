package plugins.broadcasts.definition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import core.command.CommandExecutor
import core.model.Action
import core.model.Action.CommandAction
import core.model.AppState
import core.model.BroadcastAction
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.Plugin
import plugins.broadcasts.structure.BroadcastsMiddleware
import plugins.broadcasts.ui.BroadcastActionRow
import plugins.broadcasts.ui.SendBroadcastDialog
import ui.OnAction
import ui.component.EnhancedHeaderRow
import ui.parts.EnhancedScrollableList
import ui.parts.EnhancedSearchBar
import ui.rows.RowType

enum class GroupingMode {
    BY_ACTION, BY_PACKAGE
}

class BroadcastsPlugin(
    executor: CommandExecutor,
) : Plugin<BroadcastAction> {

    sealed interface Actions : IAction {
        data object List : Actions, CommandAction
        data class Send(val broadcastAction: BroadcastAction, val extras: Map<String, String> = emptyMap()) : Actions,
            CommandAction
    }

    override val name: String = "Broadcasts"
    override val id: String = "BROADCASTS"
    override val icon: ImageVector = Icons.AutoMirrored.Filled.Send
    override val middleware: IMiddleware<AppState> = BroadcastsMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<BroadcastAction>,
        onAction: OnAction,
    ) {
        val broadcasts: List<BroadcastAction> = result.result
        val filter = result.searchTerm

        // Dialog state management
        var showSendDialog by remember { mutableStateOf(false) }
        var selectedBroadcast by remember { mutableStateOf<BroadcastAction?>(null) }

        // Grouping state management
        var groupingMode by remember { mutableStateOf(GroupingMode.BY_ACTION) }

        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar with grouping controls
            EnhancedSearchBar(
                searchTerm = filter,
                onSearchChange = { onAction(Action.ChangeFilter(id, it)) },
                filterState = groupingMode,
                onFilterChange = { groupingMode = it },
                searchPlaceholder = "Search broadcast actions..."
            ) { currentGroupingMode, onGroupingChange ->
                GroupingModeRadioButton(
                    label = "Group by Action",
                    selected = currentGroupingMode == GroupingMode.BY_ACTION,
                    onClick = { onGroupingChange(GroupingMode.BY_ACTION) }
                )
                GroupingModeRadioButton(
                    label = "Group by Package",
                    selected = currentGroupingMode == GroupingMode.BY_PACKAGE,
                    onClick = { onGroupingChange(GroupingMode.BY_PACKAGE) }
                )
            }

            // Broadcasts list with grouping
            BroadcastsScrollableList(
                broadcasts = broadcasts,
                filter = filter,
                groupingMode = groupingMode,
                onAction = onAction,
                onSendWithExtras = { broadcast ->
                    selectedBroadcast = broadcast
                    showSendDialog = true
                }
            )
        }

        // Send with extras dialog
        if (showSendDialog && selectedBroadcast != null) {
            SendBroadcastDialog(
                broadcastAction = selectedBroadcast!!,
                onSend = { extras ->
                    onAction(Actions.Send(selectedBroadcast!!, extras))
                    showSendDialog = false
                    selectedBroadcast = null
                },
                onDismiss = {
                    showSendDialog = false
                    selectedBroadcast = null
                }
            )
        }
    }
}

@Composable
private fun GroupingModeRadioButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(28.dp)
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BroadcastsScrollableList(
    broadcasts: List<BroadcastAction>,
    filter: String,
    groupingMode: GroupingMode,
    onAction: OnAction,
    onSendWithExtras: (BroadcastAction) -> Unit
) {
    // First, deduplicate broadcasts and then filter based on search
    val filteredBroadcasts = broadcasts
        .distinctBy { Triple(it.action, it.packageName, it.requiredPermission) } // Remove exact duplicates
        .filter {
            filter.length < 2 ||
                    it.action.contains(filter, ignoreCase = true) ||
                    it.packageName.contains(filter, ignoreCase = true) ||
                    (it.requiredPermission?.contains(filter, ignoreCase = true) == true)
        }

    // Then, group and flatten for display
    val flattenedItems = when (groupingMode) {
        GroupingMode.BY_ACTION -> {
            filteredBroadcasts
                .groupBy { it.action }
                .flatMap { group ->
                    listOf(Pair(RowType.Header, group.key)).plus(
                        group.value.map { Pair(RowType.Regular, it) }
                    )
                }
        }

        GroupingMode.BY_PACKAGE -> {
            filteredBroadcasts
                .groupBy { it.packageName }
                .flatMap { group ->
                    listOf(Pair(RowType.Header, group.key)).plus(
                        group.value.map { Pair(RowType.Regular, it) }
                    )
                }
        }
    }

    EnhancedScrollableList(
        items = flattenedItems
    ) { item, showBottomBorder ->
        when (item.first) {
            RowType.Header -> {
                EnhancedHeaderRow(item.second as String)
            }

            RowType.Regular -> {
                val broadcast = item.second as BroadcastAction
                BroadcastActionRow(
                    broadcastAction = broadcast,
                    groupingMode = groupingMode,
                    onAction = onAction,
                    onSendWithExtras = { onSendWithExtras(broadcast) },
                    showBottomBorder = showBottomBorder
                )
            }
        }
    }
}
