package plugins.automation.data

data class AutomationState(
    val isCreatingScript: Boolean = false,
    val currentScript: Script? = null,
    val availableScripts: List<String> = emptyList(),
    val editingStepIndex: Int? = null,
    val availablePackages: List<models.AppPackage> = emptyList(),
    val availableActivities: List<models.ActivityInfo> = emptyList(),
    val showPackageSelector: Boolean = false,
    val showActivitySelector: Boolean = false,
    val showApkPicker: Boolean = false,
)
