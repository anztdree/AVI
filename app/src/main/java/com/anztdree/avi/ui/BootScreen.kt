package com.anztdree.avi.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.ui.theme.AviPalette
import kotlin.math.PI
import kotlin.math.sin

/**
 * Boot sequence sinematik ±2,6 detik: IGNITE → SPIN-UP → SCAN → ONLINE.
 * Semua Canvas + timeline tunggal (Animatable) — tanpa bitmap. Tap kapan pun → onFinish.
 */
@Composable
fun BootScreen(palette: AviPalette, onFinish: () -> Unit) {
    var done by remember { mutableStateOf(false) }
    fun finish() {
        if (!done) {
            done = true
            onFinish()
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(2600, easing = LinearEasing))
        finish()
    }

    val t = progress.value
    val ms = t * 2600f
    val ignite = (ms / 800f).coerceIn(0f, 1f)
    val spin = ((ms - 800f) / 800f).coerceIn(0f, 1f)
    val scan = ((ms - 1600f) / 500f).coerceIn(0f, 1f)
    val online = ((ms - 2100f) / 500f).coerceIn(0f, 1f)
    val overallAlpha = if (ms > 2300f) (1f - (ms - 2300f) / 300f).coerceIn(0f, 1f) else 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .clickable { finish() },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().alpha(overallAlpha)) {

            // ---- garis sapuan scan (Fase 3) ----
            if (scan > 0f && scan < 1f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y = size.height * scan
                    drawLine(
                        color = palette.accent.copy(alpha = 0.55f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = palette.accent.copy(alpha = 0.12f),
                        start = Offset(0f, y - 14f),
                        end = Offset(size.width, y - 14f),
                        strokeWidth = 10f
                    )
                }
            }

            // ---- inti reaktor + teks ONLINE ----
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Canvas(modifier = Modifier.size(190.dp)) {
                    val TAU = (2.0 * PI).toFloat()
                    val c = center
                    val pulse = 0.5f + 0.5f * sin(t * TAU)

                    // FASE 1 — IGNITE: titik cahaya membesar jadi inti
                    val coreR = size.minDimension * 0.5f * (0.10f + 0.34f * ignite)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.coreGlow.copy(alpha = 0.9f * ignite),
                                palette.accent.copy(alpha = 0.35f * ignite),
                                Color.Transparent
                            ),
                            center = c,
                            radius = coreR * 2.4f
                        ),
                        radius = coreR * 2.4f,
                        center = c
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f * ignite),
                        radius = coreR * 0.35f,
                        center = c
                    )
                    // flash tipis di awal
                    drawCircle(
                        color = Color.White.copy(alpha = (1f - ignite) * 0.16f),
                        radius = size.minDimension * 0.5f,
                        center = c
                    )

                    // FASE 2 — SPIN-UP: ring muncul & makin cepat
                    if (spin > 0f) {
                        val angle = t * t * 1600f
                        rotate(degrees = angle, pivot = c) {
                            drawArc(
                                color = palette.accent.copy(alpha = 0.85f * spin),
                                startAngle = -90f,
                                sweepAngle = 120f,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        rotate(degrees = -angle * 0.7f, pivot = c) {
                            drawArc(
                                color = palette.gold.copy(alpha = 0.7f * spin),
                                startAngle = 60f,
                                sweepAngle = 80f,
                                useCenter = false,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        drawCircle(
                            color = palette.accent.copy(alpha = 0.45f * spin),
                            radius = size.minDimension * 0.44f,
                            center = c,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = palette.gold.copy(alpha = 0.25f + 0.2f * pulse),
                            radius = 2.5f.dp.toPx(),
                            center = Offset(
                                c.x + size.minDimension * 0.44f * sin(t * TAU * 2f),
                                c.y + size.minDimension * 0.44f * sin(t * TAU * 3f)
                            )
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "AVI ONLINE",
                    color = palette.accent,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(online)
                )
                Text(
                    "SISTEM MARKAS — HUD MARK II",
                    color = palette.textDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(online)
                )
            }

            // ---- boot log (Fase 3) ----
            val lines = listOf("OTAK AI… OK", "SISTEM SUARA… OK", "HUD MARK… OK")
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                lines.forEachIndexed { i, line ->
                    val threshold = 0.05f + i * 0.3f
                    Text(
                        line,
                        color = palette.accent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.alpha(if (scan >= threshold) 1f else 0f)
                    )
                }
            }

            // ---- hint lewati ----
            Text(
                "KETUK UNTUK LEWATI",
                color = palette.textDim,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .alpha(0.7f)
            )
        }
    }
}
