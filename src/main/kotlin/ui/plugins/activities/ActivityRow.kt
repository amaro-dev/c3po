package ui.plugins.activities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.amaro.sonic.IAction
import models.ActivityInfo

@Composable
fun ActivityRow(activity: ActivityInfo, onAction: (IAction) -> Unit) {
    Box(
        Modifier
            .clickable { onAction(ActivitiesPlugin.Actions.Launch(activity)) }
            .padding(6.dp)
            .fillMaxWidth()
    ) {
        Text(
            activity.activityPath,
            style = MaterialTheme.typography.body2,
        )
    }
}
