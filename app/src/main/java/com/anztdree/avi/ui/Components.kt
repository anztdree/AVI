package com.anztdree.avi.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.ai.GeminiModelInfo
import com.anztdree.avi.ai.ModelTestResult
import com.anztdree.avi.ui.theme.AviPalette
import com.anztdree.avi.ui.theme.LocalAviPalette
import java.util.Locale

// ============================================================
// Chip & bar atas
// ============================================================

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
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
            fontFamily = FontFamily.Monospace,
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
            fontFamily = FontFamily.Monospace,
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
// Bubble chat
// ============================================================

@Composable
fun MessageBubble(role: String, text: String, streaming: Boolean = false) {
    val palette = LocalAviPalette.current
    val isUser = role == "user"
    val bg = if (isUser) palette.userBubble else palette.aviBubble
    val border = if (isUser) palette.userBorder else palette.aviBorder
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .border(1.dp, border.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (streaming) "$text▌" else text,
                color = palette.text,
                fontSize = 14.sp,
                lineHeight = 20.sp
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
            fontFamily = FontFamily.Monospace,
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
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        if (active) {
            Spacer(Modifier.width(8.dp))
            Text(
                "AKTIF",
                color = palette.accent.copy(alpha = 0.8f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
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
                fontFamily = FontFamily.Monospace,
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
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    label,
                    color = palette.textDim,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
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
// Dock status bawah reaktor — STATIS (tidak pernah menabrak animasi)
// ============================================================

@Composable
fun StatusDock(
    items: List<Pair<String, String>>,
    palette: AviPalette = LocalAviPalette.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.panel.copy(alpha = 0.78f))
            .border(1.dp, palette.accent.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, pair ->
            if (index > 0) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(palette.accent.copy(alpha = 0.18f))
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pair.first,
                    color = palette.textDim,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = pair.second,
                    color = palette.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}

// ============================================================
// Chip perintah cepat
// ============================================================

@Composable
fun ChipRow(
    chips: List<String>,
    enabled: Boolean,
    onChip: (String) -> Unit,
    palette: AviPalette = LocalAviPalette.current
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(chips) { chip ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(palette.panelLight.copy(alpha = 0.85f))
                    .border(1.dp, palette.accent.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .clickable(enabled = enabled) { onChip(chip) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(chip, color = palette.text, fontSize = 12.sp)
            }
        }
    }
}

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
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
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
            fontFamily = FontFamily.Monospace,
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
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                } else if (test.ok) {
                    Text(
                        "✓ SIAP",
                        color = palette.accent,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "✗ GAGAL",
                        color = palette.danger,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
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
                color = palette.danger.copy(alpha = 0.85f),
                fontSize = 9.sp,
                lineHeight = 12.sp,
                maxLines = 3,
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
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
