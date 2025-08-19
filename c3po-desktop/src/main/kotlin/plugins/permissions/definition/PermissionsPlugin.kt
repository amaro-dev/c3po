package plugins.permissions.definition

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import core.command.CommandExecutor
import core.command.PermissionFlag
import core.model.Action
import core.model.AppState
import core.model.DeclaredPermissions
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.permissions.PermissionStamp
import plugins.permissions.structure.PermissionsPluginMiddleware
import ui.OnAction
import ui.component.CompactFilterCheckbox
import ui.component.CustomActionButton
import ui.component.CustomTextField
import ui.component.EnhancedHeaderRow
import ui.rows.RowType

class PermissionsPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<DeclaredPermissions> {
    sealed interface Actions : IAction {
        data object List : Actions, Action.CommandAction
    }

    override val id: String = "PERMISSIONS"
    override val name: String = "Permissions"
    override val icon: ImageVector = Icons.Filled.Security

    override val middleware: IMiddleware<AppState> = PermissionsPluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<DeclaredPermissions>,
        onAction: OnAction,
    ) {
        val items: List<DeclaredPermissions> = result.result
        val filter = result.searchTerm

        // Local filter state for permission flags - all checked by default
        var normalFilter by remember { mutableStateOf(true) }
        var dangerousFilter by remember { mutableStateOf(true) }
        var privilegedFilter by remember { mutableStateOf(true) }
        var instantFilter by remember { mutableStateOf(true) }
        var signatureFilter by remember { mutableStateOf(true) }
        var runtimeFilter by remember { mutableStateOf(true) }
        var othersFilter by remember { mutableStateOf(true) }

        Column(modifier = Modifier.fillMaxSize()) {
            // Enhanced search bar with permission filters
            PermissionsSearchBar(
                searchTerm = filter,
                onSearchChange = { onAction(Action.ChangeFilter(id, it)) },
                normalFilter = normalFilter,
                dangerousFilter = dangerousFilter,
                privilegedFilter = privilegedFilter,
                instantFilter = instantFilter,
                signatureFilter = signatureFilter,
                runtimeFilter = runtimeFilter,
                othersFilter = othersFilter,
                onNormalChange = { normalFilter = it },
                onDangerousChange = { dangerousFilter = it },
                onPrivilegedChange = { privilegedFilter = it },
                onInstantChange = { instantFilter = it },
                onSignatureChange = { signatureFilter = it },
                onRuntimeChange = { runtimeFilter = it },
                onOthersChange = { othersFilter = it }
            )

            // Permissions list
            PermissionsScrollableList(
                items = items,
                filter = filter,
                normalFilter = normalFilter,
                dangerousFilter = dangerousFilter,
                privilegedFilter = privilegedFilter,
                instantFilter = instantFilter,
                signatureFilter = signatureFilter,
                runtimeFilter = runtimeFilter,
                othersFilter = othersFilter,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun PermissionsSearchBar(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    normalFilter: Boolean,
    dangerousFilter: Boolean,
    privilegedFilter: Boolean,
    instantFilter: Boolean,
    signatureFilter: Boolean,
    runtimeFilter: Boolean,
    othersFilter: Boolean,
    onNormalChange: (Boolean) -> Unit,
    onDangerousChange: (Boolean) -> Unit,
    onPrivilegedChange: (Boolean) -> Unit,
    onInstantChange: (Boolean) -> Unit,
    onSignatureChange: (Boolean) -> Unit,
    onRuntimeChange: (Boolean) -> Unit,
    onOthersChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            CustomTextField(
                value = searchTerm,
                onValueChange = onSearchChange,
                placeholder = "Search permissions...",
                modifier = Modifier.fillMaxWidth()
            )

            // Filter checkboxes - all in one row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactFilterCheckbox(
                    label = "Normal",
                    checked = normalFilter,
                    onCheckedChange = onNormalChange
                )

                CompactFilterCheckbox(
                    label = "Dangerous",
                    checked = dangerousFilter,
                    onCheckedChange = onDangerousChange
                )

                CompactFilterCheckbox(
                    label = "Privileged",
                    checked = privilegedFilter,
                    onCheckedChange = onPrivilegedChange
                )

                CompactFilterCheckbox(
                    label = "Instant",
                    checked = instantFilter,
                    onCheckedChange = onInstantChange
                )

                CompactFilterCheckbox(
                    label = "Signature",
                    checked = signatureFilter,
                    onCheckedChange = onSignatureChange
                )

                CompactFilterCheckbox(
                    label = "Runtime",
                    checked = runtimeFilter,
                    onCheckedChange = onRuntimeChange
                )

                CompactFilterCheckbox(
                    label = "Others",
                    checked = othersFilter,
                    onCheckedChange = onOthersChange
                )
            }
        }
    }
}

