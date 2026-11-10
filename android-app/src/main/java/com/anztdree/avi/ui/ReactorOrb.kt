package com.anztdree.avi.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.AviState
import com.anztdree.avi.ui.theme.AviPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * ORB MK-XIX v41 — porting SETIA dari preview Pages yang disetujui mata Pak
 * (public/avi-agent.html · .orbBig). Satu Canvas berlapis:
 *   · AURA          — pendaran sian+emas bernapas di belakang semuanya
 *   · HUD           — k1 (3 kurung tebal ujung putih-panas, laju variabel),
 *                     k2 (busur lawan + aksen emas), htick (detak steps),
 *                     hdata (pita titik + ekor emas), hsweep (radar
 *                     tumbuh-menyusut), 3 node cahaya mengorbit
 *   · LENSA REAKTOR — cakram teal GELAP solid + bingkai 3 busur tebal (lseg)
 *                     + busur emas lawan (lseg2) + ring tik steps (ltick)
 *                     + wajah: coreGlow (kolam plasma bernapas) +
 *                     HEKSAGRAM GANDA (sian atas · emas bawah, putar 16dtk)
 *                     + garis pindai linier (lscan) + segitiga kaca (ltri)
 *                     + 6 baut hex berkedip bergiliran + 2 rel sisi (lbkt)
 *   · DENYUT        — cincin mengembang tiap detak (pulse), 2 elips miring
 *                     berekor komet (carc a/b), percikan pengorbit (spark)
 *   · STATE DENGAR  — seluruh wajah MENYALA HIJAU ZAMRUD, orb MEMBESAR 16%,
 *                     sonar 1 cincin melebar (ear), halo zamrud (hearGlow)
 *   · STATE KERJA   — semua laju ×~3, cincin progres + jendela instrumen
 * Di luar orb: ReactorHalo (pita telemetri JAM·BAT·RAM·NET·OTAK) — terpisah.
 * Visor digambar sebagai overlay composable (bukan di dalam Canvas).
 */

