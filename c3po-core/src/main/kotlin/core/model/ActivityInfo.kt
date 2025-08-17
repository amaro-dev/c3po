package core.model

data class ActivityInfo(
    val packageName: String,
    val activityPath: String,
) {
    val fullPath: String = "$packageName/$activityPath"
}
