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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.anztdree.avi.ui.theme.AviPalette
import kotlin.math.sin

/**
 * K19 — BINGKAI HUD KELAS JARVIS (lapisan paling dekoratif, paling hemat):
 *  · 4 bracket sudut L dengan titik sudut emas — bingkai khas antarmuka Stark
 *  · rel vertikal tipis di kedua sisi dengan tick pendek merambat pelan
 *  · sapuan hologram: satu garis + pita lembut melintasi layar tiap ±11 dtk
 * Seluruhnya Canvas murni tanpa alokasi per frame; mati otomatis di hemat daya.
 */
@Composable
fun HudFrameOverlay(palette: AviPalette, enabled: Boolean, modifier: Modifier = Modifier) {
    if (!enabled) return
    val transition = rememberInfiniteTransition(label = "hudFrame")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing)),
        label = "t"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val inset = 7.dp.toPx()
        val arm = 24.dp.toPx()
        val stroke = 2f

        // ---- 4 bracket sudut (dua warna: accent + titik sudut emas) ----
        fun bracket(x: Float, y: Float, dx: Float, dy: Float) {
            drawLine(palette.accent.copy(alpha = 0.55f), Offset(x, y), Offset(x + dx * arm, y), stroke)
            drawLine(palette.accent.copy(alpha = 0.55f), Offset(x, y), Offset(x, y + dy * arm), stroke)
            // ujung bracket sedikit menyala
            drawCircle(color = palette.accent, radius = 1.6f, center = Offset(x, y), alpha = 0.8f)
            drawCircle(color = palette.gold, radius = 1.1f, center = Offset(x, y), alpha = 0.9f)
        }
        bracket(inset, inset, 1f, 1f)
        bracket(w - inset, inset, -1f, 1f)
        bracket(inset, h - inset, 1f, -1f)
        bracket(w - inset, h - inset, -1f, -1f)

        // ---- rel samping tipis + tick merambat (kanan & kiri) ----
        val railAlpha = 0.10f
        drawLine(palette.accent, Offset(inset, inset + arm), Offset(inset, h - inset - arm), 1f, alpha = railAlpha)
        drawLine(palette.accent, Offset(w - inset, inset + arm), Offset(w - inset, h - inset - arm), 1f, alpha = railAlpha)
        val tickTravel = 0.5f + 0.5f * sin(t * 2f * Math.PI.toFloat())
        for (side in listOf(inset, w - inset)) {
            val yTick = inset + arm + (h - 2f * (inset + arm)) * tickTravel
            drawLine(palette.gold, Offset(side - 3f, yTick), Offset(side + 3f, yTick), 2f, alpha = 0.5f)
        }

        // ---- sapuan hologram: pita lembut + garis inti melintasi layar ----
        val y = h * t
        drawLine(palette.accent, Offset(0f, y), Offset(w, y), 1.4f, alpha = 0.09f)
        drawLine(palette.accent, Offset(0f, y - 9f), Offset(w, y - 9f), 22f, alpha = 0.035f)
        drawLine(palette.accent, Offset(0f, y + 9f), Offset(w, y + 9f), 22f, alpha = 0.035f)

        // denyut sudut halus — reaktor "bernapas" sampai ke tepi layar
        val pulse = 0.5f + 0.5f * sin(t * 2f * Math.PI.toFloat() * 2f)
        drawCircle(
            color = palette.accent.copy(alpha = 0.05f * pulse),
            radius = 60.dp.toPx(),
            center = Offset(inset, h - inset)
        )
    }
}
