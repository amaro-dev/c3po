package plugins.automation.definition.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plugins.automation.definition.AutomationPlugin
import plugins.automation.structure.AutomationState
import ui.OnAction

@Composable
fun ScriptCreationScreen(
    state: AutomationState,
    onAction: OnAction,
) {
    Card(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Steps section header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Steps (${state.currentScript?.steps?.size ?: 0})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!state.isRunning) {
                                AddStepDropdownIcon(onAction)
                            } else {
                                Text(
                                    text = "Running...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                if (state.currentScript?.steps?.isNotEmpty() == true) {
                    val executionState = StepExecutionState(
                        isScriptRunning = state.isRunning,
                        currentStepIndex = state.runningStepIndex,
                        failedStepIndex = state.failedStepIndex,
                        completedSteps = state.completedSteps
                    )
                    itemsIndexed(state.currentScript!!.steps) { index, step ->
                        val stepErrorMessage =
                            if (executionState.failedStepIndex == index && state.runLogs.isNotEmpty()) {
                                state.runLogs.find { it.contains("Step ${index + 1} failed:") }
                            } else null

                        StepCardItem(
                            step = step,
                            index = index,
                            executionState = executionState,
                            errorMessage = stepErrorMessage,
                            onRemove = { onAction(AutomationPlugin.Actions.RemoveStep(index)) },
                            onEdit = {
                                if (!state.isRunning && !state.showPackageSelector && !state.showActivitySelector && !state.showApkPicker) {
                                    onAction(AutomationPlugin.Actions.EditStep(index))
                                }
                            },
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No steps added yet. Use 'Add Step' to get started.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                item {
                    Divider(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}

