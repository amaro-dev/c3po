package core

import models.ActivityInfo
import models.AdbDevice
import models.AppPackage

data class AppState (
    val devices: List<AdbDevice> = emptyList(),
    val currentDevice: AdbDevice? = null,
    val packages: List<AppPackage> = emptyList(),
    val currentTab: Int = 0,
    val activities: List<ActivityInfo> = emptyList()
)
