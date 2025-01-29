package ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors =
            Colors(
                primary = Color(0xFFb1f1c1),
                primaryVariant = Color(0xFF2e6a44),
                onPrimary = Color(0xFF12512e),
                secondary = Color(0xFFffdf9a),
                secondaryVariant = Color(0xFF765a0b),
                onSecondary = Color(0xFF5a4300),
                error = Color(0xFFba1a1a),
                onError = Color(0xFFffffff),
                surface = Color(0xFFf7f9ff),
                onSurface = Color(0xFF181c20),
                background = Color(0xFFffffff),
                onBackground = Color(0xFF42474e),
                isLight = true,
            ),
    ) {
        content()
    }
}
