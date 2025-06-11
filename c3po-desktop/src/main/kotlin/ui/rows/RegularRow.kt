package ui.rows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.AppTheme

@Composable
fun RegularRow(
    content: String,
    modifier: Modifier = Modifier,
) {
    BaseRow(modifier) {
        Text(
            content,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
@Preview()
private fun previewRow() {
    AppTheme {
        Column {
            Spacer(Modifier.height(20.dp))
            Spacer(
                Modifier.height(1.dp)
                    .fillMaxWidth()
                    .background(Color.Black)
            )
            RegularRow("Regular Row")
            Spacer(
                Modifier.height(1.dp)
                    .fillMaxWidth()
                    .background(Color.Black)
            )

        }

    }
}
