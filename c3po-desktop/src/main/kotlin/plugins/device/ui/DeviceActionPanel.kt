package plugins.device.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.model.Action
import ui.OnAction

/**
 * Device action panel containing action buttons for device operations.
 * Follows the same card-based design pattern as other panels.
 */
@Composable
fun DeviceActionPanel(
    onAction: OnAction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
            ) {
                DeviceActionButton(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take Screenshot",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Screenshot",
                    onClick = { onAction(Action.TakeScreenshot) }
                )

                DeviceActionButton(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Restart Device",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Restart",
                    onClick = { onAction(Action.ConfirmRestartDevice) }
                )
            }
        }
    }
}

@Composable
private fun DeviceActionButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(36.dp)
        ) {
            icon()
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}