package ui

import androidx.compose.animation.core.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import core.CommandStatus

@Composable
fun CommandStatus(status: CommandStatus) {
    val infiniteTransition = rememberInfiniteTransition()

    if (status == CommandStatus.Running) {
        val degrees by infiniteTransition.animateValue(
            0f, 360f, Float.VectorConverter, infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        Icon(
            Icons.Filled.Settings,
            "",
            tint = MaterialTheme.colors.onPrimary,
            modifier = Modifier.rotate(degrees)
        )
    } else if (status == CommandStatus.Completed) {
        Icon(
            Icons.Filled.Check,
            "",
            tint = MaterialTheme.colors.onPrimary
        )
    }
}
