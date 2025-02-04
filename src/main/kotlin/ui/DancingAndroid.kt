package ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun RunningAndroid() {
    val androidColor = Color(0xFF3DDC84)
    val androidColorDark = Color(0xFF34bf72)
    val androidColorDarker = Color(0xFF1f804a)

    val infiniteTransition = rememberInfiniteTransition()

    val offset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f, // Altura do pulo
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 200
            },
            repeatMode = RepeatMode.Reverse
        )
    )
    val earsRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = max(size.width, size.height) / 2
            val canvasHeight = canvasWidth
            val centerX = canvasWidth / 2f

            val anim = offset / 100f
            val top = canvasHeight * 0.4f
            val headRadius = canvasHeight * 0.375f
            val earLength = canvasHeight * 0.25f
            val earThick = canvasHeight * 0.05f
            val eyesRadius = canvasHeight * 0.03125f
            val eyesPlace = centerX + headRadius * 0.4f + eyesRadius * 3 * anim
            val topBody = top + headRadius + 1
            val bodyWidth = canvasWidth * 0.75f
            val bodyHeight = canvasHeight / 2f
            val topLeg = topBody + bodyHeight
            val legLength = canvasHeight * 0.25f
            val legThick = canvasWidth * 0.2f
            val legPlaceLeft = centerX + (bodyWidth / 2f) * anim * -1
            val legPlaceRight = centerX + (bodyWidth / 2f) * anim
            val armLength = canvasHeight * 0.25f
            val armThick = canvasWidth * 0.125f
            val armPlaceLeftX = (centerX + armLength) + armLength * anim * -1
            val armPlaceLeftY = (topBody + armThick * 2) + armLength * anim
            val armPlaceRightX = (centerX + armLength) + armLength * anim
            val armPlaceRightY = (topBody + armThick * 2) + armLength * anim * -1

            // Antena Esquerda
            withTransform({
                rotate(-15f + earsRotation * -1)
                translate(top = earLength / 2, left = -headRadius / 2)
            }) {
                drawLine(
                    color = androidColorDarker,
                    start = Offset(centerX + headRadius, top),
                    end = Offset(centerX + headRadius, top - earLength),
                    cap = StrokeCap.Round,
                    strokeWidth = earThick,
                )
            }

            // Cabeça
            drawArc(
                color = androidColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                size = Size(headRadius * 2, headRadius * 2),
                topLeft = Offset(centerX - headRadius, top)
            )

            // Antena Direita
            withTransform({
                rotate(-15f + earsRotation)
                translate(top = earLength / 2, left = -headRadius / 2)
            }) {
                drawLine(
                    color = androidColorDark,
                    start = Offset(centerX + headRadius, top),
                    end = Offset(centerX + headRadius, top - earLength),
                    cap = StrokeCap.Round,
                    strokeWidth = earThick,
                )
            }

            // Olhos
            drawArc(
                color = Color.Black,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true,
                size = Size(eyesRadius * 2, eyesRadius * 2),
                topLeft = Offset(eyesPlace, top + headRadius * 0.5f)
            )

            // Perna esquerda
            drawLine(
                color = androidColorDarker,
                start = Offset(legPlaceLeft, topLeg),
                end = Offset(legPlaceLeft, topLeg + legLength),
                cap = StrokeCap.Round,
                strokeWidth = legThick,
            )

            // Braço esquerdo
            drawLine(
                color = androidColorDarker,
                start = Offset(armPlaceLeftX, armPlaceLeftY),
                end = Offset(armPlaceLeftX + armLength, armPlaceLeftY),
                cap = StrokeCap.Round,
                strokeWidth = armThick,
            )

            // Corpo
            drawRect(
                color = androidColor,
                topLeft = Offset(centerX - bodyWidth / 2, topBody),
                size = Size(bodyWidth, bodyHeight)
            )

            // Braço direito
            drawLine(
                color = androidColorDark,
                start = Offset(armPlaceRightX, armPlaceRightY),
                end = Offset(armPlaceRightX + armLength, armPlaceRightY),
                cap = StrokeCap.Round,
                strokeWidth = armThick,
            )

            // Perna direita
            drawLine(
                color = androidColorDark,
                start = Offset(legPlaceRight, topLeg),
                end = Offset(legPlaceRight, topLeg + legLength),
                cap = StrokeCap.Round,
                strokeWidth = legThick,
            )
        }
    }
}

@Preview
@Composable
private fun previewAndroid() {
    Box(Modifier.size(64.dp).border(1.dp, Color.Black)) {
        RunningAndroid()
    }
}
