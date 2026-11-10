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

// ============================================================
// Chip & bar atas
// ============================================================

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "chipDot")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "pulse"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .graphicsLayer { alpha = pulse }
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontFamily = AviHud,
            letterSpacing = 1.sp,
            maxLines = 1
        )
    }
}

@Composable
fun TopBarChip(label: String, color: Color, onClick: (() -> Unit)? = null) {
    val palette = LocalAviPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.panel)
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(50))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontFamily = AviHud,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 110.dp)
        )
    }
}

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

// ============================================================
// Judul panel
// ============================================================

@Composable
fun SectionTitle(text: String, palette: AviPalette = LocalAviPalette.current) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(Modifier.width(3.dp).height(14.dp).background(palette.accent))
        Spacer(Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            color = palette.accent,
            fontSize = 11.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun SystemHeader(name: String, active: Boolean, palette: AviPalette = LocalAviPalette.current) {
    val transition = rememberInfiniteTransition(label = "sysdot")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "pulse"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (active) palette.accent else palette.textDim.copy(alpha = 0.4f))
                .alpha(if (active) 0.45f + 0.55f * pulse else 1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            color = if (active) palette.text else palette.textDim,
            fontSize = 13.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        if (active) {
            Spacer(Modifier.width(8.dp))
            Text(
                "AKTIF",
                color = palette.accent.copy(alpha = 0.8f),
                fontSize = 9.sp,
                fontFamily = AviHud,
                letterSpacing = 1.sp
            )
        }
    }
}

// ============================================================
// Kontrol gaya reaktor
// ============================================================

@Composable
fun ReactorToggle(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    palette: AviPalette = LocalAviPalette.current
) {
    val thumbShift by animateDpAsState(targetValue = if (checked) 26.dp else 3.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onChange(!checked) }
            .padding(horizontal = 2.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = palette.text,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (checked) palette.accent.copy(alpha = 0.30f) else palette.panelLight)
                .border(
                    1.dp,
                    if (checked) palette.accent.copy(alpha = 0.8f) else palette.textDim.copy(alpha = 0.3f),
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier
                    .offset(x = thumbShift)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (checked) palette.accent else palette.textDim)
                    .border(2.dp, if (checked) palette.accentSoft else palette.panel, CircleShape)
            )
        }
    }
}

@Composable
fun PowerDial(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    display: String,
    onChange: (Float) -> Unit,
    palette: AviPalette = LocalAviPalette.current
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = palette.textDim, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(
                display,
                color = palette.accent,
                fontSize = 12.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = palette.accent,
                activeTrackColor = palette.accent,
                inactiveTrackColor = palette.panelLight,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}

// ============================================================
// Gauge radial
// ============================================================

@Composable
fun GaugeRing(
    label: String,
    valueText: String,
    progress: Float,
    palette: AviPalette = LocalAviPalette.current,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.size(108.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(108.dp)) {
                val p = progress.coerceIn(0f, 1f)
                drawArc(
                    color = palette.panelLight,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = palette.accent.copy(alpha = 0.25f),
                    startAngle = 135f,
                    sweepAngle = 270f * p,
                    useCenter = false,
                    style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = palette.accent,
                    startAngle = 135f,
                    sweepAngle = 270f * p,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    valueText,
                    color = palette.text,
                    fontSize = 13.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    label,
                    color = palette.textDim,
                    fontSize = 9.sp,
                    fontFamily = AviHud,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================================
// Panel HUD
// ============================================================

@Composable
fun HoloPanel(
    palette: AviPalette = LocalAviPalette.current,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.panel.copy(alpha = 0.92f))
            .border(1.dp, palette.accent.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
    ) {
        // garis scan aksen di tepi atas
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, palette.accent.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        )
        Column(Modifier.padding(14.dp), content = content)
    }
}

// ============================================================
// (StatusDock & ChipRow DIHAPUS TOTAL — keputusan pemilik K10:
//  data status kini mengorbit reaktor di ReactorHalo)
// ============================================================

// ============================================================
// Kartu model — "inti daya"
// ============================================================

private fun formatContext(limit: Long): String = when {
    limit >= 1_000_000L -> String.format(Locale.US, "%.1fM", limit / 1_000_000.0)
    limit >= 1_000L -> String.format(Locale.US, "%.0fK", limit / 1_000.0)
    else -> limit.toString()
}

@Composable
fun ModelCard(
    info: GeminiModelInfo,
    selected: Boolean,
    onSelect: () -> Unit,
    test: ModelTestResult? = null,
    palette: AviPalette = LocalAviPalette.current
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) palette.accent.copy(alpha = 0.10f) else palette.panelLight.copy(alpha = 0.6f))
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) palette.accent else palette.panelLight,
                RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                info.displayName,
                color = palette.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AviHead,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (info.isPreview) MiniBadge("UJI COBA", palette.gold)
            if (info.canTts) MiniBadge("TTS", palette.gold)
            if (info.canLive) MiniBadge("LIVE", palette.accent)
        }
        Spacer(Modifier.height(3.dp))
        Text(
            info.shortName,
            color = palette.accent,
            fontSize = 10.sp,
            fontFamily = AviCode,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Konteks: ${formatContext(info.contextLimit)}",
                color = palette.textDim,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            if (test != null) {
                Spacer(Modifier.width(8.dp))
                if (test.testing) {
                    Text(
                        "⋯ MENGUJI",
                        color = palette.goldSoft,
                        fontSize = 10.sp,
                        fontFamily = AviHud,
                        fontWeight = FontWeight.Bold
                    )
                } else if (test.ok) {
                    Text(
                        "✓ SIAP",
                        color = palette.accent,
                        fontSize = 10.sp,
                        fontFamily = AviHud,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "✗ GAGAL",
                        color = palette.danger,
                        fontSize = 10.sp,
                        fontFamily = AviHud,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        // Alasan gagal — ditampilkan utuh supaya penyebabnya jelas
        if (test != null && !test.testing && !test.ok) {
            Spacer(Modifier.height(4.dp))
            Text(
                test.message,
                color = palette.danger.copy(alpha = 0.9f),
                fontSize = 9.sp,
                lineHeight = 13.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MiniBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            text,
            color = color,
            fontSize = 8.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold
        )
    }
}
