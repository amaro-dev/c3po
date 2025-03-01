package plugins.signature

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import darkenedBy
import facade.BoolState

@Composable
fun CompliantCheck(value: BoolState) {
    when (value) {
        BoolState.TRUE -> Image(Icons.Default.Check, "", colorFilter = ColorFilter.tint(Color.Green.darkenedBy(0.1f)))
        BoolState.FALSE -> Image(Icons.Default.Close, "", colorFilter = ColorFilter.tint(Color.Red))
        BoolState.NOT_FOUND -> Image(
            Icons.Default.Warning,
            "",
            colorFilter = ColorFilter.tint(Color.Yellow.darkenedBy(0.1f))
        )
    }
}
