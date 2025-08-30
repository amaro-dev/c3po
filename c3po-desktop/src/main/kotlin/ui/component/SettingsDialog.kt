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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.overlayColor
import ui.secondaryTextColor

/**
 * Enhanced settings dialog supporting ADB Path, Updates URL, and Dark Mode configuration.
 * Replaces the simple inline settings dialog with a comprehensive, reusable component.
 *
 * @param initialAdbPath Current ADB path from settings
 * @param initialUpdatesUrl Current updates URL from settings
 * @param initialDarkMode Current dark mode setting from settings
 * @param onSave Callback when settings should be saved
 * @param onCancel Callback when dialog is cancelled
 */
@Composable
fun SettingsDialog(
    initialAdbPath: String,
    initialUpdatesUrl: String,
    initialDarkMode: Boolean,
    onSave: (adbPath: String, updatesUrl: String, darkMode: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var adbPath by remember { mutableStateOf(initialAdbPath) }
    var updatesUrl by remember { mutableStateOf(initialUpdatesUrl) }
    var darkMode by remember { mutableStateOf(initialDarkMode) }
    var showFilePicker by remember { mutableStateOf(false) }

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
                        onClick = { onSave(adbPath, updatesUrl, darkMode) }
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