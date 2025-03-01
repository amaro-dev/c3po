package ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

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
        typography = Typography(
            body1 = MaterialTheme.typography.body1.copy(
                fontFamily = fontFamily,
                fontSize = 13.sp
            ),
            body2 = MaterialTheme.typography.body2.copy(
                fontSize = 12.sp,
                fontFamily = fontFamily,
            ),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = fontFamily,
                letterSpacing = 1.sp
            ),
            subtitle2 = MaterialTheme.typography.subtitle2.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = fontFamily
            ),
            button = MaterialTheme.typography.button.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            ),
            caption = MaterialTheme.typography.caption.copy(
                fontSize = 10.sp
            ),
            overline = MaterialTheme.typography.overline.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Light
            ),
            h1 = MaterialTheme.typography.h1.copy(
                fontFamily = fontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            h2 = MaterialTheme.typography.h2.copy(
                fontFamily = fontFamily,
                fontSize = 18.sp
            ),
            h3 = MaterialTheme.typography.h3.copy(
                fontFamily = fontFamily,
                fontSize = 16.sp, fontWeight = FontWeight.Bold
            ),
            h4 = MaterialTheme.typography.h4.copy(
                fontFamily = fontFamily,
                fontSize = 16.sp
            ),
            h5 = MaterialTheme.typography.h5.copy(
                fontFamily = fontFamily,
                fontSize = 14.sp, fontWeight = FontWeight.Bold
            ),
            h6 = MaterialTheme.typography.h6.copy(
                fontFamily = fontFamily,
                fontSize = 14.sp
            )
        )
    ) {
        content()
    }
}

val fontFamily = FontFamily(
    Font(
        resource = "Inter/Inter_18pt-Black.ttf",
        weight = FontWeight.Black,
    ),
    Font(
        resource = "Inter/Inter_18pt-ExtraBold.ttf",
        weight = FontWeight.ExtraBold,
    ),
    Font(
        resource = "Inter/Inter_18pt-Bold.ttf",
        weight = FontWeight.Bold
    ),
    Font(
        resource = "Inter/Inter_18pt-SemiBold.ttf",
        weight = FontWeight.SemiBold,
    ),
    Font(
        resource = "Inter/Inter_18pt-Medium.ttf",
        weight = FontWeight.Medium
    ),
    Font(
        resource = "Inter/Inter_18pt-Regular.ttf",
        weight = FontWeight.Normal
    ),
    Font(
        resource = "Inter/Inter_18pt-Light.ttf",
        weight = FontWeight.Light
    ),
    Font(
        resource = "Inter/Inter_18pt-ExtraLight.ttf",
        weight = FontWeight.ExtraLight
    ),
    Font(
        resource = "Inter/Inter_18pt-Thin.ttf",
        weight = FontWeight.Thin
    ),
)
