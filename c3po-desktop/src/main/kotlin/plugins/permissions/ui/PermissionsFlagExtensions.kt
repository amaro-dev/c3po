package plugins.permissions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import core.command.PermissionFlag

@Composable
fun PermissionFlag.getPermissionForegroundColor(): Color = when (this) {
    PermissionFlag.NORMAL -> Color.White
    PermissionFlag.DANGEROUS -> Color.White
    PermissionFlag.SIGNATURE -> Color.White
    PermissionFlag.PRIVILEGED -> Color.White
    PermissionFlag.INSTANT -> MaterialTheme.colorScheme.onTertiary
    PermissionFlag.RUNTIME -> Color.White
    // All other flags (Others category)
    else -> Color.White
}

@Composable
fun PermissionFlag.getPermissionBackgroundColor(): Color = when (this) {
    PermissionFlag.NORMAL -> Color(0xFF38A169) // Green
    PermissionFlag.DANGEROUS -> Color(0xFFE53E3E) // Red
    PermissionFlag.SIGNATURE -> Color(0xFF3182CE) // Blue
    PermissionFlag.PRIVILEGED -> Color(0xFFD69E2E) // Orange
    PermissionFlag.INSTANT -> MaterialTheme.colorScheme.tertiary
    PermissionFlag.RUNTIME -> Color(0xFF9333EA) // Purple
    // All other flags (Others category) - gray/neutral color
    else -> Color(0xFF6B7280) // Gray
}

@Composable
fun PermissionFlag.getPermissionBorderColor(): Color = when (this) {
    PermissionFlag.NORMAL -> Color(0xFF2F855A) // Darker green
    PermissionFlag.DANGEROUS -> Color(0xFFC53030) // Darker red
    PermissionFlag.SIGNATURE -> Color(0xFF2C5282) // Darker blue
    PermissionFlag.PRIVILEGED -> Color(0xFFB7791F) // Darker orange
    PermissionFlag.INSTANT -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
    PermissionFlag.RUNTIME -> Color(0xFF7C3AED) // Darker purple
    // All other flags (Others category) - darker gray
    else -> Color(0xFF4B5563) // Darker gray
}