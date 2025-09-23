package plugins.automation.definition.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.model.ActivityInfo
import ui.component.CustomTextField
import ui.component.DialogAction
import ui.component.StandardDialog

@Composable
fun ActivitySelectorDialog(
    activities: List<ActivityInfo>,
    selectedPackage: String,
    onActivitySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchText by remember { mutableStateOf("") }
    val filteredActivities = remember(activities, searchText) {
        if (searchText.isBlank()) activities else activities.filter {
            it.activityPath.contains(
                searchText,
                ignoreCase = true
            )
        }
    }

    StandardDialog(
        title = if (selectedPackage.isNotBlank()) "Select Activity - Package: $selectedPackage" else "Select Activity",
        onDismiss = onDismiss,
        secondaryAction = DialogAction(
            text = "Cancel",
            onClick = onDismiss
        )
    ) {
        CustomTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = "Search activities...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.heightIn(min = 8.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(filteredActivities.size) { index ->
                val activity = filteredActivities[index]
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        onActivitySelected(activity.activityPath)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = activity.activityPath,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Package: ${activity.packageName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

