package plugins.packages.definition

enum class AppTypeFilter {
    ALL, SYSTEM_ONLY, USER_ONLY
}

data class PackagesFilterState(
    val appType: AppTypeFilter = AppTypeFilter.ALL,
    val showOnlyEnabled: Boolean = false,
    val showOnlyDebuggable: Boolean = false,
    val showOnlyWithSignature: Boolean = false,
) {
    fun toMap(): Map<String, Any> = mapOf(
        PackagesFilters.APP_TYPE to appType.name,
        PackagesFilters.SHOW_ONLY_ENABLED to showOnlyEnabled,
        PackagesFilters.SHOW_ONLY_DEBUGGABLE to showOnlyDebuggable,
        PackagesFilters.SHOW_ONLY_WITH_SIGNATURE to showOnlyWithSignature,
    )

    companion object {
        fun fromMap(map: Map<String, Any>): PackagesFilterState {
            val appTypeString = map[PackagesFilters.APP_TYPE] as? String ?: AppTypeFilter.ALL.name
            val appType = try {
                AppTypeFilter.valueOf(appTypeString)
            } catch (e: IllegalArgumentException) {
                AppTypeFilter.ALL
            }

            return PackagesFilterState(
                appType = appType,
                showOnlyEnabled = map[PackagesFilters.SHOW_ONLY_ENABLED] as? Boolean ?: false,
                showOnlyDebuggable = map[PackagesFilters.SHOW_ONLY_DEBUGGABLE] as? Boolean ?: false,
                showOnlyWithSignature = map[PackagesFilters.SHOW_ONLY_WITH_SIGNATURE] as? Boolean ?: false,
            )
        }
    }
}

object PackagesFilters {
    const val APP_TYPE = "appType"
    const val SHOW_ONLY_ENABLED = "showOnlyEnabled"
    const val SHOW_ONLY_DEBUGGABLE = "showOnlyDebuggable"
    const val SHOW_ONLY_WITH_SIGNATURE = "showOnlyWithSignature"
}