package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ui.definitions.Dimens
import ui.definitions.Icons
import ui.definitions.Texts

@Composable
fun CopyButton(
    visible: Boolean,
    onHover: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enterAnimation: EnterTransition = slideInHorizontally(),
    exitAnimation: ExitTransition = slideOutHorizontally(),
) {
    AnimatedVisibility(
        visible,
        enter = enterAnimation,
        exit = exitAnimation,
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(start = Dimens.ROW_HORIZONTAL_MARGIN.dp)
                    .onHover(onHover)
                    .clip(CircleShape),
        ) {
            Icon(
                painterResource(Icons.COPY),
                contentDescription = Texts.EMPTY,
                modifier =
                    Modifier
                        .clickable { onClick() }
                        .withIconStyle(),
            )
        }
    }
}
