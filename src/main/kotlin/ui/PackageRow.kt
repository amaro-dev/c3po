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
import core.Action
import models.AppPackage

@Composable
fun PackageRow(
    appPackage: AppPackage,
    onAction: (Action) -> Unit
) {
    Row(Modifier.padding(6.dp,2.dp), verticalAlignment = Alignment.CenterVertically) {
        ActionButton (painterResource("ic_delete_app.svg")) {
            onAction(Action.UninstallApp(appPackage))
        }
        ActionButton (painterResource("ic_close_app.svg")) {
            onAction(Action.StopApp(appPackage))
        }
        ActionButton (painterResource("ic_wipe_app.svg")) {
            onAction(Action.ClearAppData(appPackage))
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
