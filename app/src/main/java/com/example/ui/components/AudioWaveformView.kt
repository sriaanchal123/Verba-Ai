package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryCyanLight
import com.example.ui.theme.SecondaryIndigoLight

@Composable
fun AudioWaveformView(
    waveformPoints: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = (width / (barCount * 1.5f)).coerceAtLeast(4f)
        val space = (width - (barCount * barWidth)) / (barCount + 1)

        val gradient = Brush.verticalGradient(
            colors = listOf(PrimaryCyanLight, SecondaryIndigoLight, Color(0xFFC084FC))
        )

        for (i in 0 until barCount) {
            val pointIndex = if (waveformPoints.isNotEmpty()) {
                val ratio = i.toFloat() / barCount
                (ratio * waveformPoints.size).toInt().coerceIn(0, waveformPoints.size - 1)
            } else 0

            val rawAmplitude = if (waveformPoints.isNotEmpty()) waveformPoints[pointIndex] else 0.15f
            val amplitude = if (isRecording) {
                (rawAmplitude * pulseAnim).coerceIn(0.08f, 1.0f)
            } else {
                0.12f
            }

            val barHeight = (height * 0.9f * amplitude).coerceAtLeast(6f)
            val left = space + i * (barWidth + space)
            val top = (height - barHeight) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
