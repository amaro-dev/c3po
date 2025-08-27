package plugins.device.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.model.Action
import ui.CopyButton
import ui.OnAction
import ui.onHover
import ui.slideInHorizontallyFromRight
import ui.slideOutHorizontallyToRight

@Composable
fun DeviceInfoCard(
    title: String,
    items: List<DeviceInfoItem>,
    onAction: OnAction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            items.forEach { item ->
                DeviceInfoRow(
                    icon = item.icon,
                    label = item.label,
                    value = item.value,
                    copyable = item.copyable,
                    onAction = onAction
                )
                if (item != items.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(
    icon: String,
    label: String,
    value: String,
    onAction: OnAction,
    copyable: Boolean = true
) {
    var isHoveringValue: Boolean by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onHover { isHoveringValue = it }
                    )
                }
            }
        }

        if (copyable) {
            CopyButton(
                visible = isHoveringValue,
                onHover = { isHoveringValue = it },
                onClick = { onAction(Action.CopyText(value)) },
                modifier = Modifier.align(Alignment.CenterEnd),
                enterAnimation = slideInHorizontallyFromRight(),
                exitAnimation = slideOutHorizontallyToRight()
            )
        }
    }
}

data class DeviceInfoItem(
    val icon: String,
    val label: String,
    val value: String,
    val copyable: Boolean = true
)