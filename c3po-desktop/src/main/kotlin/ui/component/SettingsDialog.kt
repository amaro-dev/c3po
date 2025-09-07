package ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.model.Action
import ui.overlayColor
import ui.secondaryTextColor
import java.awt.Desktop
import java.io.File

/**
 * Enhanced settings dialog supporting ADB Path, Updates URL, Dark Mode, and Logging configuration.
 * Replaces the simple inline settings dialog with a comprehensive, reusable component.
 *
 * @param initialAdbPath Current ADB path from settings
 * @param initialUpdatesUrl Current updates URL from settings
 * @param initialDarkMode Current dark mode setting from settings
 * @param initialLoggingEnabled Current logging enabled setting
 * @param initialAdbLogging Current ADB logging mode ("off", "errors", "full")
 * @param initialPerformLogging Current perform logging setting
 * @param initialReduceLogging Current reduce logging setting
 * @param onSave Callback when settings should be saved
 * @param onCancel Callback when dialog is cancelled
 */
@Composable
fun SettingsDialog(
    initialAdbPath: String,
    initialUpdatesUrl: String,
    initialDarkMode: Boolean,
    initialLoggingEnabled: Boolean,
    initialAdbLogging: String,
    initialPerformLogging: Boolean,
    initialReduceLogging: Boolean,
    isSearchingAdbPath: Boolean,
    adbSearchError: String?,
    onAction: (Action) -> Unit,
    onSave: (adbPath: String, updatesUrl: String, darkMode: Boolean, loggingEnabled: Boolean, adbLogging: String, performLogging: Boolean, reduceLogging: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var adbPath by remember { mutableStateOf(initialAdbPath) }
    var updatesUrl by remember { mutableStateOf(initialUpdatesUrl) }
    var darkMode by remember { mutableStateOf(initialDarkMode) }
    var showFilePicker by remember { mutableStateOf(false) }

    // Update local adbPath when settings change (including from search results)
    LaunchedEffect(initialAdbPath) {
        adbPath = initialAdbPath
    }

    // Logging configuration state
    var loggingEnabled by remember { mutableStateOf(initialLoggingEnabled) }
    var adbLogging by remember { mutableStateOf(initialAdbLogging) }
    var performLogging by remember { mutableStateOf(initialPerformLogging) }
    var reduceLogging by remember { mutableStateOf(initialReduceLogging) }
    var showAdbDropdown by remember { mutableStateOf(false) }

    // Function to open logs folder in system file manager
    fun openLogsFolder() {
        try {
            val logsDir = File("${System.getProperty("user.home")}/.c3po/logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            Desktop.getDesktop().open(logsDir)
        } catch (e: Exception) {
            // Fallback - could show error dialog, but for now just print to console
            println("Failed to open logs folder: ${e.message}")
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.overlayColor),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(600.dp)
        ) {
            Column(
                Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                // ADB Path Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomTextField(
                            value = adbPath,
                            onValueChange = { adbPath = it },
                            placeholder = "/path/to/adb",
                            modifier = Modifier.weight(1f)
                        )

                        // ADB Search Button
                        IconButton(
                            onClick = {
                                onAction(Action.SearchAdbPath)
                            },
                            enabled = !isSearchingAdbPath,
                            modifier = Modifier
                                .background(
                                    if (isSearchingAdbPath) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                                    MaterialTheme.shapes.medium
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search for ADB",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // File Browser Button
                        IconButton(
                            onClick = { showFilePicker = true },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.shapes.medium
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen,
                                contentDescription = "Browse",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = "Select the Android Debug Bridge (adb) executable on your system",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryTextColor
                    )

                    // Show error message if ADB search failed
                    adbSearchError?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Updates URL Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomTextField(
                        value = updatesUrl,
                        onValueChange = { updatesUrl = it },
                        placeholder = "https://api.github.com/repos/...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "URL endpoint for checking application updates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryTextColor
                    )
                }

                // Dark Mode Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Dark Mode",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Use dark theme for the application interface",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondaryTextColor
                        )
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }

                // Logging Configuration Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Master logging toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Enable Logging",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Capture structured logs for debugging and issue reporting",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondaryTextColor
                            )
                        }
                        Switch(
                            checked = loggingEnabled,
                            onCheckedChange = { loggingEnabled = it }
                        )
                    }

                    // Logging options (only visible when logging is enabled)
                    if (loggingEnabled) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // ADB Logging dropdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ADB Commands",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Box {
                                    OutlinedButton(
                                        onClick = { showAdbDropdown = true }
                                    ) {
                                        Text(
                                            text = when (adbLogging) {
                                                "off" -> "Off"
                                                "errors" -> "Errors Only"
                                                "full" -> "Full Results"
                                                else -> "Off"
                                            }
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showAdbDropdown,
                                        onDismissRequest = { showAdbDropdown = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Off") },
                                            onClick = {
                                                adbLogging = "off"
                                                showAdbDropdown = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Errors Only") },
                                            onClick = {
                                                adbLogging = "errors"
                                                showAdbDropdown = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Full Results") },
                                            onClick = {
                                                adbLogging = "full"
                                                showAdbDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Action Perform logging toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Action Dispatch",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = performLogging,
                                    onCheckedChange = { performLogging = it }
                                )
                            }

                            // State Reduce logging toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "State Changes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = reduceLogging,
                                    onCheckedChange = { reduceLogging = it }
                                )
                            }
                        }
                    }

                    // Open logs folder button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { openLogsFolder() }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = "Open Logs Folder",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = " Open Logs Folder",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onCancel
                    )

                    PrimaryButton(
                        text = "Save",
                        onClick = {
                            onSave(
                                adbPath,
                                updatesUrl,
                                darkMode,
                                loggingEnabled,
                                adbLogging,
                                performLogging,
                                reduceLogging
                            )
                        }
                    )
                }
            }
        }
    }

    // File picker for ADB path
    if (showFilePicker) {
        FilePickerDialog(
            title = "Select ADB Executable",
            mode = FilePickerMode.FILE,
            onFileSelected = { path ->
                adbPath = path
                showFilePicker = false
            },
            onDismiss = { showFilePicker = false }
        )
    }
}