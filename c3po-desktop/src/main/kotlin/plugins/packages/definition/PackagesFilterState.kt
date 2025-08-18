package plugins.packages.definition

enum class AppTypeFilter {
    ALL, SYSTEM_ONLY, USER_ONLY
}

enum class SleepStateFilter {
    ALL, AWAKE_ONLY, ASLEEP_ONLY, UNKNOWN_ONLY
}

data class PackagesFilterState(
    val appType: AppTypeFilter = AppTypeFilter.ALL,
    val showOnlyEnabled: Boolean = false,
    val showOnlyDebuggable: Boolean = false,
    val showOnlyWithSignature: Boolean = false,
    val sleepStateFilter: SleepStateFilter = SleepStateFilter.ALL,
) {
    fun toMap(): Map<String, Any> = mapOf(
        PackagesFilters.APP_TYPE to appType.name,
        PackagesFilters.SHOW_ONLY_ENABLED to showOnlyEnabled,
        PackagesFilters.SHOW_ONLY_DEBUGGABLE to showOnlyDebuggable,
        PackagesFilters.SHOW_ONLY_WITH_SIGNATURE to showOnlyWithSignature,
        PackagesFilters.SLEEP_STATE_FILTER to sleepStateFilter.name,
    )

    companion object {
        fun fromMap(map: Map<String, Any>): PackagesFilterState {
            val appTypeString = map[PackagesFilters.APP_TYPE] as? String ?: AppTypeFilter.ALL.name
            val appType = try {
                AppTypeFilter.valueOf(appTypeString)
            } catch (e: IllegalArgumentException) {
                AppTypeFilter.ALL
            }

            val sleepStateString = map[PackagesFilters.SLEEP_STATE_FILTER] as? String ?: SleepStateFilter.ALL.name
            val sleepStateFilter = try {
                SleepStateFilter.valueOf(sleepStateString)
            } catch (e: IllegalArgumentException) {
                SleepStateFilter.ALL
            }

            return PackagesFilterState(
                appType = appType,
                showOnlyEnabled = map[PackagesFilters.SHOW_ONLY_ENABLED] as? Boolean ?: false,
                showOnlyDebuggable = map[PackagesFilters.SHOW_ONLY_DEBUGGABLE] as? Boolean ?: false,
                showOnlyWithSignature = map[PackagesFilters.SHOW_ONLY_WITH_SIGNATURE] as? Boolean ?: false,
                sleepStateFilter = sleepStateFilter,
            )
        }
    }
}

object PackagesFilters {
    const val APP_TYPE = "appType"
    const val SHOW_ONLY_ENABLED = "showOnlyEnabled"
    const val SHOW_ONLY_DEBUGGABLE = "showOnlyDebuggable"
    const val SHOW_ONLY_WITH_SIGNATURE = "showOnlyWithSignature"
    const val SLEEP_STATE_FILTER = "sleepStateFilter"
}