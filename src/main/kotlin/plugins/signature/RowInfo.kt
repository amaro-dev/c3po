package plugins.signature

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.definitions.Dimens

@Composable
fun RowInfo(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.body2.copy(fontSize = 12.sp, fontWeight = FontWeight.Black))
        Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
        Text(value, style = MaterialTheme.typography.body2.copy(fontSize = 12.sp))
    }
}
