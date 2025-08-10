package ui.rows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.AppTheme

@Composable
fun HeaderRow(content: String) {
    Surface(color = MaterialTheme.colors.primary) {
        BaseRow {
            Text(
                text = content,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
@Preview()
private fun previewRow() {
    AppTheme {
        Column {
            Spacer(Modifier.height(20.dp))
            HeaderRow("Test q gf j A")
        }
    }
}
