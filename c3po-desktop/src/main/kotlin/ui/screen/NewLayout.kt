package ui

import Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import core.App
import core.model.Action
import core.model.AppState
import core.model.CommandStatus
import dev.amaro.sonic.IAction
import plugins.Plugin
import ui.component.SettingsDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewLayout(
    app: App,
) {
    val state = app.listen().collectAsState(initial = AppState()).value
    val onAction: OnAction = { action: IAction -> app.perform(action) }
    val selectedDevice = state.currentDevice?.id
    val selectedPlugin = state.currentPlugin
    val showLoading = state.commandStatus == CommandStatus.Running
    val errorMessage = state.errorMessage
    val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP) ?: ""
    val updatesUrl = state.settings.getProperty(Settings.UPDATES_URL_PROP) ?: ""
    val settingsState = state.settingsState
    var showSettingsDialog by remember { mutableStateOf(settingsState.name == "NotInitialized" || settingsState.name == "NotFound" || adbPath.isBlank()) }

    // Automatically close dialog when settings are initialized
    LaunchedEffect(settingsState) {
        if (settingsState == core.model.SettingsState.Initialized) {
            showSettingsDialog = false
        }
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxSize()) {
            Sidebar(
                plugins = app.plugins,
                selectedPluginId = selectedPlugin,
                onPluginSelected = { onAction(Action.StartPlugin(it)) }
            )
            Column(Modifier.weight(1f).fillMaxHeight()) {
                TopBar(
                    devices = state.devices,
                    selectedDevice = selectedDevice,
                    appVersion = state.appVersion,
                    onDeviceSelected = { id ->
                        val device = state.devices.find { it.id == id }
                        if (device != null && settingsState == core.model.SettingsState.Initialized) {
                            onAction(Action.SelectDevice(device))
                        }
                    },
                    onRefreshDevices = {
                        if (settingsState == core.model.SettingsState.Initialized) onAction(Action.RefreshDevices)
                    },
                    onShowSettings = {
                        showSettingsDialog = true
                    }
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    ContentArea(
                        plugins = app.plugins,
                        selectedPluginId = selectedPlugin,
                        results = state.windows,
                        onAction = onAction
                    )

                    if (showLoading) LoadingPill()

                    errorMessage?.let {
                        ErrorMessage(it) { onAction(Action.ClearError) }
                    }
                }
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                initialAdbPath = adbPath,
                initialUpdatesUrl = updatesUrl,
                onSave = { newAdbPath, newUpdatesUrl ->
                    onAction(Action.ChangeSettingsProperty(Settings.ADB_PATH_PROP, newAdbPath))
                    onAction(Action.ChangeSettingsProperty(Settings.UPDATES_URL_PROP, newUpdatesUrl))
                    showSettingsDialog = false
                },
                onCancel = { showSettingsDialog = false }
            )
        }

        // Update notification dialog - show for various update states
        val showUpdateDialog = state.updateInfo != null && state.updateState in listOf(
            core.model.UpdateState.UpdateAvailable,
            core.model.UpdateState.Downloading,
            core.model.UpdateState.DownloadComplete,
            core.model.UpdateState.InstallReady,
            core.model.UpdateState.Installing,
            core.model.UpdateState.UpdateCancelled,
            core.model.UpdateState.Error
        )

        if (showUpdateDialog) {
            ui.component.UpdateNotificationDialog(
                updateInfo = state.updateInfo!!,
                updateState = state.updateState,
                downloadProgress = state.downloadProgress,
                errorMessage = state.errorMessage,
                onUpdateNow = {
                    onAction(Action.DownloadUpdate(state.updateInfo!!))
                },
                onInstallNow = {
                    // Get the downloaded file path from temp directory
                    val downloadDir = java.io.File(System.getProperty("java.io.tmpdir"), "c3po-updates")
                    val downloadedFile = java.io.File(downloadDir, "c3po-${state.updateInfo!!.version}.dmg")
                    if (downloadedFile.exists()) {
                        onAction(Action.InstallUpdate(downloadedFile.absolutePath))
                    } else {
                        onAction(Action.UpdateError("Downloaded file not found: ${downloadedFile.absolutePath}"))
                    }
                },
                onUpdateLater = {
                    onAction(Action.DismissUpdate) // Properly dismiss until next app restart
                },
                onRetry = {
                    // Reset error state and retry the last operation
                    when (state.updateState) {
                        core.model.UpdateState.Error -> {
                            // Determine what to retry based on available data
                            if (state.updateInfo != null) {
                                onAction(Action.DownloadUpdate(state.updateInfo!!))
                            } else {
                                onAction(Action.CheckForUpdate)
                            }
                        }

                        else -> {
                            onAction(Action.CheckForUpdate)
                        }
                    }
                },
                onCancelDownload = {
                    onAction(Action.CancelDownload)
                }
            )
        }
    }
}


@Composable
private fun Sidebar(
    plugins: List<Plugin<*>>,
    selectedPluginId: String?,
    onPluginSelected: (String) -> Unit
) {
    Column(
        Modifier.width(120.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        plugins.forEach { plugin ->
            val isSelected = plugin.id == selectedPluginId
            val background = if (isSelected) Color(0xFF4A4E69).copy(alpha = 0.5f) else Color.Transparent
            val shape = MaterialTheme.shapes.large.copy(all = androidx.compose.foundation.shape.CornerSize(20.dp))
            val horizontalPadding = 12.dp // Increased margin
            val verticalPadding = 8.dp
            val internalPadding = if (isSelected) PaddingValues(horizontal = 0.dp, vertical = 4.dp) else PaddingValues(
                horizontal = 12.dp,
                vertical = 8.dp
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .height(72.dp)
                    .background(background, shape = shape)
                    .clickable { onPluginSelected(plugin.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(internalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = plugin.icon,
                        contentDescription = plugin.name,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        plugin.name,
                        color = Color.White,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopBar(
    devices: List<core.model.AdbDevice>,
    selectedDevice: String?,
    appVersion: String,
    onDeviceSelected: (String) -> Unit,
    onRefreshDevices: () -> Unit,
    onShowSettings: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(24.dp))
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.height(40.dp)
                ) {
                    val selectedName = devices.find { it.id == selectedDevice }?.name ?: "No device"
                    Text(selectedName, color = Color(0xFF22223B))
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Show devices",
                        tint = Color(0xFF22223B),
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    devices.forEach { device ->
                        DropdownMenuItem(
                            text = { Text(device.name) },
                            onClick = {
                                onDeviceSelected(device.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onRefreshDevices() }) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh")
            }
            Spacer(Modifier.weight(1f))

            // Settings gear icon
            IconButton(onClick = { onShowSettings() }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.width(8.dp))

            // Version display on the right
            Text(
                "v$appVersion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun ContentArea(
    plugins: List<Plugin<*>>,
    selectedPluginId: String?,
    results: Map<String, core.model.WindowResult<*>>,
    onAction: OnAction
) {
    val selectedPlugin: Plugin<*>? = plugins.find { it.id == selectedPluginId }

    if (selectedPlugin != null) {
        (selectedPlugin as Plugin<*>?)?.render(results, onAction)
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No plugin selected", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun LoadingPill() {
    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = Color.White,
            shape = MaterialTheme.shapes.large,
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(top = 24.dp, end = 32.dp)
                .wrapContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF4A4E69),
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 3.dp
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    "Processing...",
                    color = Color(0xFF22223B),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = Color(0xFFE07A5F),
            shape = MaterialTheme.shapes.large,
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(top = 24.dp, end = 32.dp)
                .wrapContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

