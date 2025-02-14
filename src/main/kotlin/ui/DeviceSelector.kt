package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import models.AdbDevice
import ui.definitions.Dimens
import ui.definitions.Texts.Companion.SELECT_DEVICE
import ui.definitions.Texts.Companion.UNKNOWN

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceSelector(
    devices: List<AdbDevice>,
    selected: AdbDevice?,
    modifier: Modifier = Modifier,
    onSelect: (AdbDevice) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize(Alignment.TopEnd).padding(0.dp, 2.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            Row(
                Modifier.padding(start = Dimens.HORIZONTAL_SPACER.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                selected?.run { DeviceOption(this, Modifier.weight(1f)) } ?: Text(SELECT_DEVICE)
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
            Surface(color = MaterialTheme.colors.primaryVariant) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = !expanded },
                ) {
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

@Composable
private fun DeviceOption(
    device: AdbDevice,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = Dimens.VERTICAL_SPACER.dp)) {
        Text(
            device.name ?: UNKNOWN,
            style = MaterialTheme.typography.body1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Text(device.id, style = MaterialTheme.typography.caption)
    }
}
