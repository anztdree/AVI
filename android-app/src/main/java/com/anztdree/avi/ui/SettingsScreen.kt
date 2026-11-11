package com.anztdree.avi.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.AviState
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.R
import com.anztdree.avi.agent.AgentEngine
import com.anztdree.avi.agent.Otoritas
import com.anztdree.avi.ai.DiagnoseResult
import com.anztdree.avi.ai.ModelCatalog
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.Protocol
import com.anztdree.avi.ui.theme.AviDisplay
import com.anztdree.avi.ui.theme.AviHud
import com.anztdree.avi.ui.theme.AviPalette
import com.anztdree.avi.ui.theme.AviTheme
import com.anztdree.avi.ui.theme.LocalAviPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.delay

/**
 * P45 — PUSAT KENDALI DITULIS ULANG (umpan Pak: "Pengaturan emang kaya tapi
 * isinya gak jelas semua!!"):
 *  · 5 TAB SAMAR DIHAPUS — kini SATU HALAMAN GULIR dengan 5 seksi berlabel
 *    manusiawi: OTAK · SUARA · AGEN · GAYA · MARKAS
 *  · Setiap baris bergaya kartu preview (ikon 36dp + judul + keterangan satu
 *    baris + kendali di kanan) — ketuk baris = BUKA PENJELASAN lengkapnya
 *  · KUNCI API jadi baris pertama OTAK dengan status jelas: TERPASANG/BELUM
 *  · MODEL KECERDASAN = daftar radio ketuk-pilih (bukan jargon) dari katalog
 *    Google live + uji otomatis saat dipilih
 *  · Semua fitur asli dipertahankan: agen + perkakas + log, ingatan,
 *    protokol, gaya, diagnosa — tanpa satu pun fitur hilang
 *  · "MODUL BERIKUTNYA" (6 kartu stub SEGERA) DIBUANG — tidak ada lagi
 *    isi palsu ("isinya gak jelas")
 * Satu keluarga visual dengan layar obrolan: nebula per-Mark + hex grid +
 * bingkai HUD. Bahasa UI: Indonesia yang jelas, bukan singkatan HUD.
 */

// ============================================================
// PINTU MASUK — bungkus tema agar live-preview tema Mark tetap jalan
// ============================================================

@Composable
fun SettingsScreen(vm: AviViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    AviTheme(mark = settings.themeMark) {
        SettingsScreenContent(vm = vm, settings = settings, onBack = onBack)
    }
}

@Composable
private fun SettingsScreenContent(vm: AviViewModel, settings: AviSettings, onBack: () -> Unit) {
    val palette = LocalAviPalette.current
    val context = LocalContext.current
    val ttsReady by vm.ttsReady.collectAsState()

    // versi build — dinamis (ikut build, bukan teks manual)
    val versiApp = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }
    }

    // Jam markas — WIB hidup tiap detik
    val clockFmt = remember {
        SimpleDateFormat("HH:mm:ss", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
        }
    }
    var clock by remember { mutableStateOf("--:--:--") }
    LaunchedEffect(Unit) {
        while (true) {
            clock = clockFmt.format(Date())
            delay(1000)
        }
    }

    // hasil TES MENDENGAR (tab SUARA)
    var tesDengarHasil by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(palette.bg)) {
        // ===== latar satu keluarga dgn layar obrolan =====
        val nebulaRes = when (settings.themeMark) {
            "MK2" -> R.drawable.bg_nebula_mk2
            "MK3" -> R.drawable.bg_nebula_mk3
            else -> R.drawable.bg_nebula
        }
        Image(
            painter = painterResource(nebulaRes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.45f
        )
        HexGridBackground(
            palette = palette,
            enabled = settings.parallaxEnabled && !settings.powerSave,
            modifier = Modifier.matchParentSize()
        )
        Box(
            Modifier
                .matchParentSize()
                .background(palette.bg.copy(alpha = 0.42f))
        )
        HudFrameOverlay(
            palette = palette,
            enabled = settings.hudFrame && !settings.powerSave,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ============ HEADER: kembali + judul + jam hidup ============
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke layar depan",
                        tint = palette.text
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "PENGATURAN",
                        color = palette.text,
                        fontSize = 16.sp,
                        fontFamily = AviDisplay,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    )
                    Text(
                        "PUSAT KENDALI AVI · v$versiApp · $clock WIB",
                        color = palette.textDim,
                        fontSize = 8.5.sp,
                        fontFamily = AviHud,
                        letterSpacing = 1.6.sp
                    )
                }
                CoreBadge(palette = palette)
            }
            Box(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.3f to palette.gold.copy(alpha = 0.5f),
                            0.72f to palette.accent.copy(alpha = 0.5f),
                            1f to Color.Transparent
                        )
                    )
            )

            // ============ SATU HALAMAN GULIR — 5 SEKSI JELAS ============
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // ================= SEKSI 1 — OTAK =================
                OtakSection(vm, settings, palette)
                Spacer(Modifier.height(20.dp))

                // ================= SEKSI 2 — SUARA =================
                SuaraSection(vm, settings, palette, ttsReady, tesDengarHasil) { tesDengarHasil = it }
                Spacer(Modifier.height(20.dp))

                // ================= SEKSI 3 — AGEN =================
                AgenSection(vm, settings, palette)
                Spacer(Modifier.height(20.dp))

                // ================= SEKSI 4 — GAYA =================
                GayaSection(vm, settings, palette)
                Spacer(Modifier.height(20.dp))

                // ================= SEKSI 5 — MARKAS =================
                MarkasSection(vm, settings, palette, versiApp)
                Spacer(Modifier.height(14.dp))

                Text(
                    "AVI APK v$versiApp · PUSAT KENDALI AVI",
                    color = palette.textDim.copy(alpha = 0.75f),
                    fontSize = 8.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
            }
        }
    }
}

// ============================================================
// WIDGET BARAK P45 — baris kartu gaya preview (.setrow):
// ikon chip 36dp + judul + keterangan + kendali di kanan.
// Ketuk baris = buka/tutup area penjelasan (chevron ikut memutar).
// ============================================================

