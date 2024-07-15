package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    painter: Painter,
    onClick: () -> Unit
) {
    Icon(
        painter = painter,
        contentDescription = Texts.EMPTY,
        modifier = Modifier.clickable { onClick() }
            .size(Dimens.SQUARE_BUTTON_SIZE_REGULAR.dp)
            .padding(Dimens.SQUARE_BUTTON_PADDING.dp)

    )
}
