package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import ui.definitions.Dimens
import ui.definitions.Texts

@Composable
fun Favourite(content: @Composable () -> Unit) {
    var state by remember { mutableStateOf(false) }
    val painter = rememberVectorPainter(if (state) Icons.Filled.Favorite else Icons.Outlined.Favorite)
    var isHovering: Boolean by remember { mutableStateOf(false) }
    Box(Modifier.onHover { isHovering = it }) {
        content()
        Icon(
            painter = painter,
            contentDescription = Texts.EMPTY,
            modifier =
                Modifier
                    .size(Dimens.SQUARE_BUTTON_SIZE_REGULAR.dp)
                    .padding(Dimens.SQUARE_BUTTON_PADDING.dp)
                    .onHover { isHovering = it }
                    .popTransition(isHovering)
                    .clickable { state = !state },

            )
    }
}
