package plugins.device.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.model.DiskPartition
import ui.OnAction

@Composable
fun DiskUsageCard(
    title: String,
    diskPartitions: List<DiskPartition>,
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

            if (diskPartitions.isEmpty()) {
                Text(
                    text = "No disk usage data available",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    diskPartitions.filter { it.mountPoint.startsWith("/") && it.total.toIntOrNull() != null }
                        .forEach { partition ->
                            DiskPartitionRow(partition = partition)
                        }
                }
            }
        }
    }
}

@Composable
private fun DiskPartitionRow(partition: DiskPartition) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partition.mountPoint,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = partition.partition,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${partition.percentage}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = when {
                        partition.percentage >= 90 -> MaterialTheme.colorScheme.error
                        partition.percentage >= 75 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${formatBytes(partition.used)}/${formatBytes(partition.total)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        LinearProgressIndicator(
            progress = partition.percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = when {
                partition.percentage >= 90 -> MaterialTheme.colorScheme.error
                partition.percentage >= 75 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

private fun formatBytes(value: String): String {
    val kb = value.toIntOrNull() ?: return value
    return when {
        kb >= 1024 * 1024 -> String.format("%.1f GB", kb / (1024.0 * 1024.0))
        kb >= 1024 -> String.format("%.0f MB", kb / 1024.0)
        else -> "$kb kB"
    }
}