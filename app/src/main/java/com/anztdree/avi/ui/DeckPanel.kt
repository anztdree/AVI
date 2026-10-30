package com.anztdree.avi.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.Protocol
import com.anztdree.avi.ui.theme.AviBody
import com.anztdree.avi.ui.theme.AviDisplay
import com.anztdree.avi.ui.theme.AviHud
import com.anztdree.avi.ui.theme.AviPalette
import com.anztdree.avi.ui.theme.LocalAviPalette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
// K19 — DECK MODUL: panel modul kaya ala markas Jarvis.
// Modul NYATA : WAKTU, OTAK, DIAGNOSTIK, PINTASAN, PROTOKOL, MEMORI.
// Modul ANTRIAN (badge SEGERA, tampilan sudah hidup): CUACA, MEDIA —
// sesuai keputusan pemilik: fitur kaya boleh dipajang walau belum work.
// ============================================================

@Composable
fun DeckPanel(
    vm: AviViewModel,
    settings: AviSettings,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val palette = LocalAviPalette.current
    val diagnostics by vm.diagnostics.collectAsState()
    val protocols by vm.protocols.collectAsState()
    val memories by vm.memories.collectAsState()
    val usageToday by vm.usageToday.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.refreshDiagnostics() }

    val versi = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }
    }

    // ---- jam & tanggal berjalan (detik nyata) ----
    var jamText by remember { mutableStateOf("--.--.--") }
    var tanggalText by remember { mutableStateOf("—") }
    LaunchedEffect(Unit) {
        val fJam = SimpleDateFormat("HH.mm.ss", Locale.getDefault())
        val fTgl = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id"))
        while (true) {
            val now = Date()
            jamText = fJam.format(now)
            tanggalText = fTgl.format(now)
            delay(1000)
        }
    }

    Box(Modifier.fillMaxSize()) {
        // ---- tirai redup — ketuk di luar panel menutup deck ----
        Box(
            Modifier
                .fillMaxSize()
                .background(palette.bg.copy(alpha = 0.78f))
                .clickable { onDismiss() }
        )

        // ---- panel holo meluncur dari bawah ----
        val appear = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = appear,
            enter = slideInVertically(tween(340)) { it } + fadeIn(tween(280)),
            exit = slideOutVertically(tween(240)) { it } + fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.93f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(palette.panel.copy(alpha = 0.985f), palette.bg)
                        )
                    )
                    .border(
                        1.dp,
                        palette.accent.copy(alpha = 0.35f),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp)
            ) {
                // garis scan aksen di bibir atas panel
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, palette.accent.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                )

                Spacer(Modifier.height(8.dp))
                // ============ HEADER ============
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "DECK MODUL",
                            color = palette.text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AviDisplay,
                            letterSpacing = 3.sp
                        )
                        Text(
                            "MODUL MARKAS · v$versi",
                            color = palette.textDim,
                            fontSize = 9.sp,
                            fontFamily = AviHud,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup deck",
                            tint = palette.textDim,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ============ WAKTU + OTAK ============
                Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                    ModuleFrame(title = "WAKTU", badge = "AKTIF", badgeColor = palette.accent, modifier = Modifier.weight(1f)) {
                        Text(
                            jamText,
                            color = palette.accent,
                            fontSize = 22.sp,
                            fontFamily = AviDisplay,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            tanggalText,
                            color = palette.textDim,
                            fontSize = 9.5.sp,
                            fontFamily = AviHud,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ModuleFrame(title = "OTAK", badge = "AKTIF", badgeColor = palette.accent, modifier = Modifier.weight(1f)) {
                        Text(
                            if (settings.provider == com.anztdree.avi.data.AiProvider.GEMINI) "GEMINI" else "KOMPATIBEL-OPENAI",
                            color = palette.text,
                            fontSize = 13.sp,
                            fontFamily = AviDisplay,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            settings.geminiModel.removePrefix("models/").ifBlank {
                                settings.ocModel.ifBlank { "BELUM DIPASANG" }
                            },
                            color = palette.accent,
                            fontSize = 9.5.sp,
                            fontFamily = AviHud,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "PAKAI HARI INI: $usageToday PERMINTAAN",
                            color = palette.textDim,
                            fontSize = 8.5.sp,
                            fontFamily = AviHud
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(7.dp))
                                .clickable { onDismiss(); onOpenSettings() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                "ATUR OTAK",
                                color = palette.accent,
                                fontSize = 9.sp,
                                fontFamily = AviHud,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ============ DIAGNOSTIK MARKAS (nyata) ============
                ModuleFrame(title = "DIAGNOSTIK MARKAS", badge = "LANGSUNG", badgeColor = palette.accent) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                    ) {
                        val d = diagnostics
                        MiniGauge(
                            label = "BATERAI",
                            valueText = if (d == null) "—" else "${d.batteryPct}%",
                            progress = if (d == null) 0f else d.batteryPct / 100f,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        MiniGauge(
                            label = "RAM",
                            valueText = if (d == null) "—" else "${d.ramUsedPct}%",
                            progress = if (d == null) 0f else d.ramUsedPct / 100f,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        MiniGauge(
                            label = "SIMPAN",
                            valueText = if (d == null) "—" else "${"%.0f".format(d.storageFreeGb)}GB",
                            progress = if (d == null || d.storageTotalGb <= 0f)
                                0f
                            else
                                ((d.storageTotalGb - d.storageFreeGb) / d.storageTotalGb).coerceIn(0f, 1f),
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        MiniGauge(
                            label = "JARINGAN",
                            valueText = d?.network?.take(7) ?: "—",
                            progress = if (d == null || d.network == "OFFLINE") 0f else 1f,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ============ PINTASAN (nyata — perintah lokal instan) ============
                ModuleFrame(title = "PINTASAN MARKAS", badge = "NYATA", badgeColor = palette.accent) {
                    val perintah = listOf(
                        "Jam berapa?", "Tanggal berapa?", "Baterai berapa?",
                        "Kondisi HP", "Sisa penyimpanan", "Jaringan apa?"
                    )
                    Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                        perintah.chunked(3).forEach { baris ->
                            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                                baris.forEach { cmd ->
                                    HudCommandChip(text = cmd, modifier = Modifier.weight(1f)) {
                                        onDismiss()
                                        vm.sendText(cmd)
                                    }
                                }
                                repeat(3 - baris.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ============ CUACA (tampilan hidup — antre penyedia) ============
                ModuleFrame(title = "MODUL CUACA", badge = "SEGERA", badgeColor = palette.gold) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = palette.gold,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "—°C · MENUNGGU SAMBUNGAN",
                                color = palette.text,
                                fontSize = 13.sp,
                                fontFamily = AviDisplay,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Antarmuka sudah berdiri; menunggu penyedia cuaca gratis tanpa kunci (mis. Open-Meteo).",
                                color = palette.textDim,
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp,
                                fontFamily = AviBody
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ============ MEDIA (tampilan hidup — antre MediaSession) ============
                ModuleFrame(title = "PEMUTAR MEDIA", badge = "SEGERA", badgeColor = palette.gold) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MediaButton(palette.panelLight, palette.textDim) { }
                        Spacer(Modifier.width(14.dp))
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(palette.accent.copy(alpha = 0.16f))
                                .border(1.5.dp, palette.accent.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Putar (segera)",
                                tint = palette.accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        MediaButton(palette.panelLight, palette.textDim) { }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "TIDAK ADA LAGU",
                                color = palette.textDim,
                                fontSize = 11.sp,
                                fontFamily = AviHud,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Kontrol pemutar musik HP lewat MediaSession — dalam antrean modul.",
                                color = palette.textDim.copy(alpha = 0.8f),
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp,
                                fontFamily = AviBody
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ============ PROTOKOL (nyata) ============
                ModuleFrame(title = "PROTOKOL", badge = "NYATA", badgeColor = palette.accent) {
                    if (protocols.isEmpty()) {
                        Text(
                            "Belum ada protokol — buat di Pusat Kendali panel MEMORI.",
                            color = palette.textDim,
                            fontSize = 10.sp,
                            fontFamily = AviHud
                        )
                    } else {
                        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                            protocols.forEach { p ->
                                ProtocolRow(p = p, palette = palette, onRun = {
                                    onDismiss()
                                    vm.runProtocol(p)
                                })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ============ MEMORI (nyata) ============
                ModuleFrame(title = "MEMORI AVI", badge = "NYATA", badgeColor = palette.accent) {
                    if (memories.isEmpty()) {
                        Text(
                            "Belum ada fakta tersimpan. Bilang ke AVI: \u201Cingat bahwa…\u201D",
                            color = palette.textDim,
                            fontSize = 10.sp,
                            fontFamily = AviHud
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 130.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                        ) {
                            memories.forEach { mem ->
                                Text(
                                    "• ${mem.text}",
                                    color = palette.text,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "ANTREAN MODUL BERIKUT: CUACA · MEDIA · ALARM · AGENDA · BERITA",
                    color = palette.textDim.copy(alpha = 0.7f),
                    fontSize = 8.5.sp,
                    fontFamily = AviHud,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ============================================================
// Kartu modul — bingkai holo dengan pita judul + badge status
// ============================================================

@Composable
private fun ModuleFrame(
    title: String,
    badge: String?,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalAviPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(palette.panel.copy(alpha = 0.62f))
            .border(1.dp, palette.accent.copy(alpha = 0.22f), RoundedCornerShape(13.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 10.dp, top = 9.dp)
        ) {
            Box(Modifier.width(3.dp).height(11.dp).background(palette.accent))
            Spacer(Modifier.width(7.dp))
            Text(
                title,
                color = palette.text,
                fontSize = 10.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.weight(1f))
            if (badge != null) DeckBadge(badge, badgeColor)
        }
        Column(
            Modifier.padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 11.dp),
            content = content
        )
    }
}

@Composable
private fun DeckBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text,
            color = color,
            fontSize = 8.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

// ============================================================
// Gauge mini — lingkaran arc 58dp + nilai + label (kompak utk deck)
// ============================================================

@Composable
private fun MiniGauge(
    label: String,
    valueText: String,
    progress: Float,
    palette: AviPalette,
    modifier: Modifier = Modifier
) {
    val p = progress.coerceIn(0f, 1f)
    val warna = if (p >= 0.85f) palette.danger else palette.accent
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(Modifier.size(54.dp)) {
                drawArc(
                    color = palette.panelLight,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = warna,
                    startAngle = 135f,
                    sweepAngle = 270f * p,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                valueText,
                color = if (p >= 0.85f) palette.danger else palette.text,
                fontSize = 9.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Text(
            label,
            color = palette.textDim,
            fontSize = 8.sp,
            fontFamily = AviHud,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
    }
}

// ============================================================
// Chip perintah pintas — dipakai deck & deretan chip layar utama
// ============================================================

@Composable
fun HudCommandChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalAviPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(palette.panelLight.copy(alpha = 0.85f))
            .border(1.dp, palette.accent.copy(alpha = 0.35f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text,
            color = palette.accent,
            fontSize = 10.sp,
            fontFamily = AviHud,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================
// Baris protokol ringkas di deck
// ============================================================

@Composable
private fun ProtocolRow(p: Protocol, palette: AviPalette, onRun: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(palette.panelLight.copy(alpha = 0.5f))
            .border(1.dp, palette.accent.copy(alpha = 0.2f), RoundedCornerShape(9.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                p.name,
                color = palette.text,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${p.steps.size} langkah · \u201C${p.phrase}\u201D",
                color = palette.textDim,
                fontSize = 9.sp,
                fontFamily = AviHud,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(palette.accent.copy(alpha = 0.14f))
                .border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(7.dp))
                .clickable { onRun() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                "JALANKAN",
                color = palette.accent,
                fontSize = 9.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MediaButton(bg: Color, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bg.copy(alpha = 0.6f))
            .border(1.dp, tint.copy(alpha = 0.4f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.7f))
        )
    }
}
