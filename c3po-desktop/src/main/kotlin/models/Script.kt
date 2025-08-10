package models

data class Script(
    val name: String,
    val version: String = "1.0",
    val formatVersion: String = "1.0",
    val steps: List<ScriptStep> = emptyList(),
)

sealed class ScriptStep {
    abstract val type: String

    data class InstallApk(
        val apkPath: String,
    ) : ScriptStep() {
        override val type: String = "install_apk"
    }

    data class RemovePackage(
        val packageName: String,
    ) : ScriptStep() {
        override val type: String = "remove_package"
    }

    data class StartActivity(
        val packageName: String,
        val activityName: String,
    ) : ScriptStep() {
        override val type: String = "start_activity"
    }

    data class ClearData(
        val packageName: String,
    ) : ScriptStep() {
        override val type: String = "clear_data"
    }
}

data class ScriptExecutionState(
    val isRunning: Boolean = false,
    val currentStepIndex: Int = -1,
    val executionLogs: List<String> = emptyList(),
    val hasError: Boolean = false,
    val errorMessage: String? = null,
)

enum class ScriptStepType(
    val displayName: String,
) {
    INSTALL_APK("Install APK"),
    REMOVE_PACKAGE("Remove Package"),
    START_ACTIVITY("Start Activity"),
    CLEAR_DATA("Clear Data"),
}
