package com.anztdree.avi.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.ai.GeminiModelInfo
import com.anztdree.avi.ai.ModelTestResult
import com.anztdree.avi.ui.theme.AviBody
import com.anztdree.avi.ui.theme.AviCode
import com.anztdree.avi.ui.theme.AviDisplay
import com.anztdree.avi.ui.theme.AviHead
import com.anztdree.avi.ui.theme.AviHud
import com.anztdree.avi.ui.theme.AviPalette
import com.anztdree.avi.ui.theme.LocalAviPalette
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    val palette = LocalAviPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.danger.copy(alpha = 0.12f))
            .border(1.dp, palette.danger.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(start = 12.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(palette.danger.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text("!", color = palette.danger, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            color = palette.danger,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Tutup",
                tint = palette.danger,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

// ============================================================
// Kartu setup otak
// ============================================================

@Composable
fun SetupCard(onOpenSettings: () -> Unit) {
    val palette = LocalAviPalette.current
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.panel)
            .border(1.dp, palette.gold.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            "PASANG OTAK AVI",
            color = palette.gold,
            fontSize = 12.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Otak AVI belum terpasang, pak. Buka aistudio.google.com/apikey, buat API key gratis, lalu tempel di Pusat Kendali panel OTAK dan pilih model.",
            color = palette.textDim,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                        )
                    } catch (_: Exception) {
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.gold,
                    contentColor = palette.bg
                )
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Buka AI Studio", fontSize = 12.sp)
            }
            OutlinedButton(onClick = onOpenSettings) {
                Text("Ke Pusat Kendali", fontSize = 12.sp, color = palette.accent)
            }
        }
    }
}
// ============================================================
// P43 — PESAN: panel HUD berhias (porting setia preview .msg):
// avatar mini-orb (AVI) / segel heksagon emas (PAK) + meta berchip
// jam + bubble ber-PUNGGUNGAN aksen menyala + sudut bidik.
// TIDAK POLOS lagi — persis bahasa preview yang disetujui.
// ============================================================

