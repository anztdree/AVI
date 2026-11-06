package com.anztdree.avi.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.anztdree.avi.AviState
import com.anztdree.avi.ui.theme.AviPalette
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * "Wajah" AVI — visor horizontal dengan dua mata glow.
 * Canvas murni, ringan; dipanggil sebagai overlay Box di atas reaktor.
 */
@Composable
fun AviVisor(
    state: AviState,
    micLevel: Float,
    voiceLevel: Float,
    palette: AviPalette,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "visor")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "time"
    )
    Canvas(modifier = modifier) {
        drawVisor(
            state = state,
            mic = micLevel.coerceIn(0f, 1f),
            voice = voiceLevel.coerceIn(0f, 1f),
            palette = palette,
            time = time
        )
    }
}

private fun DrawScope.drawVisor(
    state: AviState,
    mic: Float,
    voice: Float,
    palette: AviPalette,
    time: Float
) {
    val TAU = (2.0 * PI).toFloat()
    val w = size.width
    val h = size.height
    val bandTop = Offset(0f, h * 0.18f)
    val bandSize = Size(w, h * 0.64f)
    val corner = CornerRadius(h * 0.32f, h * 0.32f)

    // Band visor gelap + tepi bercahaya accent
    drawRoundRect(color = Color(0xFF04070D), topLeft = bandTop, size = bandSize, cornerRadius = corner)
    drawRoundRect(
        color = palette.accent.copy(alpha = 0.75f),
        topLeft = bandTop,
        size = bandSize,
        cornerRadius = corner,
        style = Stroke(width = h * 0.05f)
    )
    drawRoundRect(
        color = palette.accent.copy(alpha = 0.10f),
        topLeft = Offset(w * 0.05f, bandTop.y + h * 0.10f),
        size = Size(w * 0.9f, bandSize.height * 0.8f),
        cornerRadius = corner
    )

    // Parameter mata per state
    var eyeShift = 0f
    var baseAlpha = 0.8f
    var scaleY = 1f
    var leftMul = 1f
    var rightMul = 1f
    var glowBoost = 0.55f
    var eyeR = h * 0.16f
    when (state) {
        AviState.IDLE -> {
            baseAlpha = 0.5f + 0.3f * (0.5f + 0.5f * sin(time * TAU))
            // blink tiap ±4 dtk — scale Y cepat
            scaleY = when {
                time < 0.90f -> 1f
                time < 0.93f -> 1f - ((time - 0.90f) / 0.03f) * 0.9f
                time < 0.96f -> 0.1f + ((time - 0.93f) / 0.03f) * 0.9f
                else -> 1f
            }
        }
        AviState.LISTENING -> {
            // melirik kiri-kanan pelan + glow mengikuti mic
            eyeShift = sin(time * TAU * 0.75f) * w * 0.055f
            baseAlpha = 0.7f + 0.3f * mic
            glowBoost = 0.5f + 0.5f * mic
        }
        AviState.THINKING -> {
            // mata berkedip cepat bergantian (scan)
            val s = abs(sin(time * TAU * 6f))
            leftMul = 0.35f + 0.65f * s
            rightMul = 0.35f + 0.65f * (1f - s)
            scaleY = 0.85f
            glowBoost = 0.6f
        }
        AviState.SPEAKING -> {
            baseAlpha = 0.6f + 0.4f * voice
            scaleY = 0.7f + 0.5f * voice
            glowBoost = 0.4f + 0.6f * voice
        }
        AviState.LIVE -> {
            baseAlpha = 1f
            glowBoost = 1f
            eyeR = h * 0.18f
        }
    }

    val eyeY = bandTop.y + bandSize.height * 0.5f
    val gap = w * 0.17f
    val eyes = listOf(Pair(-1f, leftMul), Pair(1f, rightMul))
    eyes.forEach { eye ->
        val side = eye.first
        val mul = eye.second
        val ex = w / 2f + side * gap + eyeShift
        val alpha = (baseAlpha * mul).coerceIn(0f, 1f)
        val glowR = eyeR * 2.6f
        scale(scaleX = 1f, scaleY = scaleY, pivot = Offset(ex, eyeY)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.accent.copy(alpha = 0.55f * alpha * glowBoost), Color.Transparent),
                    center = Offset(ex, eyeY),
                    radius = glowR
                ),
                radius = glowR,
                center = Offset(ex, eyeY)
            )
            drawCircle(color = palette.accent.copy(alpha = alpha), radius = eyeR, center = Offset(ex, eyeY))
            drawCircle(
                color = Color.White.copy(alpha = 0.85f * alpha),
                radius = eyeR * 0.38f,
                center = Offset(ex, eyeY)
            )
        }
    }
}
