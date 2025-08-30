package plugins.signature

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import core.facade.BoolState
import darkenedBy
import ui.dangerColor
import ui.successColor
import ui.warningColor

@Composable
fun CompliantCheck(value: BoolState) {
    when (value) {
        BoolState.TRUE -> Image(
            Icons.Default.Check,
            "",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.successColor.darkenedBy(0.1f))
        )

        BoolState.FALSE -> Image(
            Icons.Default.Close,
            "",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.dangerColor)
        )
        BoolState.NOT_FOUND ->
            Image(
                Icons.Default.Warning,
                "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.warningColor.darkenedBy(0.1f)),
            )
    }
}
