package plugins.packages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ui.baselinePadding
import ui.definitions.Dimens
import ui.definitions.Icons
import ui.definitions.Texts
import ui.onHover

@Composable
fun SignatureInfoRow(label: String, value: String, onCopy: ((String) -> Unit)? = null) {
    var isHoveringValue: Boolean by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.baselinePadding(Dimens.VERTICAL_SPACER)
    ) {
        Text("$label:", style = MaterialTheme.typography.subtitle2)
        Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
        Text(
            value,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.weight(1f).onHover { isHoveringValue = it })

        if (onCopy != null) {
            Icon(
                painterResource(Icons.COPY),
                contentDescription = Texts.EMPTY,
                modifier =
                    Modifier
                        .clickable { onCopy(value) }
                        .size(12.dp),
            )
        }
    }


}
