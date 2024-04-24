package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.AdbDevice
import models.DeviceAttrs

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceSelector(devices: List<AdbDevice>) {
    var expanded by remember { mutableStateOf(false) }
    var selection: AdbDevice? by remember { mutableStateOf(null) }
    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopEnd)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colors.primaryVariant, MaterialTheme.shapes.medium)
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceOption(devices[0], Modifier.weight(1f))
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = !expanded },
                modifier = Modifier.background(MaterialTheme.colors.primaryVariant)
            ) {
                Surface(color = MaterialTheme.colors.primaryVariant) {
                    devices.forEach {
                        DropdownMenuItem({ selection = it }) {
                            DeviceOption(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceOption(device: AdbDevice, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            device.details[DeviceAttrs.Name.key] ?: "<unknown>",
            style = MaterialTheme.typography.body1
        )
        Text(device.id, style = MaterialTheme.typography.caption)
    }
}
