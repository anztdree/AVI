package com.anztdree.avi.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.anztdree.avi.AviState
import com.anztdree.avi.ui.theme.AviCyan
import com.anztdree.avi.ui.theme.AviCyanSoft
import com.anztdree.avi.ui.theme.AviGold
import com.anztdree.avi.ui.theme.AviGoldSoft
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Orb arc reactor ala Iron Man.
 * IDLE      : denyut lambat cyan
 * LISTENING : denyut cepat + halo mengikuti kerasnya suara (rms)
 * THINKING  : cincin berputar cepat + 3 titik orbit emas
 * SPEAKING  : bar equalizer emas bergetar
 */
@Composable
fun ReactorOrb(
    state: AviState,
    rms: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "orb")

    val spinDur = when (state) {
        AviState.THINKING -> 1600
        AviState.SPEAKING -> 2600
        AviState.LISTENING -> 3400
        AviState.IDLE -> 9000
    }
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(spinDur, easing = LinearEasing)),
        label = "angle"
    )
    val angle2 by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween((spinDur * 3).coerceAtMost(10000), easing = LinearEasing)),
        label = "angle2"
    )
    val pulseDur = if (state == AviState.IDLE) 2600 else 900
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(pulseDur, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val wave by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "wave"
    )

    val accent = if (state == AviState.SPEAKING || state == AviState.THINKING) AviGold else AviCyan
    val accentSoft = if (accent == AviGold) AviGoldSoft else AviCyanSoft

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val base = min(size.width, size.height)

        val listening = state == AviState.LISTENING
        val speaking = state == AviState.SPEAKING
        val thinking = state == AviState.THINKING

        // ===== halo luar =====
        val haloAlpha = when {
            listening -> 0.10f + rms * 0.28f
            speaking -> 0.14f + abs(sin(wave)) * 0.10f
            thinking -> 0.16f
            else -> 0.07f + pulse * 0.05f
        }
        val haloRadius = base * 0.48f + (if (listening) rms * base * 0.05f else pulse * base * 0.015f)
        drawCircle(
            color = accent.copy(alpha = haloAlpha),
            radius = haloRadius,
            center = Offset(cx, cy)
        )

        // ===== cincin luar tipis =====
        drawCircle(
            color = accent.copy(alpha = 0.55f),
            radius = base * 0.42f,
            center = Offset(cx, cy),
            style = Stroke(width = base * 0.012f)
        )

        // ===== cincin segmen berputar (3 busur besar) =====
        rotate(degrees = angle, pivot = Offset(cx, cy)) {
            for (i in 0 until 3) {
                drawArc(
                    color = accentSoft.copy(alpha = 0.9f),
                    startAngle = i * 120f,
                    sweepAngle = 64f,
                    useCenter = false,
                    style = Stroke(width = base * 0.022f, cap = StrokeCap.Round)
                )
            }
        }

        // ===== cincin segmen dalam (8 busur kecil, arah berlawanan) =====
        rotate(degrees = angle2, pivot = Offset(cx, cy)) {
            for (i in 0 until 8) {
                drawArc(
                    color = accent.copy(alpha = if (thinking || speaking) 0.95f else 0.55f),
                    startAngle = i * 45f + 8f,
                    sweepAngle = 26f,
                    useCenter = false,
                    style = Stroke(width = base * 0.03f, cap = StrokeCap.Round)
                )
            }
        }

        // ===== ripple saat mendengarkan =====
        if (listening) {
            drawCircle(
                color = AviCyan.copy(alpha = 0.45f * (1f - rms)),
                radius = base * (0.30f + 0.16f * rms),
                center = Offset(cx, cy),
                style = Stroke(width = base * 0.010f)
            )
        }

        // ===== inti =====
        val coreRadius = base * (0.150f + 0.012f * pulse)
        drawCircle(
            color = accent.copy(alpha = 0.85f),
            radius = coreRadius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFFFFF7E0).copy(alpha = 0.95f),
            radius = coreRadius * 0.45f,
            center = Offset(cx, cy)
        )

        // ===== titik orbit saat berpikir =====
        if (thinking) {
            for (i in 0 until 3) {
                val a = angle * PI.toFloat() / 180f + i * (2f * PI.toFloat() / 3f)
                val ox = cx + cos(a) * base * 0.30f
                val oy = cy + sin(a) * base * 0.30f
                drawCircle(
                    color = AviGoldSoft,
                    radius = base * 0.028f,
                    center = Offset(ox, oy)
                )
            }
        }

        // ===== bar equalizer saat berbicara =====
        if (speaking) {
            val barWidth = base * 0.030f
            val gap = barWidth * 0.9f
            val totalWidth = 5 * barWidth + 4 * gap
            var x = cx - totalWidth / 2f
            for (i in 0 until 5) {
                val h = base * (0.06f + 0.13f * abs(sin(wave * 2.2f + i * 1.1f)))
                drawRoundRect(
                    color = accentSoft,
                    topLeft = Offset(x + barWidth / 2f - 0f, cy - h / 2f),
                    size = androidx.compose.ui.geometry.Size(barWidth, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
                )
                x += barWidth + gap
            }
        }
    }
}
