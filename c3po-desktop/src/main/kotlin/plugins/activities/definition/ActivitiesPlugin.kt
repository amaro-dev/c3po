package plugins.activities.definition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import core.command.CommandExecutor
import core.model.Action
import core.model.Action.CommandAction
import core.model.ActivityInfo
import core.model.AppState
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.activities.structure.ActivitiesPluginMiddleware
import ui.OnAction
import ui.component.CustomTextField
import ui.rows.RowType
import androidx.compose.material.icons.Icons as MaterialIcons

class ActivitiesPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<ActivityInfo> {
    sealed interface Actions : IAction {
        data object List : Actions, CommandAction

        data class Launch(
            val activityInfo: ActivityInfo,
            val forDebug: Boolean = false,
        ) : Actions,
            CommandAction

        data class SetLauncher(
            val activityInfo: ActivityInfo,
        ) : Actions,
            CommandAction
    }

    override val name: String = "Activities"
    override val id: String = "ACTIVITIES"
    override val icon: ImageVector = MaterialIcons.Filled.Android
    override val middleware: IMiddleware<AppState> = ActivitiesPluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean =
        action is Actions

    @Composable
    override fun present(
        result: WindowResult<ActivityInfo>,
        onAction: OnAction,
    ) {
        val items: List<ActivityInfo> = result.result
        val filter = result.searchTerm

        var launchableFilter by remember { mutableStateOf(false) }
        var debuggableFilter by remember { mutableStateOf(false) }
        var launcherFilter by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize()) {
            // Custom search bar with filters
            EnhancedSearchBar(
                searchTerm = filter,
                onSearchChange = { onAction(Action.ChangeFilter(id, it)) },
                launchableChecked = launchableFilter,
                debuggableChecked = debuggableFilter,
                launcherChecked = launcherFilter,
                onLaunchableChange = { launchableFilter = it },
                onDebuggableChange = { debuggableFilter = it },
                onLauncherChange = { launcherFilter = it }
            )

            // Activities list
            ActivitiesScrollableList(
                items = items,
                filter = filter,
                launchableFilter = launchableFilter,
                debuggableFilter = debuggableFilter,
                launcherFilter = launcherFilter,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun EnhancedSearchBar(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    launchableChecked: Boolean,
    debuggableChecked: Boolean,
    launcherChecked: Boolean,
    onLaunchableChange: (Boolean) -> Unit,
    onDebuggableChange: (Boolean) -> Unit,
    onLauncherChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field
            CustomTextField(
                value = searchTerm,
                onValueChange = onSearchChange,
                placeholder = "Search activities or packages...",
                modifier = Modifier.weight(1f)
            )

            // Launchable checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = launchableChecked,
                    onClick = { onLaunchableChange(!launchableChecked) }
                )
            ) {
                Checkbox(
                    checked = launchableChecked,
                    onCheckedChange = onLaunchableChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onBackground,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    "Launchable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Debuggable checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = debuggableChecked,
                    onClick = { onDebuggableChange(!debuggableChecked) }
                )
            ) {
                Checkbox(
                    checked = debuggableChecked,
                    onCheckedChange = onDebuggableChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onBackground,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    "Debuggable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Launcher checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = launcherChecked,
                    onClick = { onLauncherChange(!launcherChecked) }
                )
            ) {
                Checkbox(
                    checked = launcherChecked,
                    onCheckedChange = onLauncherChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onBackground,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    "Launcher",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActivitiesScrollableList(
    items: List<ActivityInfo>,
    filter: String,
    launchableFilter: Boolean,
    debuggableFilter: Boolean,
    launcherFilter: Boolean,
    onAction: OnAction
) {
    Card(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                val flattenedItems = items
                    .filter { activityInfo ->
                        val matchesSearch = filter.length < 3 ||
                                activityInfo.packageName.contains(filter, ignoreCase = true) ||
                                activityInfo.fullPath.contains(filter, ignoreCase = true)

                        // For now, all activities are considered launchable and debuggable
                        // These filters can be implemented when ActivityInfo model is extended
                        val matchesLaunchable = !launchableFilter || true
                        val matchesDebuggable = !debuggableFilter || true
                        val matchesLauncher = !launcherFilter || activityInfo.isLauncherCapable

                        matchesSearch && matchesLaunchable && matchesDebuggable && matchesLauncher
                    }
                    .groupBy { it.packageName }
                    .flatMap { group ->
                        listOf(Pair(RowType.Header, group.key)).plus(
                            group.value.map { Pair(RowType.Regular, it) }
                        )
                    }

                items(flattenedItems) { activityPair ->
                    val index = flattenedItems.indexOf(activityPair)
                    val isLastInGroup = index < flattenedItems.size - 1 &&
                            flattenedItems[index + 1].first == RowType.Header

                    when (activityPair.first) {
                        RowType.Header -> {
                            EnhancedHeaderRow(activityPair.second as String)
                        }

                        RowType.Regular -> {
                            val activityInfo = activityPair.second as ActivityInfo
                            EnhancedActivityRow(activityInfo, onAction, showBottomBorder = !isLastInGroup)
                        }
                    }
                }
            }

            androidx.compose.foundation.VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = androidx.compose.foundation.rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}

@Composable
private fun EnhancedHeaderRow(packageName: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = packageName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun EnhancedActivityRow(
    activityInfo: ActivityInfo,
    onAction: OnAction,
    showBottomBorder: Boolean = true
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = activityInfo.activityPath,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CustomActionButton(
                    icon = MaterialIcons.Filled.PlayArrow,
                    contentDescription = "Launch",
                    onClick = { onAction(ActivitiesPlugin.Actions.Launch(activityInfo, false)) }
                )

                CustomActionButton(
                    icon = MaterialIcons.Filled.BugReport,
                    contentDescription = "Debug",
                    onClick = { onAction(ActivitiesPlugin.Actions.Launch(activityInfo, true)) }
                )

                // Set as Launcher button - only show for launcher-capable activities
                if (activityInfo.isLauncherCapable) {
                    CustomActionButton(
                        icon = MaterialIcons.Filled.Home,
                        contentDescription = "Set as Launcher",
                        onClick = { onAction(ActivitiesPlugin.Actions.SetLauncher(activityInfo)) }
                    )
                }

                CustomActionButton(
                    icon = MaterialIcons.Filled.ContentCopy,
                    contentDescription = "Copy command",
                    onClick = {
                        // Copy command functionality - you may need to implement this action
                    }
                )
            }
        }
    }

    if (showBottomBorder) {
        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CustomActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> Color.Transparent
    }

    val iconTint = when {
        isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = iconTint
        )
    }
}

