package com.anztdree.avi.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.AviState
import com.anztdree.avi.ui.theme.AviPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Living core AVI — ikon aplikasi. Berlapis Canvas:
 * IDLE pulsa pelan · LISTENING waveform radial mic nyata · THINKING ring orbit + sapuan scan ·
 * SPEAKING glow mengikuti suara · LIVE semua ring + beam.
 * Visor digambar sebagai overlay composable (bukan di dalam Canvas).
 */
@Composable
fun ReactorOrb(
    state: AviState,
    micLevel: Float,
    voiceLevel: Float,
    palette: AviPalette,
    powerSave: Boolean,
    showVisor: Boolean,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null
) {
    val transition = rememberInfiniteTransition(label = "orb")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing)),
        label = "time"
    )
    val smoothMic by animateFloatAsState(
        targetValue = micLevel.coerceIn(0f, 1f),
        animationSpec = tween(110)
    )
    val smoothVoice by animateFloatAsState(
        targetValue = voiceLevel.coerceIn(0f, 1f),
        animationSpec = tween(110)
    )
    val action = onTap
    val rootModifier = if (action != null) modifier.clickable { action() } else modifier

    BoxWithConstraints(modifier = rootModifier, contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOrb(state, smoothMic, smoothVoice, palette, time, powerSave)
        }
        if (showVisor && !powerSave) {
            AviVisor(
                state = state,
                micLevel = smoothMic,
                voiceLevel = smoothVoice,
                palette = palette,
                modifier = Modifier.size(width = side * 0.5f, height = side * 0.17f)
            )
        }
    }
}

// ============================================================
// Diagnostik orbit ala JARVIS
// ============================================================

private data class OrbitItem(
    val align: Alignment,
    val prefix: String,
    val value: String,
    val phase: Float
)

/**
 * 4 label monospace (jam / baterai / jaringan / model) yang mengorbit perlahan
 * di sekitar area reaktor, dengan garis penghubung tipis.
 * enabled = false → tidak digambar sama sekali.
 */