// ---- warna identitas MK-XIX (dari preview yang disetujui — tetap walaupun tema ganti) ----
private val C_MINT = Color(0xFF5EEAD4)
private val C_HOT = Color(0xFFEAFBFF)      // ujung putih-panas kurung k1
private val C_LIGHT = Color(0xFFAAF6FF)    // sian terang busur
private val C_LINE = Color(0xFF96F0FF)     // bingkai lensa
private val C_EMERALD = Color(0xFF34D399)  // identitas "MIC HIDUP"
private val C_EM_SOFT = Color(0xFFA7F3D0)
private val C_EM_DEEP = Color(0xFF10B981)
private val C_GOLD_TAIL = Color(0xFFF5B54A)
private val C_GOLD_HI = Color(0xFFFFECBE)

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
    // satu sumber waktu detik-nyata — semua lapisan punya durasi sendiri,
    // persis seperti animation-duration CSS di preview
    val time = remember { mutableStateOf(0.62f) }
    if (!powerSave) {
        LaunchedEffect(Unit) {
            var last = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                time.value += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }
    val smoothMic by animateFloatAsState(
        targetValue = micLevel.coerceIn(0f, 1f),
        animationSpec = tween(110)
    )
    val smoothVoice by animateFloatAsState(
        targetValue = voiceLevel.coerceIn(0f, 1f),
        animationSpec = tween(110)
    )
    // DENGAR = orb MEMBESAR 16% + seluruh wajah menyala zamrud (mix halus)
    val hearMix by animateFloatAsState(
        targetValue = if (state == AviState.LISTENING) 1f else 0f,
        animationSpec = tween(450)
    )
    val workMix by animateFloatAsState(
        targetValue = if (state == AviState.THINKING || state == AviState.SPEAKING || state == AviState.LIVE) 1f else 0f,
        animationSpec = tween(550)
    )
    val action = onTap
    val rootModifier = if (action != null) modifier.clickable { action() } else modifier

    BoxWithConstraints(modifier = rootModifier, contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val s = 1f + 0.16f * hearMix
                    scaleX = s
                    scaleY = s
                }
        ) {
            drawMk19(
                t = time.value,
                mic = smoothMic,
                voice = smoothVoice,
                hear = hearMix,
                work = workMix,
                state = state,
                palette = palette
            )
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
// Halo Mark III — telemetri JAM·BAT·RAM·NET·OTAK mengorbit
// (pita 80–105dp) — identik dengan .otelem preview, tak berubah
// ============================================================

@Composable
fun ReactorHalo(
    palette: AviPalette,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    glowLevel: Float = 0f,
    orbitItems: List<String> = emptyList()
) {
    if (!enabled) return
    val transition = rememberInfiniteTransition(label = "halo")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "haloTime"
    )

    // Cache Paint — TIDAK dialokasi per frame (TextPaint ada di paket android.text)
    val orbitPaint = remember {
        android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(modifier = modifier) {
        val c = center
        val R = size.minDimension / 2f          // penuh kotak (±105dp+)
        val dR = R / 105f                       // skala dp→px aktual

        // ============ PITA 3 — busur & tick (99–105dp) ============
        val bandR = 97f * dR
        // busur utama berputar searah jarum jam
        rotate(degrees = t * 360f, pivot = c) {
            drawArc(
                color = palette.accent.copy(alpha = 0.32f),
                startAngle = -90f,
                sweepAngle = 64f,
                useCenter = false,
                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = palette.gold.copy(alpha = 0.30f),
                startAngle = 90f,
                sweepAngle = 44f,
                useCenter = false,
                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        // busur kedua berlawanan arah — lebih tipis
        rotate(degrees = -t * 216f, pivot = c) {
            drawArc(
                color = palette.accent.copy(alpha = 0.18f),
                startAngle = 20f,
                sweepAngle = 26f,
                useCenter = false,
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        // tick pendek ke 4 arah — hanya di pita luar, tidak masuk ke area orb/teks
        for (k in 0 until 4) {
            val rad = k * 90f * PI.toFloat() / 180f
            drawLine(
                color = palette.accent.copy(alpha = 0.20f),
                start = Offset(c.x + bandR * 0.97f * cos(rad), c.y + bandR * 0.97f * sin(rad)),
                end = Offset(c.x + R * 0.995f * cos(rad), c.y + R * 0.995f * sin(rad)),
                strokeWidth = 1f
            )
        }
        // dua titik emas berlawanan arah di pita yang sama
        for (k in 0..1) {
            val rad = (-t * 360f + k * 180f) * PI.toFloat() / 180f
            drawCircle(
                color = palette.gold,
                radius = 1.8.dp.toPx(),
                center = Offset(c.x + bandR * cos(rad), c.y + bandR * sin(rad)),
                alpha = 0.75f
            )
        }

        // ============ PITA 2 — diagnostik mengorbit (80–99dp) ============
        val items = orbitItems
        if (items.isNotEmpty()) {
            val labelR = 89f * dR                    // garis dasar label
            val glow = 0.50f + 0.22f * glowLevel + 0.10f * sin(t * 2f * PI.toFloat())
            orbitPaint.textSize = 8.5.sp.toPx()
            // kecepatan berombak organik (superposisi dua gelombang sinus)
            val wob = 16f * sin(2f * PI.toFloat() * t) +
                10f * sin(2f * PI.toFloat() * t * 3f + 1.7f)
            val n = items.size
            for (i in 0 until n) {
                val ang = t * 360f + wob + i * (360f / n)
                orbitPaint.color = if (i % 2 == 0) palette.accent.toArgb() else palette.gold.toArgb()
                orbitPaint.alpha = (255 * glow.coerceIn(0f, 1f)).toInt()
                drawIntoCanvas { canvas ->
                    val nc = canvas.nativeCanvas
                    nc.save()
                    nc.rotate(ang, c.x, c.y)
                    nc.drawText(items[i], c.x, c.y - labelR, orbitPaint)
                    nc.restore()
                }
                // titik emas pemisah antar data — ikut berputar
                val sep = (ang + 180f / n) * PI.toFloat() / 180f
                drawCircle(
                    color = palette.gold,
                    radius = 1.4.dp.toPx(),
                    center = Offset(c.x + labelR * cos(sep), c.y + labelR * sin(sep)),
                    alpha = 0.65f
                )
            }
        }
    }
}

// ============================================================
// Mesin gambar MK-XIX v41 (DrawScope murni)
// Semua radius = fraksi R (R = jari-jari orb). Geometri diterjemahkan
// dari CSS preview (orb 112px → R 56px) supaya proporsi PERSIS sama.
// ============================================================

private fun DrawScope.drawMk19(
    t: Float,
    mic: Float,
    voice: Float,
    hear: Float,
    work: Float,
    state: AviState,
    palette: AviPalette
) {
    val c = center
    val R = size.minDimension / 2f
    if (R <= 0f) return
    val TAU = (2.0 * PI).toFloat()

    // ---- waktu & warna helper ----
    // kerja = semua laju melonjak (preview: durasi ×0.27–0.4)
    fun dur(d: Float) = d * (1f - 0.70f * work)
    fun cyc(d: Float, off: Float = 0f): Float {
        val f = (t / dur(d) + off) % 1f
        return if (f < 0f) f + 1f else f
    }
    fun rot(d: Float, off: Float = 0f, reverse: Boolean = false): Float {
        val f = cyc(d, off)
        return (if (reverse) -f else f) * 360f
    }
    fun breath(d: Float, off: Float = 0f) = 0.5f + 0.5f * sin(TAU * (t / dur(d) + off))
    // warna: saat dengar, peran sian menyala ZAMRUD (identitas mic hidup)
    fun em(base: Color, emerald: Color = C_EMERALD) = lerp(base, emerald, hear)

    fun pt(r: Float, angDeg: Float) = Offset(
        c.x + r * cos(angDeg * PI.toFloat() / 180f),
        c.y + r * sin(angDeg * PI.toFloat() / 180f)
    )

    // busur dengan pendaran (2 lapis) — emulasi drop-shadow CSS
    fun arcSeg(r: Float, start: Float, sweep: Float, color: Color, th: Float, glow: Boolean = true) {
        if (glow) {
            drawArc(
                color = color.copy(alpha = color.alpha * 0.22f),
                startAngle = start, sweepAngle = sweep, useCenter = false,
                style = Stroke(width = th * 2.6f, cap = StrokeCap.Round)
            )
        }
        drawArc(
            color = color,
            startAngle = start, sweepAngle = sweep, useCenter = false,
            style = Stroke(width = th, cap = StrokeCap.Round)
        )
    }

    // ============================================================
    // 1) HEAR GLOW — halo zamrud paling belakang (dramatis, bernapas)
    // ============================================================
    if (hear > 0.01f) {
        val b = 1f + 0.15f * sin(TAU * t / 3.4f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    C_EMERALD.copy(alpha = 0.55f * hear),
                    C_EMERALD.copy(alpha = 0.22f * hear),
                    Color.Transparent
                ),
                center = c, radius = R * 1.36f * b
            ),
            radius = R * 1.36f * b, center = c
        )
    }

    // ============================================================
    // 2) AURA — pendaran panggung bernapas (7dtk)
    // ============================================================
    val auraS = 0.965f + 0.07f * breath(7f)
    scale(auraS, auraS, c) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    palette.accent.copy(alpha = 0.17f),
                    palette.accent.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(c.x, c.y - R * 0.03f), radius = R * 1.5f
            ),
            radius = R * 1.5f, center = Offset(c.x, c.y - R * 0.03f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(palette.gold.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(c.x, c.y + R * 0.05f), radius = R * 1.17f
            ),
            radius = R * 1.17f, center = Offset(c.x, c.y + R * 0.05f)
        )
    }

    // ============================================================
    // 3) LAPIS HUD (sway 15dtk: goyang halus ala preview)
    // ============================================================
    val sway = breath(15f)
    rotate(degrees = -2.2f + 4.4f * sway, pivot = c) {
        scale(1f, 1f + 0.012f * sway, c) {
            // ---- k1: TIGA kurung TEBAL ujung putih-panas, laju VARIABEL 26dtk ----
            val k1r = R * 0.978f
            val k1th = R * 0.046f
            // putaran melaju lalu menahan (hArcA: 0→128@38% →168@56% →360@100%)
            val p1 = cyc(26f)
            val k1deg = when {
                p1 < 0.38f -> 128f * (p1 / 0.38f)
                p1 < 0.56f -> 128f + 40f * ((p1 - 0.38f) / 0.18f)
                else -> 168f + 192f * ((p1 - 0.56f) / 0.44f)
            }
            rotate(degrees = k1deg, pivot = c) {
                val headCol = em(C_HOT, C_EM_SOFT)
                val tailCol = em(palette.accent, C_EMERALD)
                val midCol = em(C_LIGHT, C_EM_SOFT)
                arcSeg(k1r, 8f, 74f, tailCol.copy(alpha = 0.72f), k1th)
                arcSeg(k1r, 8f, 30f, headCol.copy(alpha = 0.95f), k1th)
                arcSeg(k1r, 128f, 38f, midCol.copy(alpha = 0.85f), k1th)
                arcSeg(k1r, 232f, 48f, em(C_MINT, C_EMERALD).copy(alpha = 0.70f), k1th)
            }
            // ---- k2: busur tipis ARAH LAWAN + satu aksen emas (19dtk) ----
            val k2r = R * 0.893f
            val k2th = R * 0.034f
            rotate(degrees = rot(19f, reverse = true), pivot = c) {
                arcSeg(k2r, 196f, 44f, em(C_LIGHT, C_EM_SOFT).copy(alpha = 0.90f), k2th)
                arcSeg(k2r, 334f, 28f, palette.gold.copy(alpha = 0.85f), k2th)
                arcSeg(k2r, 126f, 24f, em(C_LIGHT, C_EM_SOFT).copy(alpha = 0.50f), k2th)
            }
            // ---- htick: band detak KALIBRASI — steps 72 × 36dtk (eskapan mesin) ----
            val htr = R * 0.732f
            val hStep = kotlin.math.floor(cyc(36f) * 72f) / 72f * 360f
            rotate(degrees = hStep, pivot = c) {
                for (k in 0 until 72) {
                    drawArc(
                        color = em(Color(0xFF96F0FF), C_EM_SOFT).copy(alpha = 0.55f),
                        startAngle = k * 5f, sweepAngle = 1.1f, useCenter = false,
                        style = Stroke(width = R * 0.039f)
                    )
                }
            }
            // ---- hdata: pita titik DATA (13dtk) + ekor emas lawan (11dtk) ----
            val hdr = R * 0.625f
            rotate(degrees = rot(13f, off = 30f / 360f), pivot = c) {
                for (k in 0 until 50) {
                    drawArc(
                        color = em(Color(0xFFBEF7FF), C_EM_SOFT).copy(alpha = 0.80f),
                        startAngle = k * 7.2f, sweepAngle = 2.4f, useCenter = false,
                        style = Stroke(width = R * 0.032f)
                    )
                }
            }
            rotate(degrees = rot(11f, off = 250f / 360f, reverse = true), pivot = c) {
                arcSeg(R * 0.671f, 316f, 28f, palette.gold.copy(alpha = 0.80f), R * 0.025f)
            }
            // ---- hsweep: sapuan radar TUMBUH-MENYUSUT non-linier (8.6dtk) ----
            val ps = cyc(8.6f)
            val sweepDeg = when {
                ps < 0.42f -> 214f * (ps / 0.42f)
                ps < 0.57f -> 214f + 38f * ((ps - 0.42f) / 0.15f)
                ps < 0.80f -> 252f + 140f * ((ps - 0.57f) / 0.23f)
                else -> 392f + 128f * ((ps - 0.80f) / 0.20f)
            }
            val sweepScale = when {
                ps < 0.42f -> 1f + 0.02f * (ps / 0.42f)
                ps < 0.57f -> 1.02f - 0.055f * ((ps - 0.42f) / 0.15f)
                ps < 0.80f -> 0.965f + 0.045f * ((ps - 0.57f) / 0.23f)
                else -> 1.01f - 0.01f * ((ps - 0.80f) / 0.20f)
            }
            val sweepBrush = Brush.sweepGradient(
                0f to Color.Transparent,
                0.044f to em(palette.accent, C_EMERALD).copy(alpha = 0.26f),
                0.139f to em(C_LIGHT, C_EM_SOFT).copy(alpha = 0.80f),
                0.189f to Color.Transparent,
                1f to Color.Transparent,
                center = c
            )
            rotate(degrees = sweepDeg, pivot = c) {
                scale(sweepScale, sweepScale, c) {
                    drawArc(
                        brush = sweepBrush, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        style = Stroke(width = R * 0.030f)
                    )
                }
            }
            // ---- 3 NODE cahaya mengorbit (menunggangi lajur, kedip berfase) ----
            fun node(rFrac: Float, angDeg: Float, col: Color, dotR: Float, blinkPhase: Float) {
                val blink = 0.45f + 0.55f * (0.5f + 0.5f * sin(TAU * (t / dur(3.7f) - blinkPhase)))
                val pos = pt(R * rFrac, angDeg)
                drawCircle(color = col.copy(alpha = 0.32f * blink), radius = dotR * 2.4f, center = pos)
                drawCircle(color = col.copy(alpha = 0.55f * blink), radius = dotR * 1.6f, center = pos)
                drawCircle(color = Color.White.copy(alpha = 0.95f * blink), radius = dotR, center = pos)
            }
            val w1deg = k1deg // w1 mengunci laju k1 — kepala komet
            val w2deg = rot(14.5f, reverse = true)
            val w3deg = rot(9.5f, reverse = true)
            node(0.946f, w1deg + 8f, C_HOT, R * 0.036f, 0f)
            node(0.768f, w2deg, palette.gold, R * 0.027f, 1.2f / 3.7f)
            node(0.607f, w3deg, Color(0xFFCFF6FF), R * 0.027f, 2.1f / 3.7f)
        }
    }

    // ============================================================
    // 4) CORE BOX — mhalo · pulse · carc · spark
    // ============================================================
    // mhalo — halo energi di belakang lensa, bernapas 4.4dtk
    val mhBreath = breath(4.4f)
    val mhS = 0.93f + 0.12f * mhBreath
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                em(palette.accent, C_EMERALD).copy(alpha = (0.16f + if (state == AviState.SPEAKING) 0.10f * voice else 0f)),
                em(palette.accent, C_EMERALD).copy(alpha = 0.055f),
                Color.Transparent
            ),
            center = c, radius = R * 0.803f * mhS
        ),
        radius = R * 0.803f * mhS, center = c
    )
    // pulse — cincin cahaya mengembang tiap detak (4.4dtk · kerja 1.05dtk)
    val pp = cyc(if (work > 0.5f) 1.05f else 4.4f)
    if (pp < 0.55f) {
        val pr = R * 0.714f * (0.4f + 1.1f * pp)
        drawCircle(
            color = em(Color(0xFFA0F0FF), C_EM_SOFT).copy(alpha = 0.65f * (1f - pp / 0.55f)),
            radius = pr, center = c,
            style = Stroke(width = R * 0.018f)
        )
    }
    // carc — 2 elips miring berekor komet
    val carcR = R * 0.554f
    val caCol = em(palette.accent, C_EMERALD)
    rotate(degrees = 0f, pivot = c) {
        scale(1f, 0.42f, c) {
            drawCircle(color = caCol.copy(alpha = 0.13f), radius = carcR, center = c, style = Stroke(R * 0.025f))
            rotate(degrees = rot(3.2f), pivot = c) {
                arcSeg(carcR, 0f, 110f, Color(0xFFC8F7FF).copy(alpha = 0.95f), R * 0.025f, glow = false)
                arcSeg(carcR, 90f, 60f, caCol.copy(alpha = 0.50f), R * 0.025f, glow = false)
            }
        }
    }
    rotate(degrees = 24f, pivot = c) {
        scale(0.9f, 0.64f, c) {
            drawCircle(color = palette.gold.copy(alpha = 0.10f), radius = carcR * 0.9f, center = c, style = Stroke(R * 0.022f))
            rotate(degrees = rot(4.6f, reverse = true), pivot = c) {
                arcSeg(carcR * 0.9f, 0f, 100f, Color(0xFFFFDE96).copy(alpha = 0.92f), R * 0.022f, glow = false)
                arcSeg(carcR * 0.9f, 80f, 55f, palette.gold.copy(alpha = 0.40f), R * 0.022f, glow = false)
            }
        }
    }
    // spark — percikan mengitari inti (elips 0.4)
    rotate(degrees = -10f, pivot = c) {
        scale(1f, 0.40f, c) {
            val spA = rot(if (work > 0.5f) 2f else 7.2f)
            val sp = pt(R * 0.679f, spA)
            drawCircle(color = em(palette.accent, C_EMERALD).copy(alpha = 0.30f), radius = R * 0.05f, center = sp)
            drawCircle(color = Color(0xFFF2FFFF).copy(alpha = 0.95f), radius = R * 0.022f, center = sp)
        }
    }

    // ============================================================
    // 5) LENSA REAKTOR — identitas konsep 2
    // ============================================================
    val lensR = R * 0.571f
    // bayangan/glow luar lensa — cincin pendaran mengelilingi tepi cakram
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.Transparent,
            0.60f to Color.Transparent,
            0.72f to em(palette.accent, C_EMERALD).copy(alpha = 0.28f * (0.8f + 0.2f * mhBreath)),
            0.80f to em(palette.accent, C_EMERALD).copy(alpha = 0.10f),
            1f to Color.Transparent,
            center = c, radius = lensR * 1.55f
        ),
        radius = lensR * 1.55f, center = c
    )
    // tubuh lensa: cakram teal GELAP solid (offset 38% 30%)
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color(0xFF0F4152),
            0.36f to Color(0xFF092C3E),
            0.66f to Color(0xFF041A28),
            1f to Color(0xFF010A12),
            center = Offset(c.x - R * 0.137f, c.y - R * 0.228f),
            radius = lensR * 1.24f
        ),
        radius = lensR, center = c
    )
    // saat DENGAR: cakram MENYALA HIJAU — overlay zamrud (brightness 2.5 CSS)
    if (hear > 0.01f) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    C_EM_SOFT.copy(alpha = 0.42f * hear),
                    C_EMERALD.copy(alpha = 0.30f * hear),
                    Color(0xFF0A3A2A).copy(alpha = 0.30f * hear)
                ),
                center = Offset(c.x - R * 0.137f, c.y - R * 0.228f),
                radius = lensR * 1.2f
            ),
            radius = lensR, center = c
        )
    }
    // kilau tepi atas (conic dari 205°: terang di 56%)
    rotate(degrees = 205f + 42f, pivot = c) {
        drawArc(
            color = Color(0xFFBEF3FF).copy(alpha = 0.50f),
            startAngle = 0f, sweepAngle = 18f, useCenter = false,
            style = Stroke(width = R * 0.018f, cap = StrokeCap.Round)
        )
    }
    // bingkai lensa
    drawCircle(
        color = em(C_LINE, C_EM_SOFT).copy(alpha = 0.42f),
        radius = lensR, center = c,
        style = Stroke(width = R * 0.018f)
    )
    // inner top light & bottom dark (inset shadow)
    drawArc(
        color = Color(0xFFAAEBFF).copy(alpha = 0.20f),
        startAngle = 200f, sweepAngle = 140f, useCenter = false,
        style = Stroke(width = R * 0.05f)
    )
    drawArc(
        color = Color.Black.copy(alpha = 0.55f),
        startAngle = 20f, sweepAngle = 140f, useCenter = false,
        style = Stroke(width = R * 0.06f)
    )

    // ltick — ring tik kalibrasi dalam bingkai: steps 60 × 60dtk
    val ltStep = kotlin.math.floor(cyc(60f) * 60f) / 60f * 360f
    rotate(degrees = ltStep, pivot = c) {
        for (k in 0 until 40) {
            drawArc(
                color = em(Color(0xFF8CEEFF), C_EM_SOFT).copy(alpha = 0.60f),
                startAngle = k * 9f, sweepAngle = 1.2f, useCenter = false,
                style = Stroke(width = R * 0.043f)
            )
        }
    }
    // lseg — bingkai 3 BUSUR TEBAL berputar 17dtk (kerja 4.6)
    val lsegR = R * 0.625f
    val lsegTh = R * 0.082f
    rotate(degrees = rot(17f, off = -58f / 360f), pivot = c) {
        arcSeg(lsegR, 0f, 54f, em(C_LIGHT, C_EM_SOFT).copy(alpha = 0.98f), lsegTh)
        arcSeg(lsegR, 118f, 52f, em(palette.accent, C_EMERALD).copy(alpha = 0.82f), lsegTh)
        arcSeg(lsegR, 236f, 52f, em(C_MINT, C_EMERALD).copy(alpha = 0.70f), lsegTh)
    }
    // lseg2 — busur EMAS lawan, lebih tipis (23dtk reverse · kerja 6.2)
    rotate(degrees = rot(23f, off = 140f / 360f, reverse = true), pivot = c) {
        arcSeg(R * 0.589f, 38f, 20f, palette.gold.copy(alpha = 0.85f), R * 0.039f)
        arcSeg(R * 0.589f, 212f, 12f, palette.gold.copy(alpha = 0.45f), R * 0.039f)
    }
    // lring — ring dalam bernapas
    val lrA = 0.40f + 0.18f * breath(4.4f)
    drawCircle(
        color = em(Color(0xFF82EEFF), C_EM_SOFT).copy(alpha = lrA),
        radius = R * 0.393f, center = c,
        style = Stroke(width = R * 0.018f)
    )

    // ---- WAJAH lensa (clip bulat): coreGlow · hexp · lscan ----
    val faceR = R * 0.482f
    val facePath = Path().apply {
        addOval(Rect(left = c.x - faceR, top = c.y - faceR, right = c.x + faceR, bottom = c.y + faceR))
    }
    clipPath(facePath) {
        // coreGlow — kolam plasma bernapas 3.6dtk; MELMEBAR saat dengar / mengikuti suara
        val cgBase = R * 0.321f
        val cgGrow = 0.06f * breath(3.6f) +
            0.28f * hear +                                    // 36→52px saat dengar
            (if (state == AviState.SPEAKING) 0.22f * voice else 0f) +
            0.10f * mic
        val cgR = cgBase * (1f + cgGrow)
        val cgCol = lerp(
            em(palette.accent, C_EMERALD),
            Color(0xFFD1FAE5),
            hear * 0.75f
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    cgCol.copy(alpha = 0.50f + 0.42f * hear),
                    cgCol.copy(alpha = 0.24f + 0.24f * hear),
                    Color.Transparent
                ),
                center = c, radius = cgR
            ),
            radius = cgR, center = c
        )
        // hexp — HEKSAGRAM GANDA: segitiga sian ke atas + emas ke bawah, putar 16dtk
        val hxR = R * 0.375f
        val hxA = cyc(16f) * 360f
        // warna dilerp dulu (lerp hanya untuk Color), brush dibangun dari hasilnya
        val upTop = lerp(em(Color(0xFF96F0FF), C_EM_SOFT), C_EM_SOFT, hear).copy(alpha = 0.70f + 0.25f * hear)
        val upBot = lerp(em(palette.accent, C_EMERALD), C_EM_DEEP, hear).copy(alpha = 0.15f + 0.20f * hear)
        val dnTop = lerp(palette.gold, Color(0xFFFDE68A), hear).copy(alpha = 0.60f + 0.32f * hear)
        val dnBot = palette.gold.copy(alpha = 0.10f + 0.20f * hear)
        val hxUp = Brush.verticalGradient(
            listOf(upTop, upBot), startY = c.y - hxR, endY = c.y + hxR
        )
        val hxDown = Brush.verticalGradient(
            listOf(dnTop, dnBot), startY = c.y - hxR, endY = c.y + hxR
        )
        fun triPath(up: Boolean): Path {
            val p = Path()
            fun v(x: Float, y: Float) = Offset(c.x + x * cos(hxA * PI.toFloat() / 180f) - y * sin(hxA * PI.toFloat() / 180f),
                c.y + x * sin(hxA * PI.toFloat() / 180f) + y * cos(hxA * PI.toFloat() / 180f))
            val a = if (up) v(0f, -hxR) else v(0f, hxR)
            val b2 = if (up) v(0.72f * hxR, hxR) else v(0.72f * hxR, -hxR)
            val d2 = if (up) v(-0.72f * hxR, hxR) else v(-0.72f * hxR, -hxR)
            p.moveTo(a.x, a.y); p.lineTo(b2.x, b2.y); p.lineTo(d2.x, d2.y); p.close()
            return p
        }
        drawPath(triPath(true), hxUp, alpha = 0.55f + 0.45f * hear)
        drawPath(triPath(false), hxDown, alpha = 0.55f + 0.45f * hear)
        // lscan — garis pindai LINIER melintasi wajah (6.4dtk · kerja 2.5)
        val pc = cyc(if (work > 0.5f) 2.5f else 6.4f)
        if (pc > 0.64f) {
            val q = (pc - 0.64f) / 0.36f
            val scanY = c.y - faceR + (2f * faceR) * q
            val scanA = when {
                q < 0.22f -> 0.95f * (q / 0.22f)
                q > 0.85f -> 0.85f * (1f - (q - 0.85f) / 0.15f)
                else -> 0.90f
            }
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFFA0F4FF).copy(alpha = 0.30f * scanA),
                        Color.White.copy(alpha = 0.55f * scanA),
                        Color(0xFFA0F4FF).copy(alpha = 0.30f * scanA),
                        Color.Transparent
                    ),
                    startY = scanY - R * 0.08f, endY = scanY + R * 0.08f
                ),
                topLeft = Offset(c.x - faceR, scanY - R * 0.08f),
                size = Size(faceR * 2f, R * 0.16f)
            )
        }
    }

    // ltri — reticle segitiga KACA (identitas konsep 2)
    val triW = R * 0.375f
    val triH = R * 0.34f
    val triPath = Path().apply {
        moveTo(c.x, c.y - triH / 2f)
        lineTo(c.x + triW / 2f, c.y + triH / 2f)
        lineTo(c.x - triW / 2f, c.y + triH / 2f)
        close()
    }
    drawPath(
        triPath,
        Brush.verticalGradient(
            listOf(
                em(Color(0xFFE8FDFF), C_EM_SOFT).copy(alpha = 0.95f),
                em(Color(0xFF7DE9F9), C_EMERALD).copy(alpha = 0.70f),
                em(Color(0xFF18A9CE), C_EMERALD).copy(alpha = 0.85f)
            ),
            startY = c.y - triH / 2f, endY = c.y + triH / 2f
        )
    )
    // segitiga dalam gelap (inset) — wajah kaca berongga
    val innerTri = Path().apply {
        moveTo(c.x, c.y - triH / 2f + R * 0.025f)
        lineTo(c.x + triW / 2f - R * 0.025f, c.y + triH / 2f - R * 0.010f)
        lineTo(c.x - triW / 2f + R * 0.025f, c.y + triH / 2f - R * 0.010f)
        close()
    }
    drawPath(innerTri, Color(0xFF04141F).copy(alpha = 0.60f))

    // lbol — 6 BAUT HEX di bezel, berkedip bergiliran (4.8dtk)
    val boltAngles = floatArrayOf(-65f, -16f, 57f, 136f, 180f, -130f)
    val boltRads = floatArrayOf(0.59f, 0.52f, 0.49f, 0.54f, 0.59f, 0.61f)
    for (i in boltAngles.indices) {
        val phase = cyc(4.8f, -i * 0.8f / 4.8f)
        val bright = when {
            phase < 0.12f -> 1.9f
            phase < 0.26f -> 1.4f
            else -> 1f
        }
        val alpha = if (phase < 0.26f) 1f else 0.5f
        val goldBolt = i == 3
        val base = if (goldBolt) Color(0xFFF5B54A) else Color(0xFF4FCDE6)
        val hi = if (goldBolt) Color(0xFFFFF3D2) else Color(0xFFD9F9FF)
        val col = lerp(hi, base, 0.35f)
        val pos = pt(R * boltRads[i], boltAngles[i])
        val bw = R * 0.045f
        val bh = R * 0.05f
        val hex = Path().apply {
            moveTo(pos.x, pos.y - bh)
            lineTo(pos.x + bw, pos.y - bh / 2f)
            lineTo(pos.x + bw, pos.y + bh / 2f)
            lineTo(pos.x, pos.y + bh)
            lineTo(pos.x - bw, pos.y + bh / 2f)
            lineTo(pos.x - bw, pos.y - bh / 2f)
            close()
        }
        drawPath(hex, col.copy(alpha = alpha))
        if (bright > 1.2f) {
            drawCircle(
                color = col.copy(alpha = 0.35f * (bright - 1f)),
                radius = bw * 2.6f, center = pos
            )
        }
    }
    // lbkt — 2 rel HUD di sisi lensa, hanyut naik-turun berlawanan (7dtk)
    val bkA = sin(TAU * t / dur(7f))
    val bkCol = em(Color(0xFF8CEEFF), C_EM_SOFT).copy(alpha = 0.50f)
    val bkW = R * 0.125f
    val bkH = R * 0.30f
    val bkOff = R * 0.089f * bkA
    // kiri
    drawLine(bkCol, Offset(c.x - R * 0.714f, c.y - bkH / 2f + bkOff), Offset(c.x - R * 0.714f, c.y + bkH / 2f + bkOff), R * 0.02f)
    drawLine(bkCol, Offset(c.x - R * 0.714f, c.y - bkH / 2f + bkOff), Offset(c.x - R * 0.714f + bkW, c.y - bkH / 2f + bkOff), R * 0.02f)
    drawLine(bkCol, Offset(c.x - R * 0.714f, c.y + bkH / 2f + bkOff), Offset(c.x - R * 0.714f + bkW, c.y + bkH / 2f + bkOff), R * 0.02f)
    // kanan (lawan arah)
    val bkOff2 = -bkOff
    drawLine(bkCol, Offset(c.x + R * 0.714f, c.y - bkH / 2f + bkOff2), Offset(c.x + R * 0.714f, c.y + bkH / 2f + bkOff2), R * 0.02f)
    drawLine(bkCol, Offset(c.x + R * 0.714f, c.y - bkH / 2f + bkOff2), Offset(c.x + R * 0.714f - bkW, c.y - bkH / 2f + bkOff2), R * 0.02f)
    drawLine(bkCol, Offset(c.x + R * 0.714f, c.y + bkH / 2f + bkOff2), Offset(c.x + R * 0.714f - bkW, c.y + bkH / 2f + bkOff2), R * 0.02f)

    // ============================================================
    // 6) LAPIS KERJA — jendela instrumen + cincin progres
    // ============================================================
    if (work > 0.05f) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFF020C14).copy(alpha = 0.74f * work),
                    Color(0xFF020C14).copy(alpha = 0.42f * work),
                    Color.Transparent
                ),
                center = c, radius = R * 0.41f
            ),
            radius = R * 0.41f, center = c
        )
        // cincin progres berputar (indeterminate — tugas % tak dipalsukan)
        rotate(degrees = rot(1.6f), pivot = c) {
            drawArc(
                color = em(palette.accent, C_EMERALD),
                startAngle = -90f, sweepAngle = 100f, useCenter = false,
                style = Stroke(width = R * 0.043f, cap = StrokeCap.Round)
            )
        }
        drawCircle(
            color = em(palette.accent, C_EMERALD).copy(alpha = 0.14f),
            radius = R * 0.83f, center = c,
            style = Stroke(width = R * 0.043f)
        )
    }

    // ============================================================
    // 7) EAR — sonar DENGAR: SATU cincin tipis melebar pelan (3.4dtk)
    // ============================================================
    if (hear > 0.01f) {
        val ep = cyc(3.4f)
        drawCircle(
            color = C_EM_SOFT.copy(alpha = 0.55f * (1f - ep) * hear),
            radius = R * 1.107f * (0.86f + 0.76f * ep),
            center = c,
            style = Stroke(width = R * 0.027f)
        )
    }
}
