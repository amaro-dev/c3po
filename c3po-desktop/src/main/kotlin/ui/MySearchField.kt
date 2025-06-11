package ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.definitions.Dimens
import ui.definitions.Texts

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
            TextField(
                searchTerm,
                onValueChange = { searchTerm = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Search, Texts.EMPTY, Modifier.size(Dimens.ICON_SIZE_SMALL.dp))
                },
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = Color.Black
                )
            )
        }
    }
}
