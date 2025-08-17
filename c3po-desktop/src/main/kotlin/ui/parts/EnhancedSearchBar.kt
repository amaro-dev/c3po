package ui.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.component.CustomTextField

/**
 * Generic enhanced search bar with configurable filters
 */
@Composable
fun <F> EnhancedSearchBar(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    filterState: F,
    onFilterChange: (F) -> Unit,
    searchPlaceholder: String = "Search...",
    filterContent: @Composable RowScope.(F, (F) -> Unit) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field - full width on first line
            CustomTextField(
                value = searchTerm,
                onValueChange = onSearchChange,
                placeholder = searchPlaceholder,
                modifier = Modifier.fillMaxWidth()
            )

            // Generic filter content - on second line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filterContent(filterState, onFilterChange)
            }
        }
    }
}

