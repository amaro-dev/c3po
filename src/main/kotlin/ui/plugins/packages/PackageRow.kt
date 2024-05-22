package ui.plugins.packages


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.amaro.sonic.IAction
import models.AppPackage
import ui.ActionButton

@Composable
fun PackageRow(
    appPackage: AppPackage,
    onAction: (IAction) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(0.dp, 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(appPackage.packageName, Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            ActionButton(painterResource("ic_delete_app.svg")) {
                onAction(PackagesPlugin.Actions.Uninstall(appPackage))
            }
            ActionButton(painterResource("ic_close_app.svg")) {
                onAction(PackagesPlugin.Actions.Stop(appPackage))
            }
            ActionButton(painterResource("ic_wipe_app.svg")) {
                onAction(PackagesPlugin.Actions.ClearData(appPackage))
            }
        }
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onSurface))
    }
}
