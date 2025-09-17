package core.model

data class ActivityInfo(
    val packageName: String,
    val activityPath: String,
    val isLauncherCapable: Boolean = false,
) {
    val fullPath: String = "$packageName/$activityPath"
}
