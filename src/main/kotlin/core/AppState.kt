package core

import models.ActivityInfo
import models.AdbDevice
import models.AppPackage

data class AppState (
    val devices: List<AdbDevice> = emptyList(),
    val currentDevice: AdbDevice? = null,
    val packages: List<AppPackage> = emptyList(),
    val currentPlugin: String? = null,
    val activities: List<ActivityInfo> = emptyList(),
    val windows: Map<String, Any> = emptyMap()
)
