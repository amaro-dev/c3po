package ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import core.CompanionState
import ui.definitions.Dimens
import ui.definitions.Icons
import ui.definitions.Texts

@Composable
fun CompanionStatus(companionState: CompanionState) {
    val (icon, description, color) =
        when {
            companionState.isOnline() ->
                Triple(
                    Icons.ONLINE,
                    Texts.CONNECTED_TO_COMPANION,
                    MaterialTheme.colors.primary,
                )

            else -> Triple(Icons.CONNECTING, Texts.DISCONNECTED_FROM_COMPANION, MaterialTheme.colors.onPrimary)
        }

    Icon(
        painterResource(icon),
        description,
        tint = color,
        modifier = Modifier.size(Dimens.ICON_SIZE_REGULAR.dp),
    )
}
