package ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AndroidGreenLight = Color(0xFF218c4a) // More contrast for sidebar and primary
private val AndroidGreenPrimaryText = Color(0xFFFFFFFF) // White text for contrast
private val AndroidGreenDark = Color(0xFF1B5E20)
private val AndroidGreenSurface = Color(0xFF2E7D32)
private val AndroidGreenError = Color(0xFFD32F2F)

// Semantic colors for permissions and status
private val AndroidGreenSuccess = Color(0xFF2E7D32) // Green for success/normal
private val AndroidGreenWarning = Color(0xFFED6C02) // Orange for warning 
private val AndroidGreenInfo = Color(0xFF0288D1) // Blue for info/signature
private val AndroidGreenDanger = Color(0xFFD32F2F) // Red for danger
private val AndroidGreenMuted = Color(0xFF6B7280) // Gray for muted/unknown

private val LightColors = lightColorScheme(
    primary = AndroidGreenLight,
    onPrimary = AndroidGreenPrimaryText,
    secondary = AndroidGreenSurface,
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF22223B),
    surface = Color.White,
    onSurface = Color(0xFF22223B), // Dark text on white surface
    surfaceVariant = Color(0xFFEEEEEE), // TopBar color for filter cards
    onSurfaceVariant = Color(0xFF22223B), // Dark text on light surface
    error = AndroidGreenError,
    onError = Color.White,
    // Custom semantic colors
    tertiary = AndroidGreenSuccess, // Used for success states
    onTertiary = Color.White,
    tertiaryContainer = AndroidGreenSuccess.copy(alpha = 0.2f),
    onTertiaryContainer = AndroidGreenSuccess
)

private val DarkColors = darkColorScheme(
    primary = AndroidGreenDark,
    onPrimary = Color.White,
    secondary = AndroidGreenSurface,
    onSecondary = Color.White,
    background = Color(0xFF414141),
    onBackground = Color.White,
    surface = Color(0xFF525252),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3A3A3A), // Darker variant for dark theme
    onSurfaceVariant = Color.White,
    error = AndroidGreenError,
    onError = Color.White,
    // Custom semantic colors for dark theme
    tertiary = AndroidGreenSuccess, // Success color works in dark too
    onTertiary = Color.White,
    tertiaryContainer = AndroidGreenSuccess.copy(alpha = 0.3f),
    onTertiaryContainer = AndroidGreenSuccess.copy(alpha = 0.9f)
)

@Composable
fun AndroidGreenTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

// Semantic color extensions for consistent alpha usage
val ColorScheme.dividerColor: Color
    get() = onBackground.copy(alpha = 0.12f)

val ColorScheme.secondaryTextColor: Color
    get() = onSurface.copy(alpha = 0.7f)

val ColorScheme.mutedTextColor: Color
    get() = onSurface.copy(alpha = 0.6f)

val ColorScheme.overlayColor: Color
    get() = scrim.copy(alpha = 0.4f)

val ColorScheme.surfaceVariantAlpha: Color
    get() = surfaceVariant.copy(alpha = 0.5f)

val ColorScheme.primaryAlpha: Color
    get() = primary.copy(alpha = 0.2f)

// Interaction state alpha extensions
val ColorScheme.pressedAlpha: Color
    get() = primary.copy(alpha = 0.15f)

val ColorScheme.hoverAlpha: Color
    get() = primary.copy(alpha = 0.08f)

val ColorScheme.highEmphasisAlpha: Color
    get() = primary.copy(alpha = 0.9f)

val ColorScheme.borderAlpha: Color
    get() = primary.copy(alpha = 0.8f)

// Error state alpha
val ColorScheme.errorAlpha: Color
    get() = error.copy(alpha = 0.7f)

// Permission flag semantic colors
val ColorScheme.successColor: Color
    get() = AndroidGreenSuccess

val ColorScheme.warningColor: Color
    get() = AndroidGreenWarning

val ColorScheme.infoColor: Color
    get() = AndroidGreenInfo

val ColorScheme.dangerColor: Color
    get() = AndroidGreenDanger

val ColorScheme.mutedColor: Color
    get() = AndroidGreenMuted

// Text colors on semantic backgrounds
val ColorScheme.onSuccessColor: Color
    get() = Color.White

val ColorScheme.onWarningColor: Color
    get() = Color.White

val ColorScheme.onInfoColor: Color
    get() = Color.White

val ColorScheme.onDangerColor: Color
    get() = Color.White

val ColorScheme.onMutedColor: Color
    get() = Color.White
