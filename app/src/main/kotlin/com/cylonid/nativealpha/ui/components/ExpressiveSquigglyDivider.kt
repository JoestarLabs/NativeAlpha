package com.cylonid.nativealpha.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A native Material 3 Expressive squiggly / wavy divider.
 * Draws a smooth sinusoidal bezier wave across the available width.
 */
@Composable
fun ExpressiveSquigglyDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
    waveLength: Dp = 22.dp,
    waveAmplitude: Dp = 3.5.dp,
    strokeWidth: Dp = 2.dp,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(waveAmplitude * 2 + strokeWidth + 4.dp),
    ) {
        val width = size.width
        val midY = size.height / 2f
        val wl = waveLength.toPx()
        val amp = waveAmplitude.toPx()

        if (width <= 0f || wl <= 0f) return@Canvas

        val cycleCount = (width / wl).roundToInt().coerceAtLeast(1)
        val actualWl = width / cycleCount

        val path = Path()
        path.moveTo(0f, midY)

        for (i in 0 until cycleCount) {
            // First half of wave (peak)
            path.relativeQuadraticTo(actualWl / 4f, -amp, actualWl / 2f, 0f)
            // Second half of wave (trough)
            path.relativeQuadraticTo(actualWl / 4f, amp, actualWl / 2f, 0f)
        }

        drawPath(
            path = path,
            color = color,
            style =
                Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
        )
    }
}
