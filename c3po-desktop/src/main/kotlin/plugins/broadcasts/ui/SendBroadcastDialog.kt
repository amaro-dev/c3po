package plugins.broadcasts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.model.BroadcastAction
import ui.component.CustomActionButton
import ui.component.CustomTextField
import ui.component.DialogAction
import ui.component.StandardDialog

@Composable
fun SendBroadcastDialog(
    broadcastAction: BroadcastAction,
    onSend: (Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var extras by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    fun addExtra() {
        extras = extras + ("" to "")
    }

    fun removeExtra(index: Int) {
        extras = extras.filterIndexed { i, _ -> i != index }
    }

    fun updateExtra(index: Int, key: String, value: String) {
        extras = extras.mapIndexed { i, pair ->
            if (i == index) key to value else pair
        }
    }

    StandardDialog(
        title = "Send Broadcast with Extras",
        onDismiss = onDismiss,
        primaryAction = DialogAction(
            text = "Send",
            onClick = {
                val extrasMap = extras
                    .filter { it.first.isNotBlank() }
                    .toMap()
                onSend(extrasMap)
            },
            isPrimary = true,
            enabled = true
        ),
        secondaryAction = DialogAction(
            text = "Cancel",
            onClick = onDismiss
        ),
        width = 700
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Broadcast Action Info
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Action:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = broadcastAction.action,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Text(
                    text = "Package: ${broadcastAction.packageName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                broadcastAction.requiredPermission?.let { permission ->
                    Text(
                        text = "Required Permission: $permission",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }

            // Extras Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Extras (String key-value pairs):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CustomActionButton(
                        icon = Icons.Filled.Add,
                        contentDescription = "Add extra",
                        tooltipText = "Add new extra",
                        onClick = ::addExtra
                    )
                }

                if (extras.isEmpty()) {
                    Text(
                        text = "No extras will be sent. Click + to add extras.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        extras.forEachIndexed { index, (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomTextField(
                                    value = key,
                                    onValueChange = { updateExtra(index, it, value) },
                                    placeholder = "Key",
                                    modifier = Modifier.weight(1f)
                                )

                                CustomTextField(
                                    value = value,
                                    onValueChange = { updateExtra(index, key, it) },
                                    placeholder = "Value",
                                    modifier = Modifier.weight(1f)
                                )

                                CustomActionButton(
                                    icon = Icons.Filled.Remove,
                                    contentDescription = "Remove extra",
                                    tooltipText = "Remove this extra",
                                    onClick = { removeExtra(index) }
                                )
                            }
                        }
                    }
                }
            }

            // Preview Section
            if (extras.isNotEmpty() && extras.any { it.first.isNotBlank() }) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Command Preview:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val extrasString = extras
                        .filter { it.first.isNotBlank() }
                        .joinToString(" ") { (key, value) ->
                            "--es \"$key\" \"$value\""
                        }

                    val fullCommand = "adb shell am broadcast -a \"${broadcastAction.action}\" $extrasString"

                    Text(
                        text = fullCommand,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}