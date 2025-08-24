package ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import core.model.UpdateInfo
import core.model.UpdateState

@Composable
fun UpdateNotificationDialog(
    updateInfo: UpdateInfo,
    updateState: UpdateState,
    downloadProgress: Int = 0,
    errorMessage: String? = null,
    onUpdateNow: () -> Unit,
    onInstallNow: () -> Unit = {},
    onUpdateLater: () -> Unit,
    onRetry: () -> Unit = {},
    onDismiss: () -> Unit = onUpdateLater
) {
    Dialog(onDismissRequest = { if (updateState != UpdateState.Installing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val (title, description, isError) = when (updateState) {
                    UpdateState.UpdateAvailable -> Triple(
                        "Update Available",
                        "Version ${updateInfo.version} is now available. Would you like to download and install it now?",
                        false
                    )

                    UpdateState.Downloading -> Triple(
                        "Downloading Update",
                        "Downloading version ${updateInfo.version}... Please wait.",
                        false
                    )

                    UpdateState.DownloadComplete, UpdateState.InstallReady -> Triple(
                        "Ready to Install",
                        "Version ${updateInfo.version} has been downloaded and is ready to install.",
                        false
                    )

                    UpdateState.Installing -> Triple(
                        "Installing Update",
                        "Installing version ${updateInfo.version}... The application will restart automatically.",
                        false
                    )

                    UpdateState.Error -> Triple(
                        "Update Failed",
                        errorMessage ?: "An error occurred while updating. Please try again.",
                        true
                    )

                    else -> Triple(
                        "Update Available",
                        "Version ${updateInfo.version} is now available.",
                        false
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )

                // Error details section
                if (isError && errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Error Details:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Show progress indicator for downloading/installing states
                when (updateState) {
                    UpdateState.Downloading -> {
                        if (downloadProgress > 0) {
                            LinearProgressIndicator(
                                progress = downloadProgress / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$downloadProgress%",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Preparing download...",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    UpdateState.Installing -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Installing... Do not close the application.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    else -> {}
                }

                if (updateInfo.releaseNotesUrl != null && !isError) {
                    TextButton(
                        onClick = { /* Open release notes URL */ },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "View release notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Buttons based on current state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    when (updateState) {
                        UpdateState.UpdateAvailable -> {
                            TextButton(onClick = onUpdateLater) {
                                Text("Later")
                            }
                            Button(onClick = onUpdateNow) {
                                Text("Download")
                            }
                        }

                        UpdateState.DownloadComplete, UpdateState.InstallReady -> {
                            TextButton(onClick = onUpdateLater) {
                                Text("Later")
                            }
                            Button(onClick = onInstallNow) {
                                Text("Install Now")
                            }
                        }

                        UpdateState.Error -> {
                            TextButton(onClick = onUpdateLater) {
                                Text("Cancel")
                            }
                            Button(onClick = onRetry) {
                                Text("Try Again")
                            }
                        }

                        UpdateState.Downloading, UpdateState.Installing -> {
                            // No buttons during these states - user must wait
                            // But allow canceling download
                            if (updateState == UpdateState.Downloading) {
                                TextButton(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }

                        else -> {
                            TextButton(onClick = onUpdateLater) {
                                Text("Close")
                            }
                        }
                    }
                }

                // Warning message for critical states
                if (updateState == UpdateState.Installing) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ Do not close the application or shut down your computer during installation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}