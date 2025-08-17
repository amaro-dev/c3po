package plugins.permissions

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import core.command.PermissionFlag
import darkenedBy

@Composable
fun PermissionFlag.getPermissionForegroundColor(): Color =
    if (isBase) {
        MaterialTheme.colors.onPrimary
    } else {
        if (this == PermissionFlag.PRIVILEGED) {
            MaterialTheme.colors.onError
        } else {
            MaterialTheme.colors.onSecondary
        }
    }

@Composable
fun PermissionFlag.getPermissionBackgroundColor(): Color =
    if (isBase) {
        MaterialTheme.colors.primary
    } else {
        if (this == PermissionFlag.PRIVILEGED) {
            MaterialTheme.colors.error
        } else {
            MaterialTheme.colors.secondary
        }
    }

@Composable
fun PermissionFlag.getPermissionBorderColor(): Color =
    if (isBase) {
        MaterialTheme.colors.primaryVariant
    } else {
        if (this == PermissionFlag.PRIVILEGED) {
            MaterialTheme.colors.error.darkenedBy(0.3f)
        } else {
            MaterialTheme.colors.secondaryVariant
        }
    }
