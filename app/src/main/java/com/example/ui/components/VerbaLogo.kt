package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepIndigo
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.WarmAmber

@Composable
fun VerbaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "verba_logo_transition")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scaleMultiplier = if (animated) pulse else 1f

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width * scaleMultiplier
            val h = this.size.height * scaleMultiplier
            val offsetX = (this.size.width - w) / 2f
            val offsetY = (this.size.height - h) / 2f

            // Soft tactile rounded card background
            drawRoundRect(
                color = Color(0xFFF1F5F9),
                topLeft = Offset(offsetX + w * 0.08f, offsetY + h * 0.08f),
                size = Size(w * 0.84f, h * 0.84f),
                cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)
            )

            // Left Folio Page (Transforming Knowledge)
            val leftPage = Path().apply {
                moveTo(offsetX + w * 0.24f, offsetY + h * 0.32f)
                cubicTo(
                    offsetX + w * 0.34f, offsetY + h * 0.28f,
                    offsetX + w * 0.44f, offsetY + h * 0.30f,
                    offsetX + w * 0.47f, offsetY + h * 0.34f
                )
                lineTo(offsetX + w * 0.47f, offsetY + h * 0.74f)
                cubicTo(
                    offsetX + w * 0.44f, offsetY + h * 0.70f,
                    offsetX + w * 0.34f, offsetY + h * 0.68f,
                    offsetX + w * 0.24f, offsetY + h * 0.72f
                )
                close()
            }
            drawPath(
                path = leftPage,
                brush = Brush.verticalGradient(
                    colors = listOf(DeepIndigo, Color(0xFF1D4ED8)),
                    startY = offsetY + h * 0.3f,
                    endY = offsetY + h * 0.74f
                )
            )

            // Right Folio Page
            val rightPage = Path().apply {
                moveTo(offsetX + w * 0.51f, offsetY + h * 0.34f)
                cubicTo(
                    offsetX + w * 0.54f, offsetY + h * 0.30f,
                    offsetX + w * 0.64f, offsetY + h * 0.28f,
                    offsetX + w * 0.74f, offsetY + h * 0.32f
                )
                lineTo(offsetX + w * 0.74f, offsetY + h * 0.72f)
                cubicTo(
                    offsetX + w * 0.64f, offsetY + h * 0.68f,
                    offsetX + w * 0.54f, offsetY + h * 0.70f,
                    offsetX + w * 0.51f, offsetY + h * 0.74f
                )
                close()
            }
            drawPath(
                path = rightPage,
                brush = Brush.verticalGradient(
                    colors = listOf(IndigoLight, SlateNavy),
                    startY = offsetY + h * 0.3f,
                    endY = offsetY + h * 0.74f
                )
            )

            // Soundwave / Clarity Text Lines on Left Page
            val waveStroke = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(offsetX + w * 0.30f, offsetY + h * 0.45f),
                end = Offset(offsetX + w * 0.41f, offsetY + h * 0.45f),
                strokeWidth = waveStroke.width,
                cap = waveStroke.cap
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(offsetX + w * 0.30f, offsetY + h * 0.54f),
                end = Offset(offsetX + w * 0.41f, offsetY + h * 0.54f),
                strokeWidth = waveStroke.width,
                cap = waveStroke.cap
            )

            // Right Page Lines
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(offsetX + w * 0.57f, offsetY + h * 0.45f),
                end = Offset(offsetX + w * 0.68f, offsetY + h * 0.45f),
                strokeWidth = waveStroke.width,
                cap = waveStroke.cap
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(offsetX + w * 0.57f, offsetY + h * 0.54f),
                end = Offset(offsetX + w * 0.68f, offsetY + h * 0.54f),
                strokeWidth = waveStroke.width,
                cap = waveStroke.cap
            )

            // Clarity Star / Spark Prism Emerging from Center
            val starPath = Path().apply {
                val cx = offsetX + w * 0.49f
                val cy = offsetY + h * 0.22f
                val r = w * 0.11f
                moveTo(cx, cy - r)
                cubicTo(cx, cy - r * 0.25f, cx + r * 0.25f, cy, cx + r, cy)
                cubicTo(cx + r * 0.25f, cy, cx, cy + r * 0.25f, cx, cy + r)
                cubicTo(cx, cy + r * 0.25f, cx - r * 0.25f, cy, cx - r, cy)
                cubicTo(cx - r * 0.25f, cy, cx, cy - r * 0.25f, cx, cy - r)
                close()
            }
            drawPath(path = starPath, color = SageGreen)

            // Small Accent Sparkle
            val smallStar = Path().apply {
                val cx = offsetX + w * 0.68f
                val cy = offsetY + h * 0.20f
                val r = w * 0.05f
                moveTo(cx, cy - r)
                cubicTo(cx, cy, cx, cy, cx + r, cy)
                cubicTo(cx, cy, cx, cy, cx, cy + r)
                cubicTo(cx, cy, cx, cy, cx - r, cy)
                cubicTo(cx, cy, cx, cy, cx, cy - r)
                close()
            }
            drawPath(path = smallStar, color = WarmAmber)
        }
    }
}
