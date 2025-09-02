package ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import ui.definitions.Dimens
import ui.definitions.Texts

@Composable
fun ActionButton(
    painter: Painter,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        painter = painter,
        contentDescription = Texts.EMPTY,
        modifier = Modifier
            .size(Dimens.SQUARE_BUTTON_SIZE_REGULAR.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .padding(Dimens.SQUARE_BUTTON_PADDING.dp),
    )
}
