package ui.plugins.packages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PackageHeader(packageName: String) {
    Surface(color = MaterialTheme.colors.primary) {
        Box(Modifier.fillMaxWidth().padding(20.dp, 6.dp)) {
            Text(packageName, style = MaterialTheme.typography.subtitle1)
        }
    }
}
