package plugins.permissions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import core.command.PermissionFlag
import ui.dangerColor
import ui.emphasizedDangerColor
import ui.emphasizedInfoColor
import ui.emphasizedSuccessColor
import ui.emphasizedWarningColor
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
fun getPermissionBorderColor(permissionFlag: PermissionFlag): Color = when (permissionFlag) {
    PermissionFlag.NORMAL -> MaterialTheme.colorScheme.emphasizedSuccessColor // Enhanced green
    PermissionFlag.DANGEROUS -> MaterialTheme.colorScheme.emphasizedDangerColor // Enhanced red
    PermissionFlag.SIGNATURE -> MaterialTheme.colorScheme.emphasizedInfoColor // Enhanced blue
    PermissionFlag.PRIVILEGED -> MaterialTheme.colorScheme.emphasizedWarningColor // Enhanced orange
    PermissionFlag.INSTANT -> MaterialTheme.colorScheme.tertiary
    PermissionFlag.RUNTIME -> MaterialTheme.colorScheme.primary // Primary purple
    else -> MaterialTheme.colorScheme.mutedColor // Muted gray
}