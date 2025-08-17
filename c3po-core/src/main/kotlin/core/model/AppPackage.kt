package core.model

data class AppPackage(
    val packageName: String,
    val versionName: String = "",
    val versionCode: Int = -1,
    val targetSdk: Int = -1,
    val signerInfo: SignatureInfo? = null,
    val sleepState: SleepState = SleepState.Unknown,
    // New fields for filtering
    val isSystemApp: Boolean = false,
    val isDebuggable: Boolean = false,
    val isEnabled: Boolean = true,
    val installPath: String? = null,
)

sealed interface SleepState {
    data object Unknown : SleepState

    data object Awake : SleepState

    data object Asleep : SleepState
}
