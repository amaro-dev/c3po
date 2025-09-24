package plugins.automation.structure

data class AutomationState(
    val isCreatingScript: Boolean = false,
    val currentScript: Script? = null,
    val isDirty: Boolean = false,
    val availableScripts: List<String> = emptyList(),
    val editingStepIndex: Int? = null,
    val availablePackages: List<core.model.AppPackage> = emptyList(),
    val availableActivities: List<core.model.ActivityInfo> = emptyList(),
    val showPackageSelector: Boolean = false,
    val showActivitySelector: Boolean = false,
    val showApkPicker: Boolean = false,
    val showOpenScriptPicker: Boolean = false,
    val showNameDialog: Boolean = false,
    val isRenameDialog: Boolean = false,
    val openScriptError: String? = null,
    val malformedScriptFolderPath: String? = null,
    val showExportPicker: Boolean = false,
    val pendingExportFilePath: String? = null,
    val showExportOverwriteDialog: Boolean = false,
    val showImportPicker: Boolean = false,
    val showImportConflictDialog: Boolean = false,
    val showImportRenameDialog: Boolean = false,
    val importSuggestedName: String? = null,
    val importConflictExistingName: String? = null,
    val pendingImportFilePath: String? = null,
    val importErrorMessage: String? = null,
    // UC3 - running
    val isRunning: Boolean = false,
    val runningStepIndex: Int = -1,
    val runLogs: List<String> = emptyList(),
    val failedStepIndex: Int = -1, // Track which step failed (-1 means no failure)
    val completedSteps: Set<Int> = emptySet(), // Track which steps have completed successfully
    val currentScriptFolder: String? = null,
)
