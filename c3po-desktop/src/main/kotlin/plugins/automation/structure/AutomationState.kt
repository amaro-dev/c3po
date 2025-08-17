package plugins.automation.structure

data class AutomationState(
    val isCreatingScript: Boolean = false,
    val currentScript: Script? = null,
    val availableScripts: List<String> = emptyList(),
    val editingStepIndex: Int? = null,
    val availablePackages: List<core.model.AppPackage> = emptyList(),
    val availableActivities: List<core.model.ActivityInfo> = emptyList(),
    val showPackageSelector: Boolean = false,
    val showActivitySelector: Boolean = false,
    val showApkPicker: Boolean = false,
    val showOpenScriptPicker: Boolean = false,
    val openScriptError: String? = null,
    val malformedScriptFolderPath: String? = null,
    // UC3 - running
    val isRunning: Boolean = false,
    val runningStepIndex: Int = -1,
    val runLogs: List<String> = emptyList(),
    val currentScriptFolder: String? = null,
)