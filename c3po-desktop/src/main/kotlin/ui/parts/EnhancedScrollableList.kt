package ui.parts

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.component.EnhancedHeaderRow
import ui.rows.RowType

/**
 * Generic enhanced scrollable list with Card wrapper and optional grouping
 */
@Composable
fun <T> EnhancedScrollableList(
    items: List<T>,
    groupBy: ((T) -> String)? = null,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T, Boolean) -> Unit, // item, showBottomBorder
) {
    Card(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                val flattenedItems = if (groupBy != null) {
                    // Group items and create header/regular pairs
                    items
                        .groupBy { groupBy(it) }
                        .flatMap { group ->
                            listOf(Pair(RowType.Header, group.key)).plus(
                                group.value.map { Pair(RowType.Regular, it) }
                            )
                        }
                } else {
                    // No grouping, just regular items
                    items.map { Pair(RowType.Regular, it) }
                }

                items(flattenedItems) { itemPair ->
                    val index = flattenedItems.indexOf(itemPair)
                    val isLastInGroup = index < flattenedItems.size - 1 &&
                            flattenedItems[index + 1].first == RowType.Header

                    when (itemPair.first) {
                        RowType.Header -> {
                            EnhancedHeaderRow(itemPair.second as String)
                        }

                        RowType.Regular -> {
                            @Suppress("UNCHECKED_CAST")
                            val item = itemPair.second as T
                            itemContent(item, !isLastInGroup)
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}