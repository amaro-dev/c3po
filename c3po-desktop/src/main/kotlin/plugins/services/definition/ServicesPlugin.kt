package plugins.services.definition

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import plugins.services.structure.ServicesPluginMiddleware
import ui.OnAction
import ui.component.CustomActionButton
import ui.component.CustomTextField
import ui.component.EnhancedHeaderRow
import ui.rows.RowType
import androidx.compose.material.icons.Icons as MaterialIcons

class ServicesPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<Pair<String, List<ActivityInfo>>> {
    sealed interface Actions : IAction {
        data object LIST : Actions, CommandAction

        data class Launch(
            val activityInfo: ActivityInfo,
        ) : Actions,
            CommandAction
    }

    override val name: String = "Services"
    override val id: String = "SERVICES"
    override val icon: ImageVector = MaterialIcons.Filled.Build
    override val middleware: IMiddleware<AppState> = ServicesPluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean =
        action is Actions

    @Composable
    override fun present(
        result: WindowResult<Pair<String, List<ActivityInfo>>>,
        onAction: OnAction,
    ) {
        val items: List<Pair<String, List<ActivityInfo>>> = result.result
        val filter = result.searchTerm

        Column(modifier = Modifier.fillMaxSize()) {
            // Custom search bar (without filters since Services don't have many actions)
            ServiceSearchBar(
                searchTerm = filter,
                onSearchChange = { onAction(Action.ChangeFilter(id, it)) }
            )

            // Services list
            ServicesScrollableList(
                items = items,
                filter = filter,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun ServiceSearchBar(
    searchTerm: String,
    onSearchChange: (String) -> Unit
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomTextField(
                value = searchTerm,
                onValueChange = onSearchChange,
                placeholder = "Search services...",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ServicesScrollableList(
    items: List<Pair<String, List<ActivityInfo>>>,
    filter: String,
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
            val listState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                // First flatten all services into individual ActivityInfo items, then filter and group properly
                val allServices = items.flatMap { group ->
                    group.second.map { service -> service }
                }

                // Deduplicate services based on their fullPath (package + activity path)
                val uniqueServices = allServices.distinctBy { it.fullPath }

                val filteredServices = uniqueServices.filter { serviceInfo ->
                    filter.length < 3 ||
                            serviceInfo.packageName.contains(filter, ignoreCase = true) ||
                            serviceInfo.fullPath.contains(filter, ignoreCase = true) ||
                            serviceInfo.activityPath.contains(filter, ignoreCase = true)
                }

                val flattenedItems = filteredServices
                    .groupBy { it.packageName }
                    .flatMap { group ->
                        listOf(Pair(RowType.Header, group.key)).plus(
                            group.value.map { Pair(RowType.Regular, it) }
                        )
                    }

                items(flattenedItems) { servicePair ->
                    val index = flattenedItems.indexOf(servicePair)
                    val isLastInGroup = index < flattenedItems.size - 1 &&
                            flattenedItems[index + 1].first == RowType.Header

                    when (servicePair.first) {
                        RowType.Header -> {
                            EnhancedHeaderRow(servicePair.second as String)
                        }

                        RowType.Regular -> {
                            val serviceInfo = servicePair.second as ActivityInfo
                            EnhancedServiceRow(serviceInfo, onAction, showBottomBorder = !isLastInGroup)
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}

@Composable
private fun EnhancedServiceRow(
    serviceInfo: ActivityInfo,
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
                text = serviceInfo.activityPath,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CustomActionButton(
                    icon = MaterialIcons.Filled.PlayArrow,
                    contentDescription = "Start service",
                    onClick = { onAction(ServicesPlugin.Actions.Launch(serviceInfo)) }
                )

                CustomActionButton(
                    icon = MaterialIcons.Filled.ContentCopy,
                    contentDescription = "Copy command",
                    onClick = {
                        onAction(Action.CopyText("adb shell am start-foreground-service ${serviceInfo.fullPath}"))
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