@Composable
fun DiagnosticsOrbit(
    time: String,
    battery: String,
    network: String,
    model: String,
    palette: AviPalette,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!enabled) return
    val transition = rememberInfiniteTransition(label = "orbit")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "orbitTime"
    )
    val TAU = (2.0 * PI).toFloat()
    Box(modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = center
            val r = size.minDimension / 2f * 0.92f
            rotate(degrees = t * 360f, pivot = c) {
                drawArc(
                    color = palette.accent.copy(alpha = 0.30f),
                    startAngle = -90f,
                    sweepAngle = 70f,
                    useCenter = false,
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = palette.gold.copy(alpha = 0.28f),
                    startAngle = 90f,
                    sweepAngle = 50f,
                    useCenter = false,
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            // garis penghubung tipis ke 4 arah
            listOf(0f, 90f, 180f, 270f).forEach { a ->
                val rad = a * PI.toFloat() / 180f
                drawLine(
                    color = palette.accent.copy(alpha = 0.22f),
                    start = Offset(c.x + r * 0.50f * cos(rad), c.y + r * 0.50f * sin(rad)),
                    end = Offset(c.x + r * cos(rad), c.y + r * sin(rad)),
                    strokeWidth = 1f
                )
            }
        }
        val items = listOf(
            OrbitItem(Alignment.TopCenter, "JAM", time, 0f),
            OrbitItem(Alignment.CenterEnd, "BAT", battery, 1.6f),
            OrbitItem(Alignment.BottomCenter, "NET", network, 3.1f),
            OrbitItem(Alignment.CenterStart, "OTAK", model, 4.7f)
        )
        items.forEach { item ->
            val dx = (sin(t * TAU + item.phase) * 3f).dp
            val dy = (cos(t * TAU + item.phase) * 2f).dp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(item.align)
                    .offset(x = dx, y = dy)
                    .widthIn(max = 150.dp)
                    .padding(6.dp)
            ) {
                Text(
                    text = item.prefix,
                    color = palette.textDim,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = item.value,
                    color = palette.accent,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================
// Mesin gambar orb (DrawScope murni)
// ============================================================

private fun DrawScope.drawOrb(
    state: AviState,
    mic: Float,
    voice: Float,
    palette: AviPalette,
    time: Float,
    powerSave: Boolean
) {
    val TAU = (2.0 * PI).toFloat()
    val c = center
    val maxR = size.minDimension / 2f
    if (maxR <= 0f) return
    val ringR = maxR * 0.78f
    val pulse = 0.5f + 0.5f * sin(time * TAU)
    val active = state != AviState.IDLE

    fun pt(r: Float, angDeg: Float): Offset = Offset(
        c.x + r * cos(angDeg * PI.toFloat() / 180f),
        c.y + r * sin(angDeg * PI.toFloat() / 180f)
    )

    // ---- ring luar + tick halus ----
    drawCircle(
        color = palette.panelLight,
        radius = ringR,
        center = c,
        style = Stroke(width = 1.5.dp.toPx())
    )
    val ticks = if (powerSave) 24 else 48
    for (i in 0 until ticks) {
        val a = i * (360f / ticks) + time * 12f
        drawLine(
            color = palette.accent.copy(alpha = 0.28f),
            start = pt(ringR * 0.955f, a),
            end = pt(ringR * 0.995f, a),
            strokeWidth = 1.2.dp.toPx()
        )
    }

    // ---- efek per state ----
    when (state) {
        AviState.IDLE -> {
            drawCircle(
                color = palette.accent.copy(alpha = 0.20f + 0.14f * pulse),
                radius = ringR * 1.02f,
                center = c,
                style = Stroke(width = 2.5.dp.toPx())
            )
            drawArc(
                color = palette.gold.copy(alpha = 0.35f),
                startAngle = -60f,
                sweepAngle = 46f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = palette.accent.copy(alpha = 0.35f),
                startAngle = 120f,
                sweepAngle = 70f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        AviState.LISTENING, AviState.LIVE -> {
            val level = if (state == AviState.LIVE) maxOf(mic, voice) else mic
            // ripple keluar
            if (!powerSave) {
                for (k in 0..1) {
                    val frac = (time + k * 0.5f) % 1f
                    drawCircle(
                        color = palette.accent.copy(alpha = (1f - frac) * 0.35f * (0.25f + level)),
                        radius = ringR * (0.8f + 0.5f * frac),
                        center = c,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            // ring utama bereaksi
            drawCircle(
                color = palette.accent.copy(alpha = 0.45f + 0.35f * level),
                radius = ringR,
                center = c,
                style = Stroke(width = (2.5f + 2.5f * level).dp.toPx())
            )
            // waveform radial NYATA (64 segmen + noise sinus)
            val segments = 64
            for (i in 0 until segments) {
                val ang = i * (360f / segments)
                val noise = sin(time * TAU * 3f + i * 0.55f) * 0.5f + 0.5f
                val len = ringR * (0.05f + 0.26f * level * (0.35f + 0.65f * noise))
                drawLine(
                    color = palette.accent.copy(alpha = 0.85f),
                    start = pt(ringR * 0.96f, ang),
                    end = pt(ringR * 0.96f - len, ang),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                if (!powerSave) {
                    drawLine(
                        color = palette.gold.copy(alpha = 0.35f),
                        start = pt(ringR * 0.88f, ang + 180f),
                        end = pt(ringR * 0.88f - len * 0.6f, ang + 180f),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        AviState.THINKING -> {
            rotate(degrees = time * 360f, pivot = c) {
                drawArc(
                    color = palette.accent,
                    startAngle = -90f,
                    sweepAngle = 110f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            rotate(degrees = -time * 360f * 1.4f, pivot = c) {
                drawArc(
                    color = palette.gold,
                    startAngle = 30f,
                    sweepAngle = 70f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            if (!powerSave) {
                // sapuan scan 360°
                rotate(degrees = time * 720f, pivot = c) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                palette.accent.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = c
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        alpha = 0.8f,
                        style = Stroke(width = ringR * 0.22f)
                    )
                }
            }
            // titik orbit emas
            for (k in 0..2) {
                drawCircle(
                    color = palette.gold,
                    radius = 2.6.dp.toPx(),
                    center = pt(ringR * 0.84f, time * 360f + k * 120f)
                )
            }
        }
        AviState.SPEAKING -> {
            val glow = 0.25f + 0.75f * voice
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.accent.copy(alpha = 0.40f * glow), Color.Transparent),
                    center = c,
                    radius = maxR
                ),
                radius = maxR,
                center = c
            )
            val pr = ringR * (1f + 0.05f * voice * sin(time * TAU * 2f))
            drawCircle(
                color = palette.accent.copy(alpha = 0.55f + 0.4f * voice),
                radius = pr,
                center = c,
                style = Stroke(width = 3.5.dp.toPx())
            )
            drawCircle(
                color = palette.goldSoft.copy(alpha = 0.25f + 0.35f * voice),
                radius = ringR * 0.6f,
                center = c,
                style = Stroke(width = 1.8.dp.toPx())
            )
        }
    }

    // ---- beam mode LIVE ----
    if (state == AviState.LIVE) {
        val beams = 8
        for (k in 0 until beams) {
            rotate(degrees = time * 360f + k * (360f / beams), pivot = c) {
                drawLine(
                    color = palette.accent.copy(alpha = 0.28f + 0.3f * pulse),
                    start = pt(ringR * 0.55f, 0f),
                    end = pt(ringR * 1.02f, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        rotate(degrees = -time * 360f, pivot = c) {
            drawArc(
                color = palette.gold.copy(alpha = 0.5f),
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }

    // ---- partikel terbatas (mati di powerSave) ----
    if (!powerSave && active) {
        val count = 12
        for (k in 0 until count) {
            val speed = 0.5f + 0.35f * (k % 3)
            val ang = time * TAU * speed + k * (TAU / count)
            val r = ringR * (0.86f + 0.09f * sin(time * TAU + k))
            drawCircle(
                color = if (k % 4 == 0) palette.gold else palette.accentSoft,
                radius = 1.4.dp.toPx(),
                center = Offset(c.x + r * cos(ang), c.y + r * sin(ang)),
                alpha = 0.55f
            )
        }
    }

    // ---- inti reaktor: gradien radial berlapis ----
    val coreBoost = if (state == AviState.SPEAKING) 0.05f * voice else 0f
    val coreR = ringR * (0.52f + 0.02f * pulse + coreBoost)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.accent.copy(alpha = 0.55f),
                palette.coreGlow.copy(alpha = 0.18f),
                Color.Transparent
            ),
            center = c,
            radius = coreR * 1.6f
        ),
        radius = coreR * 1.6f,
        center = c
    )
    drawCircle(color = palette.accent.copy(alpha = 0.9f), radius = coreR * 0.55f, center = c)
    drawCircle(color = Color.White.copy(alpha = 0.75f), radius = coreR * 0.22f, center = c)
    drawCircle(
        color = palette.panelLight.copy(alpha = 0.9f),
        radius = ringR * 0.62f,
        center = c,
        style = Stroke(width = 1.dp.toPx())
    )
    // kilau tipis mengikuti rotasi (hemat: hanya bila bukan powerSave)
    if (!powerSave) {
        rotate(degrees = time * 360f, pivot = c) {
            drawArc(
                color = palette.coreGlow.copy(alpha = 0.22f),
                startAngle = -90f,
                sweepAngle = 40f,
                useCenter = false,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
