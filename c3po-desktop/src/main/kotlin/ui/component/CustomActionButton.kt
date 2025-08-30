package ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ui.highEmphasisAlpha
import ui.hoverAlpha
import ui.pressedAlpha

/**
 * Configuração para posicionamento do tooltip
 */
enum class TooltipPosition {
    TOP, BOTTOM, LEFT, RIGHT, CURSOR_POINT
}

/**
 * Tipo de tooltip a ser usado
 */
enum class TooltipType {
    PLAIN, RICH
}

/**
 * Configuração avançada para tooltips
 */
data class TooltipConfig(
    val position: TooltipPosition = TooltipPosition.TOP,
    val delayMillis: Int = 600,
    val type: TooltipType = TooltipType.PLAIN,
    val isPersistent: Boolean = false,
    val enableHover: Boolean = true,
    val enableFocus: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tooltipText: String? = contentDescription,
    tooltipConfig: TooltipConfig = TooltipConfig(TooltipPosition.CURSOR_POINT)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isPressed -> MaterialTheme.colorScheme.pressedAlpha
        isHovered -> MaterialTheme.colorScheme.hoverAlpha
        else -> Color.Transparent
    }

    val iconTint = when {
        isPressed -> MaterialTheme.colorScheme.highEmphasisAlpha
        else -> MaterialTheme.colorScheme.primary
    }

    val buttonContent: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
                tint = iconTint
            )
        }
    }

    if (tooltipText != null) {
        TooltipArea(
            tooltip = {
                Surface(
                    modifier = Modifier.shadow(4.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = RoundedCornerShape(4.dp),
                    tonalElevation = 6.dp
                ) {
                    Text(
                        text = tooltipText,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            },
            delayMillis = tooltipConfig.delayMillis,
            tooltipPlacement = getDesktopTooltipPlacement(tooltipConfig.position)
        ) {
            buttonContent()
        }
    } else {
        buttonContent()
    }
}


/**
 * Obtém o TooltipPlacement apropriado para Compose Desktop baseado na configuração
 */
@OptIn(ExperimentalFoundationApi::class)
private fun getDesktopTooltipPlacement(position: TooltipPosition): TooltipPlacement = when (position) {
    TooltipPosition.TOP -> TooltipPlacement.ComponentRect(
        anchor = Alignment.TopCenter,
        alignment = Alignment.BottomCenter,
        offset = DpOffset(0.dp, (-4).dp)
    )

    TooltipPosition.BOTTOM -> TooltipPlacement.ComponentRect(
        anchor = Alignment.BottomCenter,
        alignment = Alignment.TopCenter,
        offset = DpOffset(0.dp, 4.dp)
    )

    TooltipPosition.LEFT -> TooltipPlacement.ComponentRect(
        anchor = Alignment.CenterStart,
        alignment = Alignment.CenterEnd,
        offset = DpOffset((-4).dp, 0.dp)
    )

    TooltipPosition.RIGHT -> TooltipPlacement.ComponentRect(
        anchor = Alignment.CenterEnd,
        alignment = Alignment.CenterStart,
        offset = DpOffset(4.dp, 0.dp)
    )

    TooltipPosition.CURSOR_POINT -> TooltipPlacement.CursorPoint(
        alignment = Alignment.TopCenter,
        offset = DpOffset(0.dp, (-8).dp)
    )
}
