package ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import commands.AdbCommand
import commands.StopAppCommand
import commands.UninstallAppCommand
import models.AdbDevice
import models.AppPackage

@Composable
fun PackageRow(
    appPackage: AppPackage,
    currentDevice: AdbDevice?,
    onAction: (AdbCommand<*>) -> Unit
) {
    Row(Modifier.padding(6.dp,2.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            currentDevice?.run {
                UninstallAppCommand(this, appPackage.packageName).run()
            }
        }) {
            Icon(Icons.Default.Delete, "")
        }
        IconButton(onClick = {
            currentDevice?.run {
                StopAppCommand(this, appPackage.packageName).run()
            }
        }) {
            Icon(Icons.Default.Clear, "")
        }
        Spacer(Modifier.width(8.dp))
        Text(appPackage.packageName)
    }
}
