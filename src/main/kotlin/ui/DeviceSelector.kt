package ui

import Settings.NAME_SYSTEM_PROP
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.AdbDevice
import ui.Texts.Companion.SELECT_DEVICE
import ui.Texts.Companion.UNKNOWN

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceSelector(devices: List<AdbDevice>, selected: AdbDevice?, onSelect: (AdbDevice) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopEnd)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            Surface(color = MaterialTheme.colors.primary) {
                Row(
                    Modifier.fillMaxWidth().padding(start = Dimens.HORIZONTAL_SPACER.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    selected?.run { DeviceOption(this, Modifier.weight(1f)) } ?: Text(SELECT_DEVICE)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = !expanded },
                modifier = Modifier.background(MaterialTheme.colors.primaryVariant).fillMaxWidth()
            ) {
                Surface(color = MaterialTheme.colors.primaryVariant) {
                    Column(Modifier.height(300.dp)) {
                        devices.forEach {
                            DropdownMenuItem({
                                onSelect(it)
                                expanded = false
                            }) {
                                DeviceOption(it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceOption(device: AdbDevice, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(bottom = Dimens.VERTICAL_SPACER.dp)) {
        Text(
            device.details[NAME_SYSTEM_PROP] ?: UNKNOWN,
            style = MaterialTheme.typography.body1
        )
        Text(device.id, style = MaterialTheme.typography.caption)
    }
}