/** Label seksi kecil redup dengan garis pudar ke kanan (gaya .clab preview). */
@Composable
private fun SectionLabel(label: String, palette: AviPalette) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Text(
            label.uppercase(),
            color = palette.textDim,
            fontSize = 9.5.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            palette.textDim.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * Baris kartu pengaturan gaya preview. Argumen:
 *  @iconGold  true = chip ikon emas (aksi utama), false = chip sian
 *  @sub       keterangan satu baris — WAJIB jelas, ini kunci "isinya gak jelas"
 *  @control   kendali kanan (saklar/tombol); null → baris memakai chevron
 *  @onClick   aksi ketuk baris (biasanya toggle buka/tutup); null = tak bisa ditekan
 *  @expanded  memutar chevron 90° saat area penjelasan terbuka
 */
@Composable
private fun SetRow(
    icon: ImageVector,
    title: String,
    sub: String,
    palette: AviPalette,
    iconGold: Boolean = false,
    subColor: Color? = null,
    control: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    expanded: Boolean = false
) {
    val chip = if (iconGold) palette.gold else palette.accent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.panel.copy(alpha = 0.72f))
            .border(
                1.dp,
                if (expanded) chip.copy(alpha = 0.45f) else palette.panelLight.copy(alpha = 0.85f),
                RoundedCornerShape(14.dp)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(chip.copy(alpha = 0.11f))
                .border(1.dp, chip.copy(alpha = 0.42f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = chip, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = palette.text,
                fontSize = 12.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                sub,
                color = subColor ?: palette.textDim,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        control?.invoke()
        if (control == null && onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "Tutup" else "Buka",
                tint = palette.textDim.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = if (expanded) 90f else 0f }
            )
        }
    }
}

/** Area penjelasan yang meluncur terbuka di bawah sebuah SetRow. */
@Composable
private fun ExpandArea(
    visible: Boolean,
    palette: AviPalette,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.panel.copy(alpha = 0.55f))
                .border(1.dp, palette.panelLight.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            content = content
        )
    }
}

/** Baris radio model — daftar ketuk-pilih (bukan ketuk-putar). */
@Composable
private fun ModelRadioRow(
    title: String,
    desc: String,
    selected: Boolean,
    testOk: Boolean?,
    palette: AviPalette,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) palette.gold.copy(alpha = 0.12f) else palette.bg.copy(alpha = 0.45f)
            )
            .border(
                1.dp,
                if (selected) palette.gold.copy(alpha = 0.55f) else palette.panelLight,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // titik radio
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .border(
                    1.5.dp,
                    if (selected) palette.gold else palette.panelLight,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(palette.gold)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = palette.text,
                fontSize = 11.5.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                desc,
                color = palette.textDim,
                fontSize = 9.5.sp,
                fontFamily = AviHud,
                lineHeight = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        // lencana hasil uji
        when (testOk) {
            true -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Model siap (uji lulus)",
                tint = palette.accent,
                modifier = Modifier.size(14.dp)
            )
            false -> Icon(
                Icons.Default.Cancel,
                contentDescription = "Model gagal diuji",
                tint = palette.danger,
                modifier = Modifier.size(14.dp)
            )
            null -> Unit
        }
    }
}

