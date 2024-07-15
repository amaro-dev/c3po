package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MySearchField(onChangeFilter: (String) -> Unit) {
    var searchTerm by remember { mutableStateOf(Texts.EMPTY) }
    searchTerm.useDebounce { onChangeFilter(it) }

    Surface(color = MaterialTheme.colors.background) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp)
        ) {
            Icon(Icons.Filled.Search, Texts.EMPTY, Modifier.size(Dimens.ICON_SIZE_REGULAR.dp))
            Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
            BasicTextField(
                searchTerm,
                onValueChange = { searchTerm = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}
