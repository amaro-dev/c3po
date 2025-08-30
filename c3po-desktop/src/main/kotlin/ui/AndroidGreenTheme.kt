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

// Light theme semantic colors
private val AndroidGreenSuccessLight = Color(0xFF2E7D32) // Dark green for light theme
private val AndroidGreenWarningLight = Color(0xFFED6C02) // Dark orange for light theme
private val AndroidGreenInfoLight = Color(0xFF0288D1) // Dark blue for light theme
private val AndroidGreenDangerLight = Color(0xFFD32F2F) // Dark red for light theme
private val AndroidGreenMutedLight = Color(0xFF6B7280) // Gray for light theme

// Dark theme semantic colors (WCAG AAA compliant - 7:1 contrast ratio)
private val AndroidGreenSuccessDark = Color(0xFF7FCC7F) // WCAG AAA compliant light green  
private val AndroidGreenWarningDark = Color(0xFFFFB366) // WCAG AAA compliant light orange
private val AndroidGreenInfoDark = Color(0xFF66B3FF) // WCAG AAA compliant light blue
private val AndroidGreenDangerDark = Color(0xFFF29999) // WCAG AAA compliant light red
private val AndroidGreenMutedDark = Color(0xFFB3B3B3) // WCAG AAA compliant light gray

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
    tertiary = AndroidGreenSuccessLight, // Used for success states
    onTertiary = Color.White,
    tertiaryContainer = AndroidGreenSuccessLight.copy(alpha = 0.2f),
    onTertiaryContainer = AndroidGreenSuccessLight
)

private val DarkColors = darkColorScheme(
    primary = AndroidGreenSuccessDark, // WCAG AAA compliant light green
    onPrimary = Color.Black, // Black text on light green background
    secondary = AndroidGreenSurface,
    onSecondary = Color.White,
    background = Color(0xFF414141),
    onBackground = Color.White,
    surface = Color(0xFF525252),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3A3A3A), // Darker variant for dark theme
    onSurfaceVariant = Color.White,
    error = AndroidGreenDangerDark, // WCAG AAA compliant light red
    onError = Color.Black, // Black text on light red background
    // Custom semantic colors for dark theme - using lighter colors
    tertiary = AndroidGreenSuccessDark, // Lighter success color for dark theme
    onTertiary = Color.Black, // Dark text on light success background
    tertiaryContainer = AndroidGreenSuccessDark.copy(alpha = 0.3f),
    onTertiaryContainer = AndroidGreenSuccessDark
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

// Theme-aware semantic colors (automatically light/dark appropriate)
val ColorScheme.successColor: Color
    get() = if (background.red < 0.5f) AndroidGreenSuccessDark else AndroidGreenSuccessLight

val ColorScheme.warningColor: Color
    get() = if (background.red < 0.5f) AndroidGreenWarningDark else AndroidGreenWarningLight

val ColorScheme.infoColor: Color
    get() = if (background.red < 0.5f) AndroidGreenInfoDark else AndroidGreenInfoLight

val ColorScheme.dangerColor: Color
    get() = if (background.red < 0.5f) AndroidGreenDangerDark else AndroidGreenDangerLight

val ColorScheme.mutedColor: Color
    get() = if (background.red < 0.5f) AndroidGreenMutedDark else AndroidGreenMutedLight

// Text colors on semantic backgrounds (theme-aware)
val ColorScheme.onSuccessColor: Color
    get() = if (background.red < 0.5f) Color.Black else Color.White

val ColorScheme.onWarningColor: Color
    get() = if (background.red < 0.5f) Color.Black else Color.White

val ColorScheme.onInfoColor: Color
    get() = if (background.red < 0.5f) Color.Black else Color.White

val ColorScheme.onDangerColor: Color
    get() = if (background.red < 0.5f) Color.Black else Color.White

val ColorScheme.onMutedColor: Color
    get() = if (background.red < 0.5f) Color.Black else Color.White

// Enhanced semantic color extensions for better dark mode support
val ColorScheme.emphasizedSuccessColor: Color
    get() = if (background.red < 0.5f) AndroidGreenSuccessDark else AndroidGreenSuccessLight.copy(alpha = 0.9f)

val ColorScheme.emphasizedWarningColor: Color
    get() = if (background.red < 0.5f) AndroidGreenWarningDark else AndroidGreenWarningLight.copy(alpha = 0.9f)

val ColorScheme.emphasizedDangerColor: Color
    get() = if (background.red < 0.5f) AndroidGreenDangerDark else AndroidGreenDangerLight.copy(alpha = 0.9f)

val ColorScheme.emphasizedInfoColor: Color
    get() = if (background.red < 0.5f) AndroidGreenInfoDark else AndroidGreenInfoLight.copy(alpha = 0.9f)