@Composable
private fun PermissionsScrollableList(
    items: List<DeclaredPermissions>,
    filter: String,
    normalFilter: Boolean,
    dangerousFilter: Boolean,
    privilegedFilter: Boolean,
    instantFilter: Boolean,
    signatureFilter: Boolean,
    runtimeFilter: Boolean,
    othersFilter: Boolean,
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
                // Apply all filters
                val filteredItems = items
                    .map { declaredPermissions ->
                        // Filter permissions within each app based on flag filters
                        val filteredPermissions = declaredPermissions.permissions
                            .filter { (permissionName, flags) ->
                                // Apply search filter at permission level
                                val matchesPermissionSearch = filter.length < 3 ||
                                        permissionName.contains(filter, ignoreCase = true) ||
                                        declaredPermissions.ownerApp.contains(filter, ignoreCase = true)

                                // Apply flag filters - only show permissions matching checked filters
                                val hasNormal = flags.contains(PermissionFlag.NORMAL)
                                val hasDangerous = flags.contains(PermissionFlag.DANGEROUS)
                                val hasPrivileged = flags.contains(PermissionFlag.PRIVILEGED)
                                val hasInstant = flags.contains(PermissionFlag.INSTANT)
                                val hasSignature = flags.contains(PermissionFlag.SIGNATURE)
                                val hasRuntime = flags.contains(PermissionFlag.RUNTIME)

                                // Others includes all flags that don't have specific checkboxes
                                val specificFlags = setOf(
                                    PermissionFlag.NORMAL,
                                    PermissionFlag.DANGEROUS,
                                    PermissionFlag.PRIVILEGED,
                                    PermissionFlag.INSTANT,
                                    PermissionFlag.SIGNATURE,
                                    PermissionFlag.RUNTIME
                                )
                                val hasOthers = flags.any { flag -> flag !in specificFlags }

                                val matchesFlagFilters = (normalFilter && hasNormal) ||
                                        (dangerousFilter && hasDangerous) ||
                                        (privilegedFilter && hasPrivileged) ||
                                        (instantFilter && hasInstant) ||
                                        (signatureFilter && hasSignature) ||
                                        (runtimeFilter && hasRuntime) ||
                                        (othersFilter && hasOthers)

                                matchesPermissionSearch && matchesFlagFilters
                            }

                        declaredPermissions.copy(permissions = filteredPermissions)
                    }
                    .filter { it.permissions.isNotEmpty() } // Only keep apps that have matching permissions

                // Flatten into display items
                val flattenedItems = filteredItems.flatMap { appPermissions ->
                    listOf(Pair(RowType.Header, appPermissions.ownerApp)).plus(
                        appPermissions.permissions.map { permission ->
                            Pair(RowType.Regular, permission)
                        }
                    )
                }

                items(flattenedItems) { item ->
                    val index = flattenedItems.indexOf(item)
                    val isLastInGroup = index < flattenedItems.size - 1 &&
                            flattenedItems[index + 1].first == RowType.Header

                    when (item.first) {
                        RowType.Header -> {
                            EnhancedHeaderRow(item.second as String)
                        }

                        RowType.Regular -> {
                            @Suppress("UNCHECKED_CAST")
                            val permission = item.second as Map.Entry<String, List<PermissionFlag>>
                            EnhancedPermissionRow(permission, onAction, showBottomBorder = !isLastInGroup)
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
private fun EnhancedPermissionRow(
    permission: Map.Entry<String, List<PermissionFlag>>,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permission.key,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (permission.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        permission.value.sortedByDescending { it.isBase }.forEach { flag ->
                            PermissionStamp(flag)
                        }
                    }
                }
            }

            CustomActionButton(
                icon = Icons.Filled.ContentCopy,
                contentDescription = "Copy permission name",
                onClick = { onAction(Action.CopyText(permission.key)) }
            )
        }
    }

    if (showBottomBorder) {
        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}