package ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.definitions.Dimens

fun Modifier.baselinePadding(bottom: Int = Dimens.ROW_VERTICAL_MARGIN_BOTTOM) = padding(
    top = (bottom * 0.6f).dp,
    bottom = bottom.dp
)

fun Modifier.horizontalPadding() = padding(
    start = Dimens.ROW_HORIZONTAL_MARGIN.dp,
    end = Dimens.ROW_HORIZONTAL_MARGIN.dp
)
