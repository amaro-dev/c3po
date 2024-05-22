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
    var searchTerm by remember { mutableStateOf("") }
    searchTerm.useDebounce { onChangeFilter(it) }
//    placeholder = { Text("Type to search") },
    Surface(color = MaterialTheme.colors.background) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Filled.Search, "", Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                searchTerm,
                onValueChange = { searchTerm = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }


}
