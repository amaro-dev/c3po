package plugins.permissions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import core.command.PermissionFlag
import ui.dangerColor
import ui.infoColor
import ui.mutedColor
import ui.onDangerColor
import ui.onInfoColor
import ui.onMutedColor
import ui.onSuccessColor
import ui.onWarningColor
import ui.successColor
import ui.warningColor

@Composable
fun PermissionFlag.getPermissionForegroundColor(): Color = when (this) {
    PermissionFlag.NORMAL -> MaterialTheme.colorScheme.onSuccessColor
    PermissionFlag.DANGEROUS -> MaterialTheme.colorScheme.onDangerColor
    PermissionFlag.SIGNATURE -> MaterialTheme.colorScheme.onInfoColor
    PermissionFlag.PRIVILEGED -> MaterialTheme.colorScheme.onWarningColor
    PermissionFlag.INSTANT -> MaterialTheme.colorScheme.onTertiary
    PermissionFlag.RUNTIME -> MaterialTheme.colorScheme.onPrimary
    // All other flags (Others category)
    else -> MaterialTheme.colorScheme.onMutedColor
}

@Composable
fun PermissionFlag.getPermissionBackgroundColor(): Color = when (this) {
    PermissionFlag.NORMAL -> MaterialTheme.colorScheme.successColor // Green
    PermissionFlag.DANGEROUS -> MaterialTheme.colorScheme.dangerColor // Red
    PermissionFlag.SIGNATURE -> MaterialTheme.colorScheme.infoColor // Blue
    PermissionFlag.PRIVILEGED -> MaterialTheme.colorScheme.warningColor // Orange
    PermissionFlag.INSTANT -> MaterialTheme.colorScheme.tertiary
    PermissionFlag.RUNTIME -> MaterialTheme.colorScheme.primary // Purple
    // All other flags (Others category) - gray/neutral color
    else -> MaterialTheme.colorScheme.mutedColor // Gray
}

@Composable
fun PermissionFlag.getPermissionBorderColor(): Color = when (this) {
    PermissionFlag.NORMAL -> MaterialTheme.colorScheme.successColor.copy(alpha = 0.8f) // Darker green
    PermissionFlag.DANGEROUS -> MaterialTheme.colorScheme.dangerColor.copy(alpha = 0.8f) // Darker red
    PermissionFlag.SIGNATURE -> MaterialTheme.colorScheme.infoColor.copy(alpha = 0.8f) // Darker blue
    PermissionFlag.PRIVILEGED -> MaterialTheme.colorScheme.warningColor.copy(alpha = 0.8f) // Darker orange
    PermissionFlag.INSTANT -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
    PermissionFlag.RUNTIME -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Darker purple
    // All other flags (Others category) - darker gray
    else -> MaterialTheme.colorScheme.mutedColor.copy(alpha = 0.8f) // Darker gray
}