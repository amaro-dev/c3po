package plugins.packages.definition

import core.model.AppPackage
import core.model.SleepState

/**
 * Filter logic for packages based on search term and filter state
 */
fun filterPackages(
    packages: List<AppPackage>,
    searchTerm: String,
    filterState: PackagesFilterState,
): List<AppPackage> {
    return packages.filter { pkg ->
        // Search term filtering
        val matchesSearch = searchTerm.length < 3 ||
                pkg.packageName.contains(searchTerm, ignoreCase = true)

        // App type filtering (All, System only, User only)
        val matchesAppType = when (filterState.appType) {
            AppTypeFilter.ALL -> true
            AppTypeFilter.SYSTEM_ONLY -> pkg.isSystemApp
            AppTypeFilter.USER_ONLY -> !pkg.isSystemApp
        }

        // Filter options: when checked, show only items that match, when unchecked show all
        val matchesEnabledFilter = if (filterState.showOnlyEnabled) pkg.isEnabled else true
        val matchesDebuggableFilter = if (filterState.showOnlyDebuggable) pkg.isDebuggable else true
        val matchesSignatureFilter = if (filterState.showOnlyWithSignature) pkg.signerInfo != null else true

        // Sleep state filtering
        val matchesSleepStateFilter = when (filterState.sleepStateFilter) {
            SleepStateFilter.ALL -> true
            SleepStateFilter.AWAKE_ONLY -> pkg.sleepState == SleepState.Awake
            SleepStateFilter.ASLEEP_ONLY -> pkg.sleepState == SleepState.Asleep
            SleepStateFilter.UNKNOWN_ONLY -> pkg.sleepState == SleepState.Unknown
        }

        // Combine all filters
        matchesSearch &&
                matchesAppType &&
                matchesEnabledFilter &&
                matchesDebuggableFilter &&
                matchesSignatureFilter &&
                matchesSleepStateFilter
    }
}