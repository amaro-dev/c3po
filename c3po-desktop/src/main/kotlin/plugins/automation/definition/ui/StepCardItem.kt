package plugins.automation.definition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plugins.automation.structure.ScriptStep
import ui.component.CustomActionButton

@Composable
fun StepCardItem(
    step: ScriptStep,
    index: Int,
    executionState: StepExecutionState,
    errorMessage: String? = null,
    onRemove: () -> Unit,
    onEdit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val stepStatus = getStepStatus(index, executionState)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                when (stepStatus) {
                                    StepStatus.SUCCESS -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                        .copy(alpha = 0.2f)

                                    StepStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    StepStatus.RUNNING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    StepStatus.PENDING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (stepStatus) {
                            StepStatus.PENDING -> Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )

                            StepStatus.RUNNING -> CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            StepStatus.SUCCESS -> Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Completed",
                                modifier = Modifier.size(16.dp),
                                tint = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            )

                            StepStatus.FAILED -> Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Failed",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getStepDisplayName(step),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        val description = getStepDescription(step)
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        } else {
                            Text(
                                text = "⚠️ Configuration needed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            )
                        }
                    }
                }

                CustomActionButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "Remove step",
                    onClick = onRemove
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

