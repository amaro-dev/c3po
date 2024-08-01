package ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(content: @Composable ( ) -> Unit) {
    MaterialTheme(
        colors = Colors(
            primary = Color(0xFF2d638b),
            primaryVariant = Color(0xFF001d32),
            onPrimary = Color(0xFFffffff),
            secondary =     Color(0xFF51606f),
            secondaryVariant = Color(0xFF0d1d2a),
            onSecondary = Color(0xFFffffff),
            error = Color(0xFFba1a1a),
            onError = Color(0xFFffffff),
            surface = Color(0xFFf7f9ff),
            onSurface = Color(0xFF181c20),
            background = Color(0xFFffffff),
            onBackground = Color(0xFF42474e),
            isLight = true,
        )
    ){
        content()
    }
}
