package plugins.broadcasts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import core.model.Action
import core.model.BroadcastAction
import plugins.broadcasts.definition.BroadcastsPlugin
import plugins.broadcasts.definition.GroupingMode
import ui.OnAction

@Composable
fun BroadcastActionRow(
    broadcastAction: BroadcastAction,
    groupingMode: GroupingMode,
    onAction: OnAction,
    onSendWithExtras: () -> Unit = {},
    showBottomBorder: Boolean = true
) {
    // Generate ADB command for copying
    val adbCommand = "adb shell am broadcast -a \"${broadcastAction.action}\""

    // Determine what to show based on grouping mode
    val primaryText = when (groupingMode) {
        GroupingMode.BY_PACKAGE -> broadcastAction.action     // Show action when grouped by package
        GroupingMode.BY_ACTION -> broadcastAction.packageName // Show package when grouped by action
    }

    val secondaryText = broadcastAction.requiredPermission // Always show permission as secondary

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Broadcast info
            Column(modifier = Modifier.weight(1f)) {
                // Primary text (action or package depending on grouping)
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Secondary text (permission if available)
                secondaryText?.let { permission ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Required permission",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                        Text(
                            text = permission,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Right side: Action buttons (always visible)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BroadcastActionButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send broadcast",
                    onClick = { onAction(BroadcastsPlugin.Actions.Send(broadcastAction)) }
                )

                BroadcastActionButton(
                    icon = Icons.Filled.Settings,
                    contentDescription = "Send with extras",
                    onClick = onSendWithExtras
                )

                BroadcastActionButton(
                    icon = Icons.Filled.ContentCopy,
                    contentDescription = "Copy ADB command",
                    onClick = { onAction(Action.CopyText(adbCommand)) }
                )
            }
        }
    }

    if (showBottomBorder) {
        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BroadcastActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> Color.Transparent
    }

    val iconTint = when {
        isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = iconTint
        )
    }
}