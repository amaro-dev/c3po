package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import models.CommandStatus

@Composable
fun (BoxScope).Feedback(status: CommandStatus, errorMessage: String?, onDiscard: () -> Unit) {
    AnimatedVisibility(
        visible = status == CommandStatus.Failed,
        enter = slideInVertically { (40.dp.value).toInt() } + fadeIn(),
        exit = slideOutVertically { (40.dp.value).toInt() } + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Snackbar(modifier = Modifier.horizontalPadding().baselinePadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(errorMessage ?: status.name)
            }
        }
        LaunchedEffect(status) {
            delay(3000)
            if (status != CommandStatus.Idle) onDiscard()
        }
    }
}
