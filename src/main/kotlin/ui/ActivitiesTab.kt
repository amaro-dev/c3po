package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.Action
import models.ActivityInfo

enum class RowType {
    Header,
    Regular
}

@Composable
fun PackageHeader(packageName: String) {
    Surface(color = MaterialTheme.colors.primary, shape = MaterialTheme.shapes.small) {
        Box(Modifier.fillMaxWidth().padding(8.dp, 4.dp)) {
            Text(packageName, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun ActivityRow(activity: ActivityInfo, onAction: (Action) -> Unit) {
    Box(
        Modifier
            .clickable { onAction(Action.StartActivity(activity)) }
            .padding(6.dp)
            .fillMaxWidth()
    ) {
        Text(
            activity.activityPath,
            style = MaterialTheme.typography.body2,
        )
    }
}