@Composable
fun MessageBubble(role: String, text: String, streaming: Boolean = false, time: String? = null) {
    val palette = LocalAviPalette.current
    val isUser = role == "user"
    // ekor sudut menghadap pengirim — identitas visual tiap sisi
    val shape = if (isUser)
        RoundedCornerShape(20.dp, 3.dp, 20.dp, 20.dp)
    else
        RoundedCornerShape(3.dp, 20.dp, 20.dp, 20.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isUser) PakAvatar(palette = palette) else AviAvatar(palette = palette)
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // mmeta — nama + chip jam (mono berjersey)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isUser) {
                    if (time != null) TimeChip(time, palette)
                    Spacer(Modifier.width(7.dp))
                }
                Text(
                    if (isUser) "PAK" else "AVI",
                    color = if (isUser) palette.gold else palette.accent,
                    fontSize = 9.5.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.4.sp
                )
                if (!isUser) {
                    Spacer(Modifier.width(7.dp))
                    if (time != null) TimeChip(time, palette)
                }
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                (if (isUser) palette.userBubble else palette.aviBubble).copy(alpha = 0.94f),
                                if (isUser) palette.userBubble else palette.aviBubble
                            )
                        )
                    )
                    .border(1.dp, palette.textDim.copy(alpha = 0.27f), shape)
                    // pendaran radial di sudut atas (radial-gradient .bub) + PUNGGUNGAN
                    // aksen menyala (bub::before) + sudut bidik (::after) — di atas latar
                    .drawBehind {
                        val tint = if (isUser) palette.gold else palette.accent
                        drawRect(
                            brush = Brush.radialGradient(
                                listOf(tint.copy(alpha = 0.10f), Color.Transparent),
                                center = if (isUser) Offset(size.width, 0f) else Offset(0f, 0f),
                                radius = size.width * 1.3f
                            ),
                            topLeft = Offset.Zero, size = size
                        )
                        // PUNGGUNGAN aksen menyala — kiri utk AVI, kanan utk PAK
                        val spineX = if (isUser) size.width - 2.5.dp.toPx() else 0f
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    (if (isUser) palette.gold else palette.accent),
                                    (if (isUser) palette.gold else palette.accent).copy(alpha = 0.06f)
                                ),
                                startY = 9.dp.toPx(), endY = size.height - 9.dp.toPx()
                            ),
                            topLeft = Offset(spineX, 9.dp.toPx()),
                            size = Size(2.5.dp.toPx(), size.height - 18.dp.toPx())
                        )
                        // sudut bidik di ekor bubble (::after) — aksen pengirim
                        val ck = if (isUser) palette.gold else palette.accent
                        val w = 2.dp.toPx()
                        val s = 9.dp.toPx()
                        val bx = if (isUser) size.width - s else 0f
                        val by = size.height - s
                        drawLine(ck.copy(alpha = 0.75f), Offset(bx, by + w / 2f), Offset(bx + s, by + w / 2f), w)
                        drawLine(ck.copy(alpha = 0.75f), Offset(bx + w / 2f, by), Offset(bx + w / 2f, by + s), w)
                    }
                    .padding(
                        start = if (isUser) 17.dp else 20.dp,
                        end = if (isUser) 20.dp else 17.dp,
                        top = 13.dp,
                        bottom = 13.dp
                    )
            ) {
                if (streaming && text.isEmpty()) {
                    // menunggu suku kata pertama — tetesan mengetik
                    TypingDots(palette = palette)
                } else {
                    Row {
                        Text(
                            text = text,
                            color = palette.text,
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp,
                            fontFamily = AviBody
                        )
                        if (streaming) {
                            val blink = rememberInfiniteTransition(label = "cursor")
                            val on by blink.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.1f,
                                animationSpec = infiniteRepeatable(tween(460, easing = LinearEasing)),
                                label = "on"
                            )
                            Text(
                                "▌",
                                color = palette.accent,
                                fontSize = 14.5.sp,
                                fontFamily = AviBody,
                                modifier = Modifier.graphicsLayer { alpha = on }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Chip jam kecil (mmeta .mts preview) — mono berbingkai */
@Composable
private fun TimeChip(time: String, palette: AviPalette) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(palette.panel.copy(alpha = 0.6f))
            .border(1.dp, palette.textDim.copy(alpha = 0.22f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 1.5.dp)
    ) {
        Text(
            time,
            color = palette.textDim,
            fontSize = 8.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        )
    }
}

/**
 * Avatar AVI — mini-orb identik lambang header preview (.mav):
 * ring tick mesin + inti kaca gelap + heksagram ganda + pip emas.
 * Statis (tanpa loop per pesan — hemat baterai, wajah tetap sama).
 */
@Composable
private fun AviAvatar(palette: AviPalette) {
    Canvas(Modifier.size(34.dp)) {
        val c = center
        val R = size.minDimension / 2f
        fun pt(r: Float, deg: Float) = Offset(
            c.x + r * cos(deg * PI.toFloat() / 180f),
            c.y + r * sin(deg * PI.toFloat() / 180f)
        )
        // pendaran lembut
        drawCircle(palette.accent.copy(alpha = 0.14f), radius = R * 1.25f, center = c)
        // ring tick mesin — 30 gerigi
        for (k in 0 until 30) {
            val a = k * 12f
            drawLine(
                palette.textDim.copy(alpha = 0.50f),
                pt(R * 0.90f, a), pt(R, a),
                strokeWidth = 1.dp.toPx()
            )
        }
        // inti kaca gelap (cd)
        drawCircle(
            brush = Brush.radialGradient(
                0f to Color.White.copy(alpha = 0.36f),
                0.42f to palette.accent.copy(alpha = 0.20f),
                0.88f to Color(0xFF0A1322).copy(alpha = 0.96f),
                1f to Color(0xFF05080F),
                center = Offset(c.x - R * 0.17f, c.y - R * 0.23f),
                radius = R * 1.05f
            ),
            radius = R * 0.87f, center = c
        )
        drawCircle(palette.accent.copy(alpha = 0.32f), radius = R * 0.87f, center = c, style = Stroke(1.dp.toPx()))
        // heksagram ganda — sian atas, emas bawah (statik, miring -13°)
        val hr = R * 0.50f
        val rot = -13f
        fun tri(base: Float, col: Color, w: Float) {
            for (k in 0..2) {
                val a1 = (rot + base - 90f + k * 120f) * PI.toFloat() / 180f
                val a2 = (rot + base - 90f + ((k + 1) % 3) * 120f) * PI.toFloat() / 180f
                drawLine(
                    col,
                    Offset(c.x + hr * cos(a1), c.y + hr * sin(a1)),
                    Offset(c.x + hr * cos(a2), c.y + hr * sin(a2)),
                    strokeWidth = w, cap = StrokeCap.Round
                )
            }
        }
        tri(0f, palette.accent, 1.6.dp.toPx())
        tri(180f, palette.gold.copy(alpha = 0.9f), 1.3.dp.toPx())
        // pip emas di tepi — denyut identitas
        val pip = pt(R * 0.93f, -45f)
        drawCircle(palette.gold.copy(alpha = 0.45f), radius = R * 0.16f, center = pip)
        drawCircle(palette.gold, radius = R * 0.10f, center = pip)
    }
}

/** Avatar PAK — segel heksagon emas bermonogram (mavP preview) */
@Composable
private fun PakAvatar(palette: AviPalette) {
    Box(Modifier.size(width = 31.dp, height = 33.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val R = size.minDimension / 2f * 0.94f
            val hex = Path()
            for (k in 0 until 6) {
                val a = (-90f + k * 60f) * PI.toFloat() / 180f
                val p = Offset(c.x + R * cos(a), c.y + R * sin(a))
                if (k == 0) hex.moveTo(p.x, p.y) else hex.lineTo(p.x, p.y)
            }
            hex.close()
            drawPath(hex, palette.gold.copy(alpha = 0.12f))
            drawPath(hex, palette.gold.copy(alpha = 0.85f), style = Stroke(1.2.dp.toPx()))
        }
        Text(
            "P",
            color = palette.gold,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = AviDisplay
        )
    }
}

/** Tiga tetesan melompat bergiliran — AVI sedang berpikir */
@Composable
fun TypingDots(palette: AviPalette) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        repeat(3) { i ->
            val p by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(820, easing = LinearEasing),
                    initialStartOffset = StartOffset(i * 160)
                ),
                label = "dot$i"
            )
            // gelombang segitiga 0→1→0
            val bob = if (p < 0.5f) p * 2f else (1f - p) * 2f
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(7.dp)
                    .graphicsLayer { translationY = -(4f * bob).dp.toPx() }
                    .clip(CircleShape)
                    .background(if (i == 1) palette.gold else palette.accent)
            )
        }
    }
}
