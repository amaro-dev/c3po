package core.model

data class AppPackage(
    val packageName: String,
    val versionName: String = "",
    val versionCode: Int = -1,
    val targetSdk: Int = -1,
    val signerInfo: SignatureInfo? = null,
    val sleepState: SleepState = SleepState.Unknown,
)

sealed interface SleepState {
    data object Unknown : SleepState

    data object Awake : SleepState

    data object Asleep : SleepState
}
