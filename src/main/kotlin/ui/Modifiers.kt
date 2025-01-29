package ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.definitions.Dimens

fun Modifier.baselinePadding() = padding(
    top = Dimens.ROW_VERTICAL_MARGIN.dp,
    bottom = Dimens.ROW_VERTICAL_MARGIN_BOTTOM.dp
)

fun Modifier.horizontalPadding() = padding(
    start = Dimens.ROW_HORIZONTAL_MARGIN.dp,
    end = Dimens.ROW_HORIZONTAL_MARGIN.dp
)
