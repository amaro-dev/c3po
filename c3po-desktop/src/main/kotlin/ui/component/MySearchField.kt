package ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ui.component.CustomTextField

@Composable
fun MySearchField(
    initialValue: String,
    onChangeFilter: (String) -> Unit,
) {
    var searchTerm by remember { mutableStateOf(initialValue) }
    searchTerm.useDebounce { onChangeFilter(it) }

    Surface(color = MaterialTheme.colors.surface) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier,
        ) {
            CustomTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = "Search...",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
