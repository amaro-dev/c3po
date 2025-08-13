package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Close

@Composable
fun NewLayout() {
    // State for UI-related elements
    val (selectedPlugin, setSelectedPlugin) = remember { mutableStateOf("Attributes") }
    val (selectedDevice, setSelectedDevice) = remember { mutableStateOf<String?>("Device 1") }
    val (isCompanionConnected, setCompanionConnected) = remember { mutableStateOf(false) }
    val (showLoading, setShowLoading) = remember { mutableStateOf(true) } // Set to true for demo
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>("Something went wrong!") } // Set for demo
    val (showSettingsDialog, setShowSettingsDialog) = remember { mutableStateOf(true) } // Set to true for demo
    val (showCompanionDialog, setShowCompanionDialog) = remember { mutableStateOf(true) } // Set to true for demo

    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            Sidebar(selectedPlugin, setSelectedPlugin)
            Column(Modifier.weight(1f).fillMaxHeight()) {
                TopBar(
                    selectedDevice = selectedDevice,
                    onDeviceSelected = setSelectedDevice,
                    isCompanionConnected = isCompanionConnected,
                    onRefreshDevices = { /* TODO: Implement refresh logic */ },
                    onCompanionClick = { /* TODO: Open companion dialog */ }
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    ContentArea(selectedPlugin, selectedDevice)

                    if (showLoading) LoadingPill()

                    errorMessage?.let {
                        ErrorMessage(it) { setErrorMessage(null) }
                    }
                }
            }
        }

        // Show dialogs for demonstration
        if (showSettingsDialog) {
            SettingsDialog(
                onSave = { setShowSettingsDialog(false) },
                onCancel = { setShowSettingsDialog(false) }
            )
        }

        if (showCompanionDialog) {
            CompanionDialog(
                onInstall = { setShowCompanionDialog(false) },
                onSkip = { setShowCompanionDialog(false) },
                onCancel = { setShowCompanionDialog(false) }
            )
        }
    }
}

@Composable
fun TopBar(
    selectedDevice: String?,
    onDeviceSelected: (String?) -> Unit,
    isCompanionConnected: Boolean,
    onRefreshDevices: () -> Unit,
    onCompanionClick: () -> Unit
) {
    // Example device list for demonstration
    val devices = listOf("Device 1", "Device 2", "Device 3")
    var expanded by remember { mutableStateOf(false) }

    Surface(color = Color(0xFFEEEEEE), shadowElevation = 4.dp) {
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
                    Text(selectedDevice ?: "No device", color = Color(0xFF22223B))
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
                            text = { Text(device) },
                            onClick = {
                                onDeviceSelected(device)
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
            IconButton(onClick = { onCompanionClick() }) {
                Icon(
                    imageVector = if (isCompanionConnected) Icons.Filled.VerifiedUser else Icons.Filled.Error,
                    contentDescription = if (isCompanionConnected) "Companion Connected" else "Companion Disconnected",
                    tint = if (isCompanionConnected) Color(0xFF4CAF50) else Color(0xFFE07A5F)
                )
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun Sidebar(selectedPlugin: String, onPluginSelected: (String) -> Unit) {
    data class Plugin(val name: String, val icon: ImageVector, val label: String)
    val plugins = listOf(
        Plugin("Attributes", Icons.Filled.FormatListBulleted, "Attributes"),
        Plugin("Activities", Icons.Filled.Android, "Activities"),
        Plugin("Packages", Icons.Filled.Inventory2, "Packages"),
        Plugin("Services", Icons.Filled.Dns, "Services"),
        Plugin("Permissions", Icons.Filled.VerifiedUser, "Permissions"),
        Plugin("Signature", Icons.Filled.Key, "Signature"),
        Plugin("Automation", Icons.Filled.Settings, "Automation")
    )
    Column(
        Modifier.width(120.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        plugins.forEach { plugin ->
            val isSelected = plugin.name == selectedPlugin
            val background = if (isSelected) Color(0xFF4A4E69).copy(alpha = 0.5f) else Color.Transparent
            val shape = MaterialTheme.shapes.large.copy(all = androidx.compose.foundation.shape.CornerSize(24.dp))
            val horizontalPadding = 16.dp // Increased margin
            val verticalPadding = 8.dp
            val internalPadding = if (isSelected) PaddingValues(horizontal = 0.dp, vertical = 4.dp) else PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .height(72.dp)
                    .background(background, shape = shape)
                    .clickable { onPluginSelected(plugin.name) },
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
                        contentDescription = plugin.label,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        plugin.label,
                        color = Color.White,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun ContentArea(selectedPlugin: String, selectedDevice: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Content Area", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Selected Plugin: $selectedPlugin")
        Text("Selected Device: ${selectedDevice ?: "None"}")
    }
}

@Composable
fun LoadingPill() {
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
fun ErrorMessage(message: String, onDismiss: () -> Unit) {
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

@Composable
fun SettingsDialog(onSave: (String) -> Unit, onCancel: () -> Unit) {
    var adbPath by remember { mutableStateOf("") }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xAA22223B)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            shadowElevation = 16.dp,
            color = Color.White,
            modifier = Modifier.width(400.dp)
        ) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Configure ADB Path", style = MaterialTheme.typography.titleLarge, color = Color(0xFF22223B))
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = adbPath,
                    onValueChange = { adbPath = it },
                    label = { Text("ADB Path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSave(adbPath) }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun CompanionDialog(onInstall: () -> Unit, onSkip: () -> Unit, onCancel: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xAA22223B)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            shadowElevation = 16.dp,
            color = Color.White,
            modifier = Modifier.width(400.dp)
        ) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Install Companion App", style = MaterialTheme.typography.titleLarge, color = Color(0xFF22223B))
                Spacer(Modifier.height(24.dp))
                Text("The companion app is required for full functionality. Would you like to install it now?", color = Color(0xFF22223B))
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onSkip) { Text("Skip") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onInstall) { Text("Install") }
                }
            }
        }
    }
}