/** Chip kecil MULAI/BERHENTI gaya preview (emas → merah LIVE). */
@Composable
private fun MulaiBerhentiChip(
    live: Boolean,
    labelMulai: String = "MULAI",
    labelBerhenti: String = "BERHENTI",
    palette: AviPalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (live) palette.danger.copy(alpha = 0.10f)
                else palette.gold.copy(alpha = 0.08f)
            )
            .border(
                1.dp,
                if (live) palette.danger.copy(alpha = 0.50f)
                else palette.gold.copy(alpha = 0.45f),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 8.dp)
    ) {
        Text(
            if (live) labelBerhenti else labelMulai,
            color = if (live) palette.danger else palette.gold,
            fontSize = 9.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

// ============================================================
// SEKSI 1 — OTAK: kunci API, model kecerdasan, diagnosa
// ============================================================

@Composable
private fun OtakSection(vm: AviViewModel, settings: AviSettings, palette: AviPalette) {
    val context = LocalContext.current
    val models by vm.models.collectAsState()
    val modelsLoading by vm.modelsLoading.collectAsState()
    val modelsError by vm.modelsError.collectAsState()
    val pickNote by vm.pickNote.collectAsState()
    val modelTest by vm.modelTest.collectAsState()
    val batchTest by vm.batchTest.collectAsState()
    val diagnosis by vm.diagnosis.collectAsState()
    val usageToday by vm.usageToday.collectAsState()
    var showKey by remember { mutableStateOf(false) }
    var bukaKunci by remember { mutableStateOf(false) }
    var bukaModel by remember { mutableStateOf(false) }

    val pakaiGemini = settings.provider == AiProvider.GEMINI
    val kunciTerpasang = if (pakaiGemini) settings.geminiKey.isNotBlank() else settings.ocKey.isNotBlank()
    val modelAktif = settings.geminiModel.removePrefix("models/")
    val otakSiap = if (pakaiGemini) {
        settings.geminiKey.isNotBlank() && settings.geminiModel.isNotBlank()
    } else {
        settings.ocKey.isNotBlank() && settings.ocModel.isNotBlank()
    }
    val chatModels = models.filter { it.canChat && it.isPureChat }
    val siap = modelTest.values.count { it.ok }
    val sedangBatch = batchTest != null

    SectionLabel("OTAK — MESIN PIKIRAN AVI", palette)

    // ---- baris 1: KUNCI API (status jelas: TERPASANG / BELUM) ----
    SetRow(
        icon = Icons.Default.Key,
        title = "KUNCI API ${if (pakaiGemini) "GEMINI" else "OPENAI-COMPAT"}",
        sub = when {
            otakSiap -> "Terpasang dan siap — AVI bisa berpikir. Ketuk untuk mengganti kunci."
            kunciTerpasang -> "Kunci terpasang, tapi MODEL belum dipilih — buka baris MODEL KECERDASAN."
            else -> "BELUM DIPASANG — tanpa kunci, AVI belum bisa menjawab. Ketuk untuk memasang."
        },
        palette = palette,
        iconGold = true,
        subColor = if (otakSiap) null else if (kunciTerpasang) palette.gold else palette.danger,
        onClick = { bukaKunci = !bukaKunci },
        expanded = bukaKunci
    )
    ExpandArea(visible = bukaKunci, palette = palette) {
        Text(
            "Kunci API = kartu akses otak AVI. Pilih penyedia, tempel kuncinya, lalu muat daftar model.",
            color = palette.textDim,
            fontSize = 10.5.sp,
            lineHeight = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            HudChip("GEMINI — GRATIS", pakaiGemini, palette) {
                vm.updateSettings { it.copy(provider = AiProvider.GEMINI) }
            }
            HudChip("OPENAI-COMPAT", !pakaiGemini, palette) {
                vm.updateSettings { it.copy(provider = AiProvider.OPENAI_COMPAT) }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (pakaiGemini) {
            OutlinedTextField(
                value = settings.geminiKey,
                onValueChange = { v -> vm.updateSettings { it.copy(geminiKey = v) } },
                label = { Text("Tempel kunci API Gemini di sini", color = palette.textDim, fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showKey) "Sembunyikan kunci" else "Lihat kunci",
                            tint = palette.textDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = hudFieldColors(palette)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.fetchModels() },
                enabled = !modelsLoading && settings.geminiKey.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.bg
                )
            ) {
                if (modelsLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = palette.bg
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    if (chatModels.isEmpty()) "SIMPAN & MUAT DAFTAR MODEL"
                    else "SIMPAN & PERBARUI DAFTAR MODEL",
                    fontSize = 12.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold
                )
            }
            modelsError?.let { err ->
                Spacer(Modifier.height(6.dp))
                Text(err, color = palette.danger, fontSize = 11.sp, lineHeight = 14.sp)
            }
            pickNote?.let { catatan ->
                Spacer(Modifier.height(6.dp))
                Text(catatan, color = palette.accent, fontSize = 11.sp, lineHeight = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                        )
                    } catch (_: Exception) {
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = palette.accent
                )
                Spacer(Modifier.width(6.dp))
                Text("AMBIL KUNCI GRATIS DI AI STUDIO", fontSize = 11.sp, color = palette.accent, fontFamily = AviHud)
            }
        } else {
            OutlinedTextField(
                value = settings.ocBaseUrl,
                onValueChange = { v -> vm.updateSettings { it.copy(ocBaseUrl = v) } },
                label = { Text("Alamat server (Base URL)", color = palette.textDim, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = hudFieldColors(palette)
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = settings.ocKey,
                onValueChange = { v -> vm.updateSettings { it.copy(ocKey = v) } },
                label = { Text("Kunci API", color = palette.textDim, fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = palette.textDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = hudFieldColors(palette)
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = settings.ocModel,
                onValueChange = { v -> vm.updateSettings { it.copy(ocModel = v) } },
                label = { Text("Nama model (mis. llama-3.3-70b)", color = palette.textDim, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = hudFieldColors(palette)
            )
        }
        Spacer(Modifier.height(10.dp))
        HudDivider(palette)
        Spacer(Modifier.height(4.dp))
        InfoRow(
            "Status otak",
            if (otakSiap) "TERHUBUNG — SIAP" else "BELUM SIAP",
            valueColor = if (otakSiap) palette.accent else palette.danger,
            palette = palette
        )
        InfoRow("Pemakaian hari ini", "$usageToday permintaan", palette = palette)
        Spacer(Modifier.height(8.dp))
        // Diagnosa koneksi — jalur pintas saat kunci bermasalah
        DiagnosisCard(
            diagnosis = diagnosis,
            onRun = { vm.runDiagnosis() },
            palette = palette
        )
    }
    Spacer(Modifier.height(8.dp))

    // ---- baris 2: MODEL KECERDASAN (daftar radio ketuk-pilih) ----
    SetRow(
        icon = Icons.Default.Memory,
        title = "MODEL KECERDASAN",
        sub = when {
            !pakaiGemini -> settings.ocModel.ifBlank { "Model kompatibel OpenAI belum diisi" }
            modelAktif.isBlank() -> "Belum dipilih — muat daftar model dulu di baris KUNCI API"
            else -> "$modelAktif · ketuk untuk mengganti otak AVI"
        },
        palette = palette,
        iconGold = true,
        onClick = { bukaModel = !bukaModel },
        expanded = bukaModel
    )
    ExpandArea(visible = bukaModel, palette = palette) {
        if (!pakaiGemini) {
            Text(
                "Penyedia OPENAI-COMPAT memakai nama model yang Anda tulis sendiri di baris KUNCI API.",
                color = palette.textDim,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        } else if (chatModels.isEmpty()) {
            Text(
                "Daftar model masih kosong. Buka baris KUNCI API di atas, tempel kunci, lalu tekan " +
                    "SIMPAN & MUAT DAFTAR MODEL — daftar otak langsung dari Google akan muncul di sini.",
                color = palette.textDim,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        } else {
            Text(
                "Ketuk satu model — langsung terpilih dan diuji sekali agar pasti bisa dipakai.",
                color = palette.textDim,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                chatModels.forEach { info ->
                    ModelRadioRow(
                        title = info.shortName,
                        desc = info.description,
                        selected = settings.geminiModel == info.name,
                        testOk = modelTest[info.name]?.ok,
                        palette = palette,
                        onClick = {
                            vm.updateSettings { it.copy(geminiModel = info.name) }
                            // Uji otomatis: buktikan model ini benar-benar bisa dipakai
                            vm.testModel(info.name)
                        }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { vm.testAllModels() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.accent)
            ) {
                if (sedangBatch) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(13.dp),
                        strokeWidth = 2.dp,
                        color = palette.accent
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "MENGUJI ${batchTest!!.first}/${batchTest!!.second} — MOHON TUNGGU",
                        fontSize = 11.sp, fontFamily = AviHud, fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "UJI SEMUA MODEL (${chatModels.size})",
                        fontSize = 11.sp, fontFamily = AviHud, fontWeight = FontWeight.Bold
                    )
                }
            }
            if (!sedangBatch && modelTest.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Hasil uji terakhir: $siap SIAP dari ${modelTest.size} model dicoba",
                    color = if (siap > 0) palette.accent else palette.danger,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

// ============================================================
// SEKSI 2 — SUARA: vokal jawaban, suara langsung, bahasa, cloud TTS
// ============================================================

@Composable
private fun SuaraSection(
    vm: AviViewModel,
    settings: AviSettings,
    palette: AviPalette,
    ttsReady: Boolean,
    tesDengarHasil: String?,
    onTesDengar: (String) -> Unit
) {
    val models by vm.models.collectAsState()
    val liveState by vm.state.collectAsState()
    val live = liveState == AviState.LIVE
    var ttsModelMenu by remember { mutableStateOf(false) }
    var voiceMenu by remember { mutableStateOf(false) }
    var bukaBahasa by remember { mutableStateOf(false) }
    val ttsModels = models.filter { it.canTts }

    SectionLabel("SUARA — BICARA & MENDENGAR", palette)

    // ---- VOKAL JAWABAN (saklar nyata) ----
    SetRow(
        icon = Icons.Default.VolumeUp,
        title = "VOKAL JAWABAN",
        sub = if (settings.ttsEnabled)
            "NYALA — AVI membacakan setiap balasannya dengan suara"
        else
            "MATI — jawaban hanya tampil di layar, AVI tidak bersuara",
        palette = palette,
        subColor = if (settings.ttsEnabled) palette.accent else palette.textDim,
        control = {
            HudToggle(
                on = settings.ttsEnabled,
                palette = palette
            ) { vm.updateSettings { it.copy(ttsEnabled = !settings.ttsEnabled) } }
        }
    )
    Spacer(Modifier.height(8.dp))

    // ---- SUARA LANGSUNG (LIVE STT MULAI/BERHENTI) ----
    SetRow(
        icon = Icons.Default.Podcasts,
        title = "SUARA LANGSUNG",
        sub = if (live)
            "LIVE — BERJALAN · AVI mendengar tanpa henti, bicara langsung saja"
        else
            "Mode dengar tanpa henti — AVI merespons begitu Anda bicara",
        palette = palette,
        iconGold = true,
        subColor = if (live) palette.danger else null,
        control = {
            MulaiBerhentiChip(
                live = live,
                palette = palette,
                onClick = { if (live) vm.stopLive() else vm.startLive() }
            )
        }
    )
    Spacer(Modifier.height(8.dp))

    // ---- PERCAKAPAN BERUNTUN ----
    SetRow(
        icon = Icons.Default.Autorenew,
        title = "PERCAKAPAN BERUNTUN",
        sub = if (settings.autoListen)
            "NYALA — setelah menjawab, AVI otomatis mendengar lagi tanpa disentuh"
        else
            "MATI — satu bicara satu jawaban; sentuh orb/mic untuk bicara lagi",
        palette = palette,
        control = {
            HudToggle(
                on = settings.autoListen,
                palette = palette
            ) { vm.updateSettings { it.copy(autoListen = !settings.autoListen) } }
        }
    )
    Spacer(Modifier.height(8.dp))

    // ---- BAHASA DENGARAN ----
    SetRow(
        icon = Icons.Default.Language,
        title = "BAHASA DENGARAN",
        sub = if (settings.sttLanguage == "id-ID")
            "INDONESIA — AVI mengerti perintah bahasa Indonesia"
        else
            "ENGLISH — AVI mengerti perintah bahasa Inggris",
        palette = palette,
        onClick = { bukaBahasa = !bukaBahasa },
        expanded = bukaBahasa
    )
    ExpandArea(visible = bukaBahasa, palette = palette) {
        Text(
            "Bahasa yang dipakai saat AVI MENDENGARKAN Anda — berlaku untuk obrolan, " +
                "suara langsung, maupun tes mendengar.",
            color = palette.textDim,
            fontSize = 10.5.sp,
            lineHeight = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("id-ID" to "INDONESIA", "en-US" to "ENGLISH").forEach { (kode, label) ->
                HudChip(label, settings.sttLanguage == kode, palette) {
                    vm.updateSettings { it.copy(sttLanguage = kode) }
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    // ---- SUARA NATURAL GOOGLE (opsional) ----
    SetRow(
        icon = Icons.Default.CloudQueue,
        title = "SUARA NATURAL GOOGLE",
        sub = if (settings.cloudTtsEnabled)
            "NYALA — vokal studio Google Cloud TTS (kualitas paling bagus)"
        else
            "MATI — memakai vokal bawaan Android; nyalakan untuk kualitas studio",
        palette = palette,
        iconGold = true,
        control = {
            HudToggle(
                on = settings.cloudTtsEnabled,
                palette = palette
            ) { vm.updateSettings { it.copy(cloudTtsEnabled = !settings.cloudTtsEnabled) } }
        }
    )
    if (settings.cloudTtsEnabled) {
        Spacer(Modifier.height(8.dp))
        ExpandArea(visible = true, palette = palette) {
            // pilih model TTS
            Box {
                OutlinedButton(
                    onClick = { ttsModelMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        ttsModels.firstOrNull { it.name == settings.cloudTtsModel }?.shortName?.uppercase()
                            ?: "PILIH MODEL TTS GOOGLE",
                        color = if (settings.cloudTtsModel.isBlank()) palette.textDim else palette.accent,
                        fontSize = 11.sp,
                        fontFamily = AviHud,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = palette.textDim)
                }
                DropdownMenu(expanded = ttsModelMenu, onDismissRequest = { ttsModelMenu = false }) {
                    if (ttsModels.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Tidak ada model TTS — muat daftar model dulu di seksi OTAK, pak.",
                                    color = palette.textDim,
                                    fontSize = 11.sp
                                )
                            },
                            onClick = { ttsModelMenu = false }
                        )
                    }
                    ttsModels.forEach { m ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    m.shortName,
                                    color = if (m.name == settings.cloudTtsModel) palette.accent else palette.text,
                                    fontSize = 12.sp,
                                    fontFamily = AviHud
                                )
                            },
                            onClick = {
                                vm.updateSettings { it.copy(cloudTtsModel = m.name) }
                                ttsModelMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            // pilih karakter suara
            Box {
                OutlinedButton(
                    onClick = { voiceMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (settings.cloudVoice.isBlank()) "PILIH KARAKTER SUARA"
                        else "SUARA: ${settings.cloudVoice.uppercase()} — ${
                            VOICE_DESC[settings.cloudVoice] ?: ""
                        }",
                        color = palette.accent,
                        fontSize = 11.sp,
                        fontFamily = AviHud,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = palette.textDim)
                }
                DropdownMenu(expanded = voiceMenu, onDismissRequest = { voiceMenu = false }) {
                    ModelCatalog.ttsVoices.forEach { v ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "$v — ${VOICE_DESC[v] ?: ""}",
                                    color = if (v == settings.cloudVoice) palette.accent else palette.text,
                                    fontSize = 12.sp,
                                    fontFamily = AviHud
                                )
                            },
                            onClick = {
                                vm.updateSettings { it.copy(cloudVoice = v) }
                                voiceMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.speakTest("Hay pak, semua sistem normal. AVI siap membantu.") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.gold,
                    contentColor = palette.bg
                )
            ) {
                Text(
                    "TES SUARA AVI — DENGARKAN SEKARANG",
                    fontSize = 12.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    // ---- TES MENDENGAR ----
    SetRow(
        icon = Icons.Default.Hearing,
        title = "TES MENDENGAR",
        sub = "Cek microphone AVI bisa menangkap suara Anda (tanpa kirim ke AI)",
        palette = palette,
        control = {
            MulaiBerhentiChip(live = false, labelMulai = "TES", palette = palette) {
                vm.listenOnce { result -> onTesDengar(result) }
            }
        }
    )
    tesDengarHasil?.let { res ->
        Spacer(Modifier.height(6.dp))
        Text(
            "AVI dengar: \u201C$res\u201D",
            color = palette.accent,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
    if (!ttsReady) {
        Spacer(Modifier.height(6.dp))
        Text(
            "Catatan: mesin suara Android belum siap — cek pengaturan TTS sistem bila vokal tidak bersuara.",
            color = palette.textDim.copy(alpha = 0.8f),
            fontSize = 9.5.sp,
            lineHeight = 13.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

// ============================================================
// SEKSI 3 — AGEN: mesin perintah nyata, izin, perkakas, log
// ============================================================

@Composable
private fun AgenSection(vm: AviViewModel, settings: AviSettings, palette: AviPalette) {
    val agentLog by vm.agentLog.collectAsState()
    val uji by vm.ujiPerkakasState.collectAsState()
    val registry = vm.toolRegistry
    var bukaMesin by remember { mutableStateOf(false) }
    var bukaIzin by remember { mutableStateOf(false) }
    var bukaPerkakas by remember { mutableStateOf(false) }
    var bukaUji by remember { mutableStateOf(false) }
    var bukaLog by remember { mutableStateOf(false) }

    val modeUraian = when (settings.agentMode) {
        "SELALU" -> "Semua perintah lewat mesin agen — AVI selalu menyusun langkah dulu."
        "MATI" -> "Asisten murni — mesin agen dimatikan, perilaku klasik."
        else -> "Perintah kompleks (multi-aksi) otomatis masuk mesin agen; obrolan biasa tetap di jalur asisten cepat."
    }
    val otorAktif = when (settings.agentAuthority) {
        "AMAN" -> Otoritas.AMAN
        "PENUH" -> Otoritas.PENUH
        else -> Otoritas.BAKU
    }
    val perkakasAktif = registry.daftarAktif(settings).size
    val logFmt = remember { SimpleDateFormat("dd MMM · HH:mm", Locale("id")) }

    SectionLabel("AGEN — AVI MENJALANKAN PERINTAH NYATA DI HP", palette)

    // ---- MESIN AGEN ----
    SetRow(
        icon = Icons.Default.SmartToy,
        title = "MESIN AGEN",
        sub = "Mode ${settings.agentMode} — $modeUraian",
        palette = palette,
        iconGold = true,
        onClick = { bukaMesin = !bukaMesin },
        expanded = bukaMesin
    )
    ExpandArea(visible = bukaMesin, palette = palette) {
        Text(
            "AVI bukan pengikut perintah pasif — dia menyusun langkah sendiri: berpikir, " +
                "menjalankan perkakas di HP, membaca hasil, lalu melapor. Perkakas gagal atau " +
                "model tak mendukung? Otomatis jatuh ke jalur asisten yang terbukti.",
            color = palette.textDim,
            fontSize = 10.5.sp,
            lineHeight = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("AUTO" to "OTOMATIS", "SELALU" to "SELALU", "MATI" to "MATI").forEach { (kode, label) ->
                HudChip(label, selected = settings.agentMode == kode, palette = palette) {
                    vm.updateSettings { it.copy(agentMode = kode) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            modeUraian,
            color = palette.textDim,
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(6.dp))
        InfoRow("Batas langkah agen", "${AgentEngine.MAKS_LANGKAH} langkah per perintah", palette = palette)
        InfoRow(
            "Perkakas aktif",
            "$perkakasAktif dari ${registry.semua.size} perkakas",
            valueColor = palette.gold,
            palette = palette
        )
    }
    Spacer(Modifier.height(8.dp))

    // ---- TINGKAT IZIN ----
    SetRow(
        icon = Icons.Default.Security,
        title = "TINGKAT IZIN AGEN",
        sub = "${settings.agentAuthority} — " + otorAktif.uraian,
        palette = palette,
        iconGold = true,
        onClick = { bukaIzin = !bukaIzin },
        expanded = bukaIzin
    )
    ExpandArea(visible = bukaIzin, palette = palette) {
        Text(
            "Menentukan perkakas apa saja yang boleh AVI pakai sendiri. Semakin tinggi, " +
                "semakin banyak perkakas terbuka.",
            color = palette.textDim,
            fontSize = 10.5.sp,
            lineHeight = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Otoritas.entries.forEach { o ->
                HudChip(o.label, selected = settings.agentAuthority == o.label, palette = palette) {
                    vm.updateSettings { it.copy(agentAuthority = o.label) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            otorAktif.uraian,
            color = palette.textDim,
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
            lineHeight = 13.sp
        )
        if (settings.agentAuthority == "PENUH") {
            Spacer(Modifier.height(6.dp))
            Text(
                "Telepon & SMS selalu lewat layar konfirmasi — AVI tidak pernah menelepon/mengirim diam-diam.",
                color = palette.gold,
                fontSize = 9.5.sp,
                lineHeight = 13.sp
            )
        }
    }
    Spacer(Modifier.height(8.dp))

    // ---- PERKAKAS AGEN ----
    SetRow(
        icon = Icons.Default.Handyman,
        title = "PERKAKAS AGEN",
        sub = "$perkakasAktif dari ${registry.semua.size} perkakas aktif — senter, buka aplikasi, volume, telepon, dan lainnya",
        palette = palette,
        iconGold = true,
        onClick = { bukaPerkakas = !bukaPerkakas },
        expanded = bukaPerkakas
    )
    ExpandArea(visible = bukaPerkakas, palette = palette) {
        registry.semua.forEach { tool ->
            val izin = otorAktif.mengizinkan(tool.level)
            val off = !izin || tool.name in settings.agentToolsOff
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tool.label,
                            color = if (off) palette.textDim else palette.text,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            tool.level.label,
                            color = when (tool.level) {
                                Otoritas.AMAN -> palette.accent
                                Otoritas.BAKU -> palette.gold
                                Otoritas.PENUH -> palette.danger
                            },
                            fontSize = 7.5.sp,
                            fontFamily = AviHud,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Text(
                        tool.description,
                        color = palette.textDim.copy(alpha = if (off) 0.55f else 1f),
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                HudToggle(
                    on = !off,
                    enabled = izin,
                    palette = palette
                ) {
                    if (izin) vm.setToolOff(tool.name, tool.name !in settings.agentToolsOff)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Perkakas di atas tingkat izin otomatis terkunci. Matikan perkakas yang tidak Anda mau AVI pakai.",
            color = palette.textDim,
            fontSize = 9.sp,
            fontStyle = FontStyle.Italic
        )
    }
    Spacer(Modifier.height(8.dp))

    // ---- UJI PERKAKAS ----
    SetRow(
        icon = Icons.Default.Troubleshoot,
        title = "UJI PERKAKAS",
        sub = "Jalankan perkakas aman langsung di HP (tanpa AI) — bukti perkakas bekerja",
        palette = palette,
        onClick = { bukaUji = !bukaUji },
        expanded = bukaUji
    )
    ExpandArea(visible = bukaUji, palette = palette) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            registry.semua.filter { it.level == Otoritas.AMAN }.forEach { tool ->
                HudChip(tool.label, selected = uji?.first == tool.name, palette = palette) {
                    vm.ujiPerkakas(tool.name)
                }
            }
        }
        uji?.let { (nama, hasil) ->
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (hasil.ok) "✓" else "✗",
                    color = if (hasil.ok) palette.accent else palette.danger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "${registry.byName(nama)?.label ?: nama} — ${hasil.pesan}",
                    color = if (hasil.ok) palette.text else palette.danger,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    // ---- LOG AKSI AGEN ----
    SetRow(
        icon = Icons.Default.History,
        title = "LOG AKSI AGEN",
        sub = if (agentLog.isEmpty())
            "Belum ada aksi tercatat — beri perintah berantai, mis. \u201Ccek baterai lalu atur volume 40 persen\u201D"
        else
            "${agentLog.size} aksi tercatat — setiap langkah agen tercatat di sini",
        palette = palette,
        onClick = { bukaLog = !bukaLog },
        expanded = bukaLog
    )
    ExpandArea(visible = bukaLog, palette = palette) {
        if (agentLog.isEmpty()) {
            Text(
                "Log akan terisi otomatis setiap kali mesin agen menjalankan perkakas.",
                color = palette.textDim,
                fontSize = 10.sp,
                fontStyle = FontStyle.Italic
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Terbaru di atas — ${agentLog.size} aksi",
                    color = palette.textDim,
                    fontSize = 10.sp,
                    fontFamily = AviHud,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { vm.clearAgentLog() }) {
                    Text("HAPUS LOG", color = palette.danger, fontSize = 10.sp, fontFamily = AviHud)
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                agentLog.asReversed().forEach { item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            if (item.ok) "✓" else "✗",
                            color = if (item.ok) palette.accent else palette.danger,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Row {
                                Text(
                                    item.label,
                                    color = palette.text, fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    logFmt.format(Date(item.waktu)),
                                    color = palette.textDim, fontSize = 9.sp, fontFamily = AviHud
                                )
                            }
                            if (item.argumen.isNotBlank()) {
                                Text(item.argumen, color = palette.textDim, fontSize = 9.5.sp, fontFamily = AviHud)
                            }
                            Text(
                                item.pesan,
                                color = if (item.ok) palette.textDim else palette.danger.copy(alpha = 0.85f),
                                fontSize = 9.5.sp, lineHeight = 12.sp,
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                }
            }
        }
    }
}

// ============================================================
// SEKSI 4 — GAYA: panggilan, gaya bicara, ingatan, protokol
// ============================================================

@Composable
private fun GayaSection(vm: AviViewModel, settings: AviSettings, palette: AviPalette) {
    val memories by vm.memories.collectAsState()
    val protocols by vm.protocols.collectAsState()
    var inputMem by remember { mutableStateOf("") }
    var protoName by remember { mutableStateOf("") }
    var protoPhrase by remember { mutableStateOf("") }
    var protoSteps by remember { mutableStateOf("") }
    var inputPanggilan by remember { mutableStateOf<String?>(null) } // null = ikut settings
    var bukaPanggilan by remember { mutableStateOf(false) }
    var bukaGaya by remember { mutableStateOf(false) }
    var bukaIngatan by remember { mutableStateOf(false) }
    var bukaProtokol by remember { mutableStateOf(false) }

    val panggilanAktif = inputPanggilan ?: settings.userName
    fun setPanggilan(v: String) {
        inputPanggilan = v
        val bersih = v.trim()
        vm.updateSettings { it.copy(userName = bersih.ifBlank { "Pak" }) }
    }
    val tglIngatan = remember { SimpleDateFormat("d MMM yyyy · HH:mm", Locale("id", "ID")) }

    SectionLabel("GAYA — KEPRIBADIAN AVI", palette)

    // ---- PANGGILAN SAYA ----
    SetRow(
        icon = Icons.Default.Person,
        title = "PANGGILAN SAYA",
        sub = "AVI memanggil Anda \u201C${settings.userName.ifBlank { "Pak" }}\u201D — ketuk untuk mengganti",
        palette = palette,
        onClick = { bukaPanggilan = !bukaPanggilan },
        expanded = bukaPanggilan
    )
    ExpandArea(visible = bukaPanggilan, palette = palette) {
        OutlinedTextField(
            value = panggilanAktif,
            onValueChange = { v -> setPanggilan(v) },
            label = { Text("AVI memanggil saya sebagai…", color = palette.textDim, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = hudFieldColors(palette)
        )
        Spacer(Modifier.height(7.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            listOf("Pak", "Mas", "Bang", "Kak", "Om", "Cak").forEach { n ->
                HudChip(n, settings.userName == n, palette) { setPanggilan(n) }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            "Contoh: Pak, Mas, Bang, Kak — atau nama Anda sendiri. AVI memakai panggilan ini " +
                "di sapaan dan seluruh jawabannya.",
            color = palette.textDim.copy(alpha = 0.75f),
            fontSize = 10.sp,
            fontFamily = AviHud,
            lineHeight = 13.sp
        )
    }
    Spacer(Modifier.height(8.dp))

    // ---- GAYA BICARA ----
    SetRow(
        icon = Icons.Default.RecordVoiceOver,
        title = "GAYA BICARA",
        sub = when (settings.style) {
            "sopan" -> "SOPAN — bahasa formal penuh hormat, layaknya butler eksekutif"
            "jedai" -> "JEDAI — AI markas sci-fi: tenang, presisi, sesekali melapor status"
            else -> "SANTAI — hangat tapi sopan, sesekali bercanda ringan"
        },
        palette = palette,
        onClick = { bukaGaya = !bukaGaya },
        expanded = bukaGaya
    )
    ExpandArea(visible = bukaGaya, palette = palette) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("sopan" to "SOPAN", "santai" to "SANTAI", "jedai" to "JEDAI").forEach { (value, label) ->
                HudChip(label, settings.style == value, palette) {
                    vm.updateSettings { it.copy(style = value) }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Gaya ini dipakai AVI di seluruh jawaban lisan dan tulisannya.",
            color = palette.textDim,
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic
        )
    }
    Spacer(Modifier.height(8.dp))

    // ---- INGATAN AVI ----
    SetRow(
        icon = Icons.Default.Psychology,
        title = "INGATAN AVI",
        sub = if (memories.isEmpty())
            "Kosong — tulis fakta tentang Anda dan AVI akan mengingatnya permanen"
        else
            "${memories.size} ingatan tersimpan — otomatis dibawa ke setiap obrolan",
        palette = palette,
        onClick = { bukaIngatan = !bukaIngatan },
        expanded = bukaIngatan
    )
    ExpandArea(visible = bukaIngatan, palette = palette) {
        if (memories.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                memories.forEach { mem ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.panelLight.copy(alpha = 0.45f))
                            .border(1.dp, palette.accent.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                mem.text,
                                color = palette.text,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Text(
                                "tercatat " + tglIngatan.format(Date(mem.createdAt)),
                                color = palette.textDim.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                fontFamily = AviHud
                            )
                        }
                        IconButton(onClick = { vm.deleteMemory(mem.id) }, modifier = Modifier.size(30.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus ingatan",
                                tint = palette.danger,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${memories.size} ingatan tersimpan",
                    color = palette.textDim,
                    fontSize = 10.sp,
                    fontFamily = AviHud,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { vm.clearMemories() }) {
                    Text(
                        "HAPUS SEMUA",
                        color = palette.danger,
                        fontSize = 10.sp,
                        fontFamily = AviHud,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                "Belum ada ingatan. Coba tulis: \u201CSaya suka kopi hitam\u201D — AVI akan " +
                    "mengingatnya permanen, pak.",
                color = palette.textDim,
                fontSize = 11.sp,
                fontFamily = AviHud,
                lineHeight = 14.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputMem,
                onValueChange = { inputMem = it },
                placeholder = { Text("Contoh: Saya suka kopi hitam", color = palette.textDim, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                maxLines = 2,
                shape = RoundedCornerShape(10.dp),
                colors = hudFieldColors(palette)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    vm.addMemory(inputMem)
                    inputMem = ""
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.gold,
                    contentColor = palette.bg
                )
            ) {
                Text(
                    "INGAT",
                    fontSize = 11.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    // ---- PROTOKOL PERINTAH ----
    SetRow(
        icon = Icons.Default.AutoAwesome,
        title = "PROTOKOL PERINTAH",
        sub = if (protocols.isEmpty())
            "Kosong — rangkai beberapa perintah jadi satu frasa ajaib, mis. \u201Cprotokol malam\u201D"
        else
            "${protocols.size} protokol tersimpan — jalankan lewat satu frasa",
        palette = palette,
        onClick = { bukaProtokol = !bukaProtokol },
        expanded = bukaProtokol
    )
    ExpandArea(visible = bukaProtokol, palette = palette) {
        protocols.forEach { p ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.panelLight.copy(alpha = 0.5f))
                    .border(1.dp, palette.accent.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.name, color = palette.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Frasa: \u201C${p.phrase}\u201D",
                            color = palette.accent,
                            fontSize = 10.sp,
                            fontFamily = AviHud
                        )
                        Text(
                            "${p.steps.size} langkah",
                            color = palette.textDim,
                            fontSize = 10.sp,
                            fontFamily = AviHud
                        )
                    }
                    TextButton(onClick = { vm.runProtocol(p) }) {
                        Text(
                            "JALANKAN",
                            color = palette.accent,
                            fontSize = 10.sp,
                            fontFamily = AviHud,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { vm.deleteProtocol(p.id) }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus protokol",
                            tint = palette.danger,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        Text(
            "Satu protokol = satu frasa pemicu yang menjalankan banyak perintah berurutan.",
            color = palette.textDim,
            fontSize = 10.sp,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = protoName,
            onValueChange = { protoName = it },
            label = { Text("Nama protokol (mis. Protokol Malam)", color = palette.textDim, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = hudFieldColors(palette)
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = protoPhrase,
            onValueChange = { protoPhrase = it },
            label = { Text("Frasa pemicu (mis. protokol malam)", color = palette.textDim, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = hudFieldColors(palette)
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = protoSteps,
            onValueChange = { protoSteps = it },
            label = { Text("Langkah — satu perintah per baris", color = palette.textDim, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
            shape = RoundedCornerShape(10.dp),
            colors = hudFieldColors(palette)
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val steps = protoSteps.lines().map { it.trim() }.filter { it.isNotEmpty() }
                val name = protoName.trim()
                val phrase = protoPhrase.trim()
                if (name.isNotEmpty() && phrase.isNotEmpty() && steps.isNotEmpty()) {
                    vm.saveProtocol(
                        Protocol(id = UUID.randomUUID().toString(), name = name, phrase = phrase, steps = steps)
                    )
                    protoName = ""
                    protoPhrase = ""
                    protoSteps = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.accent,
                contentColor = palette.bg
            )
        ) {
            Text(
                "SIMPAN PROTOKOL",
                fontSize = 12.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================
// SEKSI 5 — MARKAS: diagnostik nyata, data & privasi, tentang
// ============================================================

@Composable
private fun MarkasSection(
    vm: AviViewModel,
    settings: AviSettings,
    palette: AviPalette,
    versiApp: String
) {
    val context = LocalContext.current
    val diag by vm.diagnostics.collectAsState()
    val memories by vm.memories.collectAsState()
    val protocols by vm.protocols.collectAsState()
    val messages by vm.messages.collectAsState()
    var bukaData by remember { mutableStateOf(false) }
    var bukaTentang by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.refreshDiagnostics() }

    SectionLabel("MARKAS — KONDISI PERANGKAT ANDA", palette)

    // ---- 3 chip diagnostik nyata ----
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MarkasChip(
            label = "BATERAI",
            value = diag?.let { "${it.batteryPct}%${if (it.charging) "⚡" else ""}" } ?: "—",
            palette = palette,
            modifier = Modifier.weight(1f)
        )
        MarkasChip(
            label = "SIMPAN",
            value = diag?.let {
                String.format(Locale.US, "%.0f/%.0f GB", it.storageFreeGb, it.storageTotalGb)
            } ?: "—",
            palette = palette,
            modifier = Modifier.weight(1f)
        )
        MarkasChip(
            label = "INTERNET",
            value = diag?.network ?: "—",
            palette = palette,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(8.dp))

    // ---- DATA & PRIVASI ----
    SetRow(
        icon = Icons.Default.DeleteSweep,
        title = "DATA & PRIVASI",
        sub = "${messages.size} pesan · ${memories.size} ingatan · ${protocols.size} protokol — semua tersimpan LOKAL di HP ini, tanpa server",
        palette = palette,
        onClick = { bukaData = !bukaData },
        expanded = bukaData
    )
    ExpandArea(visible = bukaData, palette = palette) {
        Text(
            "Seluruh data AVI hanya tersimpan di HP Anda — tanpa server, tanpa pelacakan. " +
                "Hapus jika ingin mulai bersih:",
            color = palette.textDim,
            fontSize = 10.5.sp,
            lineHeight = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton(
                icon = Icons.Default.DeleteSweep,
                label = "HAPUS RIWAYAT CHAT",
                danger = true,
                onClick = { vm.clearHistory() },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.DeleteForever,
                label = "HAPUS INGATAN",
                danger = true,
                onClick = { vm.clearMemories() },
                modifier = Modifier.weight(1f)
            )
        }
    }
    Spacer(Modifier.height(8.dp))

    // ---- TENTANG AVI ----
    SetRow(
        icon = Icons.Default.Info,
        title = "TENTANG AVI",
        sub = "Versi $versiApp · ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} · Android ${android.os.Build.VERSION.RELEASE}",
        palette = palette,
        onClick = { bukaTentang = !bukaTentang },
        expanded = bukaTentang
    )
    ExpandArea(visible = bukaTentang, palette = palette) {
        InfoRow("Versi AVI", "v$versiApp", palette = palette)
        InfoRow(
            "Perangkat",
            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            palette = palette
        )
        InfoRow(
            "Android",
            "API ${android.os.Build.VERSION.SDK_INT} (${android.os.Build.VERSION.RELEASE})",
            palette = palette
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/anztdree/AVI"))
                    )
                } catch (_: Exception) {
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Code,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = palette.accent
            )
            Spacer(Modifier.width(6.dp))
            Text("REPO GITHUB AVI", fontSize = 11.sp, color = palette.accent, fontFamily = AviHud)
        }
    }
}

// ============================================================
// WIDGET WARISAN YANG MASIH DIPAKAI
// ============================================================

/**
 * P44 — chip diagnostik MARKAS (BATERAI · SIMPAN · NET): label kecil redup di
 * atas, nilai sian tebal di bawah — satu keluarga dengan .setinfo preview
 * Pengaturan. Non-interaktif, nilai dari DeviceActions.readDiagnostics nyata.
 */
@Composable
private fun MarkasChip(
    label: String,
    value: String,
    palette: AviPalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.panel.copy(alpha = 0.55f))
            .border(1.dp, palette.panelLight.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            color = palette.textDim,
            fontSize = 7.5.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = palette.accent,
            fontSize = 11.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * K11 — Kartu DIAGNOSA KONEKSI: membongkar akar masalah koneksi ke Google dalam
 * hitungan detik dan menampilkan JALAN KELUAR yang bisa dijalankan (tautan aktif),
 * bukan sekadar pesan gagal. Tampil menyatu dengan gaya Pusat Kendali.
 */
@Composable
private fun DiagnosisCard(
    diagnosis: DiagnoseResult?,
    onRun: () -> Unit,
    palette: AviPalette
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.panel.copy(alpha = 0.55f))
            .border(
                1.dp,
                when {
                    diagnosis == null -> palette.panelLight
                    diagnosis.running -> palette.accent.copy(alpha = 0.6f)
                    diagnosis.verdictOk -> palette.accent
                    else -> palette.danger.copy(alpha = 0.7f)
                },
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Troubleshoot,
                contentDescription = null,
                tint = palette.gold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "DIAGNOSA KONEKSI",
                color = palette.text,
                fontSize = 12.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Ketahuan akar masalahnya dalam ±15 detik — kunci, daftar model, hak pakai.",
            color = palette.textDim,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onRun,
            enabled = diagnosis?.running != true,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.gold,
                contentColor = palette.bg
            )
        ) {
            if (diagnosis?.running == true) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = palette.bg
                )
                Spacer(Modifier.width(8.dp))
                Text("MEMERIKSA…", fontSize = 12.sp, fontFamily = AviHud, fontWeight = FontWeight.Bold)
            } else {
                Text("JALANKAN DIAGNOSA", fontSize = 12.sp, fontFamily = AviHud, fontWeight = FontWeight.Bold)
            }
        }

        diagnosis?.takeIf { !it.running && it.steps.isNotEmpty() }?.let { d ->
            Spacer(Modifier.height(10.dp))
            // ---- langkah-langkah ----
            d.steps.forEach { step ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Icon(
                        if (step.ok) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = if (step.ok) "berhasil" else "gagal",
                        tint = if (step.ok) palette.accent else palette.danger,
                        modifier = Modifier.size(13.dp).padding(top = 1.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text(
                            step.label,
                            color = palette.text,
                            fontSize = 10.sp,
                            fontFamily = AviHud,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            step.detail,
                            color = if (step.ok) palette.textDim else palette.danger,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
            // ---- vonis ----
            Spacer(Modifier.height(8.dp))
            Text(
                d.verdict,
                color = if (d.verdictOk) palette.accent else palette.danger,
                fontSize = 11.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                lineHeight = 15.sp
            )
            // ---- panduan perbaikan (tautan bisa ditekan) ----
            if (d.fixGuide.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                d.fixGuide.forEachIndexed { i, line ->
                    val url = Regex("https://[^\\s,)]+").find(line)?.value
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.bg.copy(alpha = 0.45f))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            line,
                            color = palette.textDim,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                        url?.let {
                            Spacer(Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                                    } catch (_: Exception) {
                                    }
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 12.dp, vertical = 4.dp
                                ),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = palette.accent
                                )
                                Spacer(Modifier.width(5.dp))
                                Text("BUKA TAUTAN", fontSize = 9.sp, color = palette.accent, fontFamily = AviHud)
                            }
                        }
                    }
                    if (i < d.fixGuide.lastIndex) Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun hudFieldColors(palette: AviPalette) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = palette.accent,
    unfocusedBorderColor = palette.panelLight,
    cursorColor = palette.accent,
    focusedTextColor = palette.text,
    unfocusedTextColor = palette.text
)

private val VOICE_DESC = mapOf(
    "Kore" to "Tegas & netral",
    "Puck" to "Ceria & berapi-api",
    "Charon" to "Dalam & tenang",
    "Fenrir" to "Kuat & bergemuruh",
    "Leda" to "Muda & jernih",
    "Orus" to "Kokoh & mantap",
    "Zephyr" to "Terang & sepoi",
    "Aoede" to "Lembut & mengalir"
)
