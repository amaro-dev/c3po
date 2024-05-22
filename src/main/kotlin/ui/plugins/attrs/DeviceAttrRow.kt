package ui.plugins.attrs

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun DeviceAttrRow(label: String, value: String?) {
    Row(Modifier.fillMaxWidth().padding(0.dp, 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(5f), textAlign = TextAlign.End
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.weight(8f)
        )
    }
}
