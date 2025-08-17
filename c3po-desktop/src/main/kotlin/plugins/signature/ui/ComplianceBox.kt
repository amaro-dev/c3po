package plugins.signature

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.facade.BoolState
import ui.definitions.Dimens

@Composable
fun ComplianceBox(
    label: String,
    value: BoolState,
) {
    Column(
        Modifier
            .border(1.dp, MaterialTheme.colors.onSurface, shape = RoundedCornerShape(Dimens.ROUNDED_CORNER.dp))
            .width(64.dp)
            .padding(Dimens.HORIZONTAL_SPACER.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label)
        Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
        CompliantCheck(value)
    }
}
