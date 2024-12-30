package ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContentBox(
    searchTerm: String,
    onSearch: ((String) -> Unit),
    content: LazyListScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column {
            MySearchField(searchTerm) { onSearch(it) }
            Box(Modifier.fillMaxSize()) {
                val listState = rememberLazyListState()
                LazyColumn(Modifier.fillMaxSize().padding(end = Dimens.SCROLL_BAR_MARGIN.dp), state = listState) {
                    content()
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = listState),
                )
            }
        }
    }
}
