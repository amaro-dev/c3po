package ui.component

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Data class representing the complete settings state.
 * This encapsulates all settings that can be modified in the SettingsDialog.
 */
data class SettingsState(
    val adbPath: String,
    val updatesUrl: String,
    val darkMode: Boolean,
    val loggingEnabled: Boolean,
    val adbLogging: String,
    val performLogging: Boolean,
    val reduceLogging: Boolean
)

/**
 * Robust state manager for settings that handles local changes and external updates intelligently.
 *
 * Key features:
 * - Tracks local user changes separately from external state
 * - Merges external updates with local changes
 * - Automatically clears local changes when external updates conflict
 * - Prevents loss of user changes during recompositions
 *
 * Usage:
 * ```kotlin
 * val stateManager = remember { SettingsStateManager(initialSettings) }
 *
 * // User makes changes
 * stateManager.updateAdbPath(newPath)
 *
 * // External update comes in
 * LaunchedEffect(externalSettings) {
 *     stateManager.mergeExternalUpdate(externalSettings)
 * }
 * ```
 */
class SettingsStateManager(initialState: SettingsState) {

    private val localChanges = mutableMapOf<String, Any>()
    private val _currentState = mutableStateOf(initialState)
    private var _lastExternalState = initialState

    /**
     * Current state combining external updates with local changes.
     */
    val currentState: State<SettingsState> = _currentState

    // Type-safe update methods for each setting
    fun updateAdbPath(value: String) = updateField("adbPath", value)
    fun updateUpdatesUrl(value: String) = updateField("updatesUrl", value)
    fun updateDarkMode(value: Boolean) = updateField("darkMode", value)
    fun updateLoggingEnabled(value: Boolean) = updateField("loggingEnabled", value)
    fun updateAdbLogging(value: String) = updateField("adbLogging", value)
    fun updatePerformLogging(value: Boolean) = updateField("performLogging", value)
    fun updateReduceLogging(value: Boolean) = updateField("reduceLogging", value)

    /**
     * Updates a field with a local change and rebuilds the state.
     */
    private fun updateField(fieldName: String, value: Any) {
        localChanges[fieldName] = value
        rebuildState()
    }

    /**
     * Manually clear a specific local change.
     * Useful when a user action should override their previous manual change.
     */
    fun clearLocalChange(fieldName: String) {
        localChanges.remove(fieldName)
        rebuildState()
    }

    /**
     * Merge external state update with local changes.
     * Automatically clears local changes for fields that have actually changed externally.
     *
     * @param newExternalState The new state from external source (props, API, etc.)
     */
    fun mergeExternalUpdate(newExternalState: SettingsState) {
        // Detect which fields actually changed externally
        val changedFields = detectChangedFields(_lastExternalState, newExternalState)

        // Clear local changes for fields that changed externally
        // This allows external updates (like search results) to override user changes
        changedFields.forEach { fieldName ->
            localChanges.remove(fieldName)
        }

        // Update our baseline and rebuild state
        _lastExternalState = newExternalState
        rebuildState()
    }

    /**
     * Detect which fields changed between two external states.
     */
    private fun detectChangedFields(oldState: SettingsState, newState: SettingsState): Set<String> {
        val changedFields = mutableSetOf<String>()

        if (oldState.adbPath != newState.adbPath) changedFields.add("adbPath")
        if (oldState.updatesUrl != newState.updatesUrl) changedFields.add("updatesUrl")
        if (oldState.darkMode != newState.darkMode) changedFields.add("darkMode")
        if (oldState.loggingEnabled != newState.loggingEnabled) changedFields.add("loggingEnabled")
        if (oldState.adbLogging != newState.adbLogging) changedFields.add("adbLogging")
        if (oldState.performLogging != newState.performLogging) changedFields.add("performLogging")
        if (oldState.reduceLogging != newState.reduceLogging) changedFields.add("reduceLogging")

        return changedFields
    }

    /**
     * Rebuild the current state by merging external state with local changes.
     */
    private fun rebuildState() {
        _currentState.value = SettingsState(
            adbPath = localChanges["adbPath"] as? String ?: _lastExternalState.adbPath,
            updatesUrl = localChanges["updatesUrl"] as? String ?: _lastExternalState.updatesUrl,
            darkMode = localChanges["darkMode"] as? Boolean ?: _lastExternalState.darkMode,
            loggingEnabled = localChanges["loggingEnabled"] as? Boolean ?: _lastExternalState.loggingEnabled,
            adbLogging = localChanges["adbLogging"] as? String ?: _lastExternalState.adbLogging,
            performLogging = localChanges["performLogging"] as? Boolean ?: _lastExternalState.performLogging,
            reduceLogging = localChanges["reduceLogging"] as? Boolean ?: _lastExternalState.reduceLogging
        )
    }

    /**
     * Get the current state values for saving.
     */
    fun getCurrentValues(): SettingsState = _currentState.value

    /**
     * Check if there are any local changes that haven't been saved.
     */
    fun hasLocalChanges(): Boolean = localChanges.isNotEmpty()

    /**
     * Get information about which fields have local changes.
     * Useful for debugging or showing "unsaved changes" indicators.
     */
    fun getLocalChangedFields(): Set<String> = localChanges.keys.toSet()
}