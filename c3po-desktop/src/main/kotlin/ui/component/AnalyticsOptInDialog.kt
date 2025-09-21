package ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.secondaryTextColor

@Composable
fun AnalyticsOptInDialog(
    onEnable: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    StandardDialog(
        title = "Help Improve C3PO",
        onDismiss = onDismiss,
        primaryAction = DialogAction(
            text = "Enable",
            onClick = onEnable,
            isPrimary = true
        ),
        secondaryAction = DialogAction(
            text = "No thanks",
            onClick = onDecline
        )
    ) {
        Column {
            Text(
                text = "Allow anonymous usage analytics to help us prioritize features and improve stability.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "We do not collect personal data, ADB paths, device serials, or file contents.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondaryTextColor
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You can change this later in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondaryTextColor
            )
        }
    }
}

