package ui.rows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.AndroidGreenTheme
import ui.baselinePadding
import ui.definitions.Dimens
import ui.horizontalPadding

@Composable
fun BaseRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .horizontalPadding()
            .baselinePadding()
            .defaultMinSize(minHeight = Dimens.ROW_HEIGHT_REGULAR.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
@Preview()
private fun previewRow() {
    AndroidGreenTheme {
        Column {
            Spacer(Modifier.height(20.dp))
            Spacer(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(Color.Black),
            )
            BaseRow(Modifier) { Text("Custom Row") }
            Spacer(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(Color.Black),
            )
        }
    }
}
