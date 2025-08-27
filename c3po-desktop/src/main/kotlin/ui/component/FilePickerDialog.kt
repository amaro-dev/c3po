package ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

enum class FilePickerMode {
    FILE, DIRECTORY
}

data class FilePickerFilter(
    val description: String,
    val extensions: List<String>
)

/**
 * A reusable file picker dialog component that supports both file and directory selection.
 * Provides native file dialog with fallback to manual input.
 *
 * @param title The dialog title
 * @param mode Whether to pick files or directories
 * @param filter Optional file filter (only applies to file mode)
 * @param initialDirectory Optional initial directory path
 * @param onFileSelected Callback when a file/directory is selected
 * @param onDismiss Callback when dialog is dismissed without selection
 */
@Composable
fun FilePickerDialog(
    title: String,
    mode: FilePickerMode = FilePickerMode.FILE,
    filter: FilePickerFilter? = null,
    initialDirectory: String? = null,
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedPath by remember { mutableStateOf("") }
    var showFallback by remember { mutableStateOf(false) }

    // Native file picker launcher
    LaunchedEffect(Unit) {
        try {
            val path = when (mode) {
                FilePickerMode.FILE -> {
                    openFileDialog(title, filter, initialDirectory)
                }

                FilePickerMode.DIRECTORY -> {
                    openDirectoryDialog(title, initialDirectory)
                }
            }

            if (path != null) {
                selectedPath = path
            } else {
                // User cancelled the dialog
                onDismiss()
                return@LaunchedEffect
            }
        } catch (e: Exception) {
            // Fallback to manual input if file dialog fails
            showFallback = true
        }
    }

    if (selectedPath.isNotEmpty()) {
        // Show confirmation dialog with selected file/directory
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Confirm Selection",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "Selected ${mode.name.lowercase()}:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = selectedPath,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onFileSelected(selectedPath) }
                ) {
                    Text("Use This ${if (mode == FilePickerMode.FILE) "File" else "Folder"}")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    } else if (showFallback) {
        // Fallback manual input dialog
        var manualPath by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "${if (mode == FilePickerMode.FILE) "File" else "Directory"} picker unavailable. Please enter the path manually:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = manualPath,
                        onValueChange = { manualPath = it },
                        label = {
                            Text(
                                "${if (mode == FilePickerMode.FILE) "File" else "Directory"} Path",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        placeholder = {
                            Text(
                                if (mode == FilePickerMode.FILE) "/path/to/your/file" else "/path/to/your/directory",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (manualPath.isNotBlank()) {
                            onFileSelected(manualPath)
                        }
                    },
                    enabled = manualPath.isNotBlank()
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Opens a native file dialog for file selection
 */
private fun openFileDialog(title: String, filter: FilePickerFilter?, initialDirectory: String?): String? {
    return try {
        val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, title, java.awt.FileDialog.LOAD)

        // Apply file filter if provided
        filter?.let { f ->
            fileDialog.setFilenameFilter { _, name ->
                f.extensions.any { ext -> name.lowercase().endsWith(".$ext") }
            }
        }

        // Set initial directory if provided
        initialDirectory?.let { dir ->
            fileDialog.directory = dir
        }

        fileDialog.isVisible = true

        val selectedFile = fileDialog.file
        val selectedDir = fileDialog.directory

        if (selectedFile != null && selectedDir != null) {
            "$selectedDir$selectedFile"
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Opens a native directory dialog for folder selection (with macOS-specific handling)
 */
private fun openDirectoryDialog(title: String, initialDirectory: String?): String? {
    return try {
        // Temporarily enable directory selection on macOS
        val key = "apple.awt.fileDialogForDirectories"
        val previous = try {
            System.getProperty(key)
        } catch (_: Exception) {
            null
        }

        try {
            try {
                System.setProperty(key, "true")
            } catch (_: Exception) {
            }

            val chooser = java.awt.FileDialog(null as java.awt.Frame?, title, java.awt.FileDialog.LOAD)
            chooser.isMultipleMode = false

            initialDirectory?.let { dir ->
                chooser.directory = dir
            }

            chooser.isVisible = true

            val selectedPath = if (chooser.file != null) {
                File(chooser.directory, chooser.file).absolutePath
            } else {
                null
            }

            selectedPath
        } finally {
            try {
                if (previous == null) System.clearProperty(key) else System.setProperty(key, previous)
            } catch (_: Exception) {
            }
        }
    } catch (e: Exception) {
        null
    }
}