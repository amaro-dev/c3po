package ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AndroidGreenLight = Color(0xFF218c4a) // More contrast for sidebar and primary
private val AndroidGreenPrimaryText = Color(0xFFFFFFFF) // White text for contrast
private val AndroidGreenDark = Color(0xFF1B5E20)
private val AndroidGreenSurface = Color(0xFF2E7D32)
private val AndroidGreenOnSurface = Color(0xFFFFFFFF)
private val AndroidGreenError = Color(0xFFD32F2F)

private val LightColors = lightColorScheme(
    primary = AndroidGreenLight,
    onPrimary = AndroidGreenPrimaryText,
    secondary = AndroidGreenSurface,
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF22223B),
    surface = Color.White,
    onSurface = AndroidGreenOnSurface,
    error = AndroidGreenError,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = AndroidGreenDark,
    onPrimary = Color.White,
    secondary = AndroidGreenSurface,
    onSecondary = Color.White,
    background = Color(0xFF22223B),
    onBackground = Color.White,
    surface = AndroidGreenSurface,
    onSurface = Color.White,
    error = AndroidGreenError,
    onError = Color.White
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
