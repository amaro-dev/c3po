package plugins.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import commands.PermissionFlag
import ui.baselinePadding

@Composable
fun PermissionStamp(permissionFlag: PermissionFlag) {
    Text(
        text = permissionFlag.name,
        style = MaterialTheme.typography.overline.copy(fontSize = TextUnit(8f, TextUnitType.Sp)),
        color = permissionFlag.getPermissionForegroundColor(),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, permissionFlag.getPermissionBorderColor())
            .background(permissionFlag.getPermissionBackgroundColor())
            .baselinePadding(2)
            .padding(start = 6.dp, end = 6.dp)
    )
}
