package ui


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import commands.AdbCommand
import commands.ClearDataCommand
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
        ActionButton (painterResource("ic_delete_app.svg")) {
            currentDevice?.run {
                UninstallAppCommand(this, appPackage.packageName).run()
            }
        }
        ActionButton (painterResource("ic_close_app.svg")) {
            currentDevice?.run {
                StopAppCommand(this, appPackage.packageName).run()
            }
        }
        ActionButton (painterResource("ic_wipe_app.svg")) {
            currentDevice?.run {
                ClearDataCommand(this, appPackage).run()
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(appPackage.packageName)
    }
}

@Composable
fun ActionButton(
    painter: Painter,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(painter, "")
    }
}