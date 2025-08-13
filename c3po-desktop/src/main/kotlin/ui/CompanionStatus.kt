package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import core.CompanionState
import ui.definitions.Texts

@Composable
fun CompanionStatus(companionState: CompanionState) {
    val (icon, description, color) = when {
        // Connected
        companionState.isOnline() -> Triple(
            Icons.Filled.Link,
            Texts.CONNECTED_TO_COMPANION,
            MaterialTheme.colorScheme.primary
        )
        // Installing
        companionState.shouldOffer() -> Triple(
            Icons.Filled.CloudDownload,
            "Installing Companion...",
            MaterialTheme.colorScheme.secondary
        )
        // Not available
        !companionState.has(CompanionState.CHECKED_FOR_PRESENCE) -> Triple(
            Icons.Filled.Warning,
            "Companion Not Available",
            Color(0xFFE07A5F)
        )
        // Disconnected
        else -> Triple(
            Icons.Filled.LinkOff,
            Texts.DISCONNECTED_FROM_COMPANION,
            Color(0xFFE07A5F)
        )
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = color,
        modifier = Modifier.size(28.dp),
    )
}

@Preview
@Composable
fun CompanionStatusPreview() {
    AndroidGreenTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Connected
            CompanionStatus(core.CompanionState(core.CompanionState.ONLINE or core.CompanionState.INSTALLED or core.CompanionState.CHECKED_FOR_PRESENCE))
            // Installing
            CompanionStatus(core.CompanionState(core.CompanionState.CHECKED_FOR_PRESENCE))
            // Not available
            CompanionStatus(core.CompanionState(0))
            // Disconnected
            CompanionStatus(core.CompanionState(core.CompanionState.CHECKED_FOR_PRESENCE or core.CompanionState.INSTALLED))
        }
    }
}
