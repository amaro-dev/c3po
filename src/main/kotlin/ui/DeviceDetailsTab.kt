package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.AdbDevice
import models.DeviceAttrs


@Composable
fun DeviceDetailsTab(currentDevice: AdbDevice?) {
    Column(
        Modifier
            .padding(16.dp, 12.dp)
            .fillMaxHeight()
            .border(width = 1.dp, color = MaterialTheme.colors.onBackground)
    ) {
        DeviceAttrs.entries.forEach {
            DeviceAttrRow("${it.name}:", currentDevice?.details?.run { this[it.key] })
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onBackground))
        }
    }
}
