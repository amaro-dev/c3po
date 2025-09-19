package plugins.packages.definition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import core.command.CommandExecutor
import core.model.Action
import core.model.Action.CommandAction
import core.model.AppPackage
import core.model.AppState
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.packages.structure.PackagesPluginMiddleware
import plugins.packages.ui.component.EnhancedPackageRow
import ui.OnAction
import ui.component.CompactFilterCheckbox
import ui.component.CompactSelector
import ui.parts.EnhancedScrollableList
import ui.parts.EnhancedSearchBar

class PackagesPlugin(
    executor: CommandExecutor,
    apkSignatureExtractor: core.facade.ApkSignatureExtractor,
) : plugins.Plugin<AppPackage> {
    companion object {
        const val EXTRACT_KEY_INSTRUCTION = "keystore-info"
        const val CHECK_ASLEEP_INSTRUCTION = "sleep-state"
    }

    sealed interface Actions : IAction {
        data object List : Actions, CommandAction

        data class Stop(
            val packageInfo: AppPackage,
        ) : Actions,
            CommandAction

        data class Uninstall(
            val packageInfo: AppPackage,
        ) : Actions,
            CommandAction

        data class ExtractKey(
            val packageInfo: AppPackage,
        ) : Actions,
            CommandAction

        data class ClearData(
            val packageInfo: AppPackage,
        ) : Actions,
            CommandAction

        data class CheckAsleep(
            val packageInfo: AppPackage,
        ) : Actions, CommandAction

        data object LoadAllSleepStates : Actions
    }

    override val icon: ImageVector = Icons.Filled.Inventory2

    override val name: String = "Packages"

    override val id: String = "PACKAGES"

    override val middleware: IMiddleware<AppState> = PackagesPluginMiddleware(id, executor, apkSignatureExtractor)

    override fun isResponsibleFor(action: IAction): Boolean =
        action is Actions

    @Composable
    override fun present(
        result: WindowResult<AppPackage>,
        onAction: OnAction,
    ) {
        val items: List<AppPackage> = result.result
        val filter = result.searchTerm

        // Local filter state with persistence to global state
        var filters by remember(result.filterState) {
            mutableStateOf(PackagesFilterState.fromMap(result.filterState))
        }

        // Persist filter changes to global state
        LaunchedEffect(filters) {
            onAction(Action.UpdatePluginFilters(id, filters.toMap()))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            EnhancedSearchBar(
                searchTerm = filter,
                onSearchChange = { onAction(Action.ChangeFilter(id, it)) },
                filterState = filters,
                onFilterChange = { filters = it },
                searchPlaceholder = "Search packages..."
            ) { currentFilters, updateFilters ->
                // Package-specific filter controls
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // App type selector
                    CompactSelector(
                        label = "Type",
                        options = listOf(AppTypeFilter.ALL, AppTypeFilter.SYSTEM_ONLY, AppTypeFilter.USER_ONLY),
                        selectedOption = currentFilters.appType,
                        onSelectionChange = { updateFilters(currentFilters.copy(appType = it)) },
                        optionText = { appType ->
                            when (appType) {
                                AppTypeFilter.ALL -> "All"
                                AppTypeFilter.SYSTEM_ONLY -> "System"
                                AppTypeFilter.USER_ONLY -> "User"
                            }
                        }
                    )

                    // Sleep state selector
                    CompactSelector(
                        label = "Sleep State",
                        options = listOf(
                            SleepStateFilter.ALL,
                            SleepStateFilter.AWAKE_ONLY,
                            SleepStateFilter.ASLEEP_ONLY,
                            SleepStateFilter.UNKNOWN_ONLY
                        ),
                        selectedOption = currentFilters.sleepStateFilter,
                        onSelectionChange = { updateFilters(currentFilters.copy(sleepStateFilter = it)) },
                        optionText = { sleepState ->
                            when (sleepState) {
                                SleepStateFilter.ALL -> "All"
                                SleepStateFilter.AWAKE_ONLY -> "Awake"
                                SleepStateFilter.ASLEEP_ONLY -> "Asleep"
                                SleepStateFilter.UNKNOWN_ONLY -> "Unknown"
                            }
                        }
                    )

                    // Filter checkboxes: when checked, show only matching items
                    CompactFilterCheckbox(
                        label = "Only Enabled",
                        checked = currentFilters.showOnlyEnabled,
                        onCheckedChange = { updateFilters(currentFilters.copy(showOnlyEnabled = it)) }
                    )
                    CompactFilterCheckbox(
                        label = "Only Debuggable",
                        checked = currentFilters.showOnlyDebuggable,
                        onCheckedChange = { updateFilters(currentFilters.copy(showOnlyDebuggable = it)) }
                    )
                }
            }

            // Filter packages based on search term and filter state, then sort alphabetically
            val filteredPackages = filterPackages(items, filter, filters)
                .sortedBy { it.packageName }

            EnhancedScrollableList(
                items = filteredPackages,
                itemContent = { packageInfo, showBottomBorder ->
                    EnhancedPackageRow(
                        packageInfo = packageInfo,
                        onAction = onAction,
                        showBottomBorder = showBottomBorder
                    )
                }
            )
        }
    }
}
