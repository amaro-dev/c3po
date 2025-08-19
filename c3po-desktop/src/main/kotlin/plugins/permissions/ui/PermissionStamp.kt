package plugins.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.command.PermissionFlag

@Composable
fun PermissionStamp(permissionFlag: PermissionFlag) {
    Text(
        text = permissionFlag.name,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        ),
        color = permissionFlag.getPermissionForegroundColor(),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, permissionFlag.getPermissionBorderColor(), RoundedCornerShape(6.dp))
            .background(permissionFlag.getPermissionBackgroundColor())
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}