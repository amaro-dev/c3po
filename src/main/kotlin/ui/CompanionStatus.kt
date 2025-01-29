package ui

import androidx.compose.foundation.layout.Row
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
    val (icon, description) =
        when {
            companionState.isReady() -> Pair(Icons.CONNECTING, Texts.CONNECTING_TO_COMPANION)
            companionState.isOnline() -> Pair(Icons.ONLINE, Texts.CONNECTED_TO_COMPANION)
            else -> Pair(Icons.OFFLINE, Texts.DISCONNECTED_FROM_COMPANION)
        }
    Row {
        Icon(
            painterResource(icon),
            description,
            tint = MaterialTheme.colors.onPrimary,
            modifier = Modifier.size(Dimens.ICON_SIZE_REGULAR.dp),
        )
    }
}
