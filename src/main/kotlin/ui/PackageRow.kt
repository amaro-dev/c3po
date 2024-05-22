package ui


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(0.dp, 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(appPackage.packageName, Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            ActionButton(painterResource("ic_delete_app.svg")) {
                onAction(Action.UninstallApp(appPackage))
            }
            ActionButton(painterResource("ic_close_app.svg")) {
                onAction(Action.StopApp(appPackage))
            }
            ActionButton(painterResource("ic_wipe_app.svg")) {
                onAction(Action.ClearAppData(appPackage))
            }
        }
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onSurface))
    }
}

@Composable
fun ActionButton(
    painter: Painter,
    onClick: () -> Unit
) {
    Icon(
        painter = painter,
        contentDescription = "",
        modifier = Modifier.clickable { onClick() }
            .size(32.dp)
            .padding(6.dp)

    )
}
