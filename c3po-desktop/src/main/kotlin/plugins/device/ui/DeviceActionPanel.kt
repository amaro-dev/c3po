package plugins.device.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
 * Compact device action buttons designed to fit near the Disk Usage panel.
 * Uses icons with text labels below, no card wrapper for minimal footprint.
 */
@Composable
fun DeviceActionPanel(
    onAction: OnAction,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
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

        // Future action buttons will be added here
        // DeviceActionButton(
        //     icon = {
        //         Icon(
        //             imageVector = Icons.Default.Refresh,
        //             contentDescription = "Refresh",
        //             modifier = Modifier.size(20.dp)
        //         )
        //     },
        //     label = "Refresh",
        //     onClick = { onAction(Action.RefreshDevice) }
        // )
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