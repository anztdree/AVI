package com.anztdree.avi.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.Color
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
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.R
import com.anztdree.avi.ai.DiagnoseResult
import com.anztdree.avi.ai.ModelCatalog
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.Protocol
import com.anztdree.avi.ui.theme.AviCode
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
 * K20 — PUSAT KENDALI ROMBAKAN TOTAL (kelas Jarvis):
 *  · Header sinematik: inti plasma hidup + jam markas WIB + strip status 4 petak
 *  · Navigasi 5 tab misi (OTAK · SUARA · GAYA · HUD · SISTEM) dgn indikator meluncur
 *  · Setiap tab = panel misi lengkap; seluruh fungsi lama dipertahankan
 *  · Fitur kaya: kartu inti otak, preset vokal, aksi cepat 6 petak,
 *    ring kuota API, modul antrean SEGERA, kartu tema Mark 2x2
 * Satu keluarga visual dgn layar obrolan: nebula per-Mark + hex grid + bingkai HUD.
 */
@Composable
fun SettingsScreen(vm: AviViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    // Live preview tema — mark diambil langsung dari settings
    AviTheme(mark = settings.themeMark) {
        SettingsScreenContent(vm = vm, settings = settings, onBack = onBack)
    }
}

@Composable
private fun SettingsScreenContent(vm: AviViewModel, settings: AviSettings, onBack: () -> Unit) {
    val palette = LocalAviPalette.current
    val ttsReady by vm.ttsReady.collectAsState()
    val context = LocalContext.current

    // ===== state lintas-tab (disimpan di sini agar tidak hilang saat pindah tab) =====
    var tab by remember { mutableStateOf(HudTab.OTAK) }
    var inputPanggilan by remember { mutableStateOf<String?>(null) } // null = ikut settings
    var testResult by remember { mutableStateOf<String?>(null) }

    // K18 — versi build ditampilkan dinamis (ikut build, bukan teks manual)
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

    val brainActive = if (settings.provider == AiProvider.GEMINI) {
        settings.geminiKey.isNotBlank() && settings.geminiModel.isNotBlank()
    } else {
        settings.ocKey.isNotBlank() && settings.ocModel.isNotBlank()
    }
    val panggilanAktif = settings.userName.ifBlank { "Pak" }

    fun setPanggilan(v: String) {
        inputPanggilan = v
        val bersih = v.trim()
        vm.updateSettings { it.copy(userName = bersih.ifBlank { "Pak" }) }
    }

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
            // ============ HEADER SINEMATIK ============
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = palette.text
                    )
                }
                CoreBadge(palette = palette)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "PUSAT KENDALI",
                        color = palette.text,
                        fontSize = 16.sp,
                        fontFamily = AviDisplay,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "MARKAS AVI · v$versiApp · $clock WIB",
                        color = palette.textDim,
                        fontSize = 9.sp,
                        fontFamily = AviHud,
                        letterSpacing = 0.8.sp,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PANGGILAN", color = palette.textDim, fontSize = 7.5.sp, fontFamily = AviHud, letterSpacing = 1.sp)
                    Text(
                        panggilanAktif,
                        color = palette.gold,
                        fontSize = 13.sp,
                        fontFamily = AviHud,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            // ============ STRIP STATUS HIDUP ============
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                StatTile(
                    value = if (brainActive) "SIAP" else "OFF",
                    label = "OTAK",
                    active = brainActive,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = if (ttsReady) "HIDUP" else "SENYAP",
                    label = "SUARA",
                    active = ttsReady,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = panggilanAktif,
                    label = "PEMILIK",
                    active = true,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = tab.name,
                    label = "TAB MISI",
                    active = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // ============ NAVIGASI TAB MISI ============
            Box(Modifier.padding(horizontal = 16.dp)) {
                HudTabBar(
                    selected = tab,
                    onSelect = { tab = it },
                    hapticsEnabled = settings.hapticsEnabled
                )
            }
            Spacer(Modifier.height(10.dp))

            // ============ KONTEN TAB (transisi hologram) ============
            AnimatedContent(
                targetState = tab,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val dir = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                    (slideInHorizontally(tween(280)) { dir * it / 6 } + fadeIn(tween(280))) togetherWith
                        (slideOutHorizontally(tween(280)) { -dir * it / 6 } + fadeOut(tween(280)))
                },
                label = "tabKendali"
            ) { t ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    when (t) {
                        HudTab.OTAK -> OtakTab(
                            vm = vm,
                            settings = settings,
                            palette = palette,
                            onOpenAiStudio = {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        )
                        HudTab.SUARA -> SuaraTab(
                            vm = vm,
                            settings = settings,
                            palette = palette,
                            testResult = testResult,
                            onTestResult = { testResult = it }
                        )
                        HudTab.GAYA -> GayaTab(
                            vm = vm,
                            settings = settings,
                            palette = palette,
                            panggilan = inputPanggilan,
                            onPanggilanChange = { v -> setPanggilan(v) }
                        )
                        HudTab.HUD -> TampilanTab(vm = vm, settings = settings, palette = palette)
                        HudTab.SISTEM -> SistemTab(
                            vm = vm,
                            settings = settings,
                            palette = palette,
                            onGoTab = { tab = it },
                            onTestResult = { testResult = it },
                            onOpenAiStudio = {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "AVI — Asisten Virtual Indonesia · Semua data tersimpan lokal di HP Anda.",
                        color = palette.textDim,
                        fontSize = 9.sp,
                        fontFamily = AviHud,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// TAB 1 — OTAK: inti otak, provider & kunci, model, diagnosa
// ============================================================

@Composable
private fun OtakTab(
    vm: AviViewModel,
    settings: AviSettings,
    palette: AviPalette,
    onOpenAiStudio: () -> Unit
) {
    val models by vm.models.collectAsState()
    val modelsLoading by vm.modelsLoading.collectAsState()
    val modelsError by vm.modelsError.collectAsState()
    val modelTest by vm.modelTest.collectAsState()
    val batchTest by vm.batchTest.collectAsState()
    val pickNote by vm.pickNote.collectAsState()
    val diagnosis by vm.diagnosis.collectAsState()
    val usageToday by vm.usageToday.collectAsState()
    var showKey by remember { mutableStateOf(false) }

    val brainActive = if (settings.provider == AiProvider.GEMINI) {
        settings.geminiKey.isNotBlank() && settings.geminiModel.isNotBlank()
    } else {
        settings.ocKey.isNotBlank() && settings.ocModel.isNotBlank()
    }
    val chatModels = models.filter { it.canChat && it.isPureChat }
    val siap = modelTest.values.count { it.ok }
    val modelAktif = settings.geminiModel.removePrefix("models/")

    // ---- kartu inti otak ----
    HoloPanel(palette = palette) {
        PanelTitle("INTI OTAK", if (brainActive) "AKTIF" else "BELUM SIAP", palette)
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                settings.provider != AiProvider.GEMINI ->
                    settings.ocModel.ifBlank { "Model belum diisi." }
                modelAktif.isBlank() -> "Model belum dipilih — muat daftar model."
                else -> modelAktif
            },
            color = if (brainActive) palette.accent else palette.danger,
            fontSize = 13.sp,
            fontFamily = AviCode,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        HudDivider(palette)
        InfoRow(
            "Penyedia otak",
            if (settings.provider == AiProvider.GEMINI) "Google Gemini" else "Kompatibel OpenAI",
            palette = palette
        )
        InfoRow("Pemakaian hari ini", "$usageToday permintaan", palette = palette)
        InfoRow(
            "Model siap (uji terakhir)",
            if (modelTest.isEmpty()) "belum diuji" else "$siap dari ${modelTest.size}",
            valueColor = if (siap > 0) palette.accent else null,
            palette = palette
        )
    }
    Spacer(Modifier.height(12.dp))

    // ---- provider & kunci ----
    SystemHeader(name = "OTAK", active = brainActive, palette = palette)
    HoloPanel(palette = palette) {
        Text(
            "PROVIDER OTAK",
            color = palette.textDim,
            fontSize = 10.sp,
            fontFamily = AviHud,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        AiProvider.entries.forEach { p ->
            val selected = settings.provider == p
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) palette.accent.copy(alpha = 0.10f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (selected) palette.accent.copy(alpha = 0.6f) else palette.panelLight,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { vm.updateSettings { it.copy(provider = p) } }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .border(2.dp, if (selected) palette.accent else palette.textDim, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Box(Modifier.size(7.dp).clip(CircleShape).background(palette.accent))
                }
                Spacer(Modifier.width(8.dp))
                Text(p.label, color = palette.text, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 2)
            }
            Spacer(Modifier.height(6.dp))
        }

        if (settings.provider == AiProvider.GEMINI) {
            OutlinedTextField(
                value = settings.geminiKey,
                onValueChange = { v -> vm.updateSettings { it.copy(geminiKey = v) } },
                label = { Text("API Key Gemini", color = palette.textDim, fontSize = 12.sp) },
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
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.fetchModels() },
                enabled = !modelsLoading,
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
                    "MUAT DAFTAR MODEL",
                    fontSize = 12.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold
                )
            }
            modelsError?.let { err ->
                Spacer(Modifier.height(4.dp))
                Text(err, color = palette.danger, fontSize = 11.sp, lineHeight = 14.sp)
            }
            pickNote?.let { catatan ->
                Spacer(Modifier.height(4.dp))
                Text(catatan, color = palette.accent, fontSize = 11.sp, lineHeight = 14.sp)
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = onOpenAiStudio,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = palette.accent
                )
                Spacer(Modifier.width(6.dp))
                Text("Buka AI Studio", fontSize = 11.sp, color = palette.accent)
            }
        } else {
            OutlinedTextField(
                value = settings.ocBaseUrl,
                onValueChange = { v -> vm.updateSettings { it.copy(ocBaseUrl = v) } },
                label = { Text("Base URL", color = palette.textDim, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = hudFieldColors(palette)
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = settings.ocKey,
                onValueChange = { v -> vm.updateSettings { it.copy(ocKey = v) } },
                label = { Text("API Key", color = palette.textDim, fontSize = 12.sp) },
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
                label = { Text("Nama Model", color = palette.textDim, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = hudFieldColors(palette)
            )
        }
    }
    Spacer(Modifier.height(12.dp))

    // ---- model gemini ----
    if (settings.provider == AiProvider.GEMINI) {
        SystemHeader(name = "MODEL", active = settings.geminiModel.isNotBlank(), palette = palette)
        HoloPanel(palette = palette) {
            if (chatModels.isEmpty()) {
                Text(
                    "Tekan MUAT DAFTAR MODEL untuk menarik daftar model terbaru langsung dari Google.",
                    color = palette.textDim,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.bg.copy(alpha = 0.5f))
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chatModels.forEach { info ->
                        ModelCard(
                            info = info,
                            selected = settings.geminiModel == info.name,
                            onSelect = {
                                vm.updateSettings { it.copy(geminiModel = info.name) }
                                // Uji otomatis: buktikan model ini benar-benar bisa dipakai
                                vm.testModel(info.name)
                            },
                            test = modelTest[info.name],
                            palette = palette
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            // UJI SEMUA MODEL — semua generasi, bukan cuma 2.5 (K14)
            val sedangBatch = batchTest != null
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
                        "MENGUJI ${batchTest!!.first}/${batchTest!!.second} — SENTUH UTK BATAL",
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
                Spacer(Modifier.height(4.dp))
                Text(
                    "Hasil uji terakhir: $siap SIAP dari ${modelTest.size} model dicoba",
                    color = if (siap > 0) palette.accent else palette.danger,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }

    // ---- diagnosa ----
    SystemHeader(name = "DIAGNOSA", active = diagnosis?.verdictOk == true, palette = palette)
    DiagnosisCard(
        diagnosis = diagnosis,
        onRun = { vm.runDiagnosis() },
        palette = palette
    )
}

// ============================================================
// TAB 2 — SUARA: percakapan, vokal + preset, cloud TTS, tes
// ============================================================

@Composable
private fun SuaraTab(
    vm: AviViewModel,
    settings: AviSettings,
    palette: AviPalette,
    testResult: String?,
    onTestResult: (String) -> Unit
) {
    val models by vm.models.collectAsState()
    val ttsReady by vm.ttsReady.collectAsState()
    var ttsModelMenu by remember { mutableStateOf(false) }
    var voiceMenu by remember { mutableStateOf(false) }
    val ttsModels = models.filter { it.canTts }

    SystemHeader(name = "SUARA", active = ttsReady, palette = palette)
    HoloPanel(palette = palette) {
        Text(
            "AVI selalu membacakan setiap jawaban — wajib, tanpa tombol",
            color = palette.textDim,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
        Spacer(Modifier.height(4.dp))
        ReactorToggle(
            label = "Mode percakapan beruntun",
            checked = settings.autoListen,
            onChange = { v -> vm.updateSettings { it.copy(autoListen = v) } },
            palette = palette
        )
        HudDivider(palette)

        PanelTitle("PENGATURAN VOKAL", null, palette)
        PowerDial(
            label = "Kecepatan bicara",
            value = settings.speechRate,
            valueRange = 0.5f..2.0f,
            display = String.format(Locale.US, "%.1f x", settings.speechRate),
            onChange = { v -> vm.updateSettings { it.copy(speechRate = v) } },
            palette = palette
        )
        PowerDial(
            label = "Nada bicara",
            value = settings.pitch,
            valueRange = 0.5f..2.0f,
            display = String.format(Locale.US, "%.1f x", settings.pitch),
            onChange = { v -> vm.updateSettings { it.copy(pitch = v) } },
            palette = palette
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "PRESET CEPAT",
            color = palette.textDim,
            fontSize = 9.sp,
            fontFamily = AviHud,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(5.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "LEMBUT" to (0.9f to 1.05f),
                "NORMAL" to (1.0f to 1.0f),
                "CEPAT" to (1.3f to 1.1f),
                "DALAM" to (1.0f to 0.8f)
            ).forEach { pair ->
                val (label, preset) = pair
                val sel = settings.speechRate == preset.first && settings.pitch == preset.second
                HudChip(label, sel, palette) {
                    vm.updateSettings { it.copy(speechRate = preset.first, pitch = preset.second) }
                }
            }
        }
        HudDivider(palette)

        PanelTitle(
            "SUARA NATURAL CLOUD",
            if (settings.cloudTtsEnabled) "AKTIF" else null,
            palette
        )
        ReactorToggle(
            label = "Gunakan suara natural cloud (model TTS Google)",
            checked = settings.cloudTtsEnabled,
            onChange = { v -> vm.updateSettings { it.copy(cloudTtsEnabled = v) } },
            palette = palette
        )
        if (settings.cloudTtsEnabled) {
            Spacer(Modifier.height(6.dp))
            Box {
                OutlinedButton(
                    onClick = { ttsModelMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        ttsModels.firstOrNull { it.name == settings.cloudTtsModel }?.shortName?.uppercase()
                            ?: "PILIH MODEL TTS",
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
                                    "Tidak ada model TTS — muat daftar model dulu, pak.",
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
            Box {
                OutlinedButton(
                    onClick = { voiceMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (settings.cloudVoice.isBlank()) "PILIH KARAKTER SUARA"
                        else "SUARA: ${settings.cloudVoice.uppercase()}",
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
                                    voiceLabel(v),
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
                    "TES SUARA AVI",
                    fontSize = 12.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        HudDivider(palette)

        PanelTitle("RUANG TES MENDENGAR", null, palette)
        OutlinedButton(
            onClick = { vm.listenOnce { result -> onTestResult(result) } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "TES MENDENGAR",
                color = palette.accent,
                fontSize = 12.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold
            )
        }
        testResult?.let { res ->
            Spacer(Modifier.height(4.dp))
            Text(
                "AVI dengar: \u201C$res\u201D",
                color = palette.accent,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

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

private fun voiceLabel(v: String): String =
    VOICE_DESC[v]?.let { "$v — $it" } ?: "$v — karakter Google"

// ============================================================
// TAB 3 — GAYA: panggilan custom, gaya bicara, memori, protokol
// ============================================================

@Composable
private fun GayaTab(
    vm: AviViewModel,
    settings: AviSettings,
    palette: AviPalette,
    panggilan: String?,
    onPanggilanChange: (String) -> Unit
) {
    val memories by vm.memories.collectAsState()
    val protocols by vm.protocols.collectAsState()
    var inputMem by remember { mutableStateOf("") }
    var protoName by remember { mutableStateOf("") }
    var protoPhrase by remember { mutableStateOf("") }
    var protoSteps by remember { mutableStateOf("") }
    val panggilanAktif = panggilan ?: settings.userName

    SystemHeader(name = "GAYA", active = true, palette = palette)
    HoloPanel(palette = palette) {
        // ---- panggilan custom (K18) ----
        PanelTitle("PANGGILAN SAYA", settings.userName.ifBlank { "Pak" }, palette)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = panggilanAktif,
            onValueChange = onPanggilanChange,
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
                HudChip(n, settings.userName == n, palette) { onPanggilanChange(n) }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            "Contoh: Pak, Mas, Bang, Kak — atau nama Anda sendiri. " +
                "AVI memakai panggilan ini di sapaan, greeting, dan seluruh jawabannya.",
            color = palette.textDim.copy(alpha = 0.75f),
            fontSize = 10.sp,
            fontFamily = AviHud,
            lineHeight = 13.sp
        )
        HudDivider(palette)

        // ---- gaya bicara ----
        PanelTitle("GAYA BICARA", settings.style.uppercase(), palette)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("sopan" to "Sopan", "santai" to "Santai", "jedai" to "Jedai").forEach { pair ->
                val value = pair.first
                val label = pair.second
                HudChip(label, settings.style == value, palette) {
                    vm.updateSettings { it.copy(style = value) }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            when (settings.style) {
                "sopan" -> "Bahasa formal penuh hormat — layaknya butler eksekutif."
                "jedai" -> "AI markas sci-fi — tenang, presisi, sesekali melaporkan status sistem."
                else -> "Santai tapi sopan, hangat, sesekali bercanda ringan."
            },
            color = palette.textDim,
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
            lineHeight = 13.sp
        )
        HudDivider(palette)

        // ---- memori ----
        PanelTitle("MEMORI AVI", "${memories.size}", palette)
        Spacer(Modifier.height(4.dp))
        Text(
            "Fakta yang AVI ingat permanen di setiap obrolan, pak.",
            color = palette.textDim,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(6.dp))
        if (memories.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                memories.forEach { mem ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "• ${mem.text}",
                            color = palette.text,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { vm.deleteMemory(mem.id) }, modifier = Modifier.size(30.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus memori",
                                tint = palette.danger,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                "Belum ada memori, pak.",
                color = palette.textDim,
                fontSize = 11.sp,
                fontFamily = AviHud
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
        HudDivider(palette)

        // ---- protokol ----
        PanelTitle("PROTOKOL", "${protocols.size}", palette)
        Spacer(Modifier.height(4.dp))
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
        OutlinedTextField(
            value = protoName,
            onValueChange = { protoName = it },
            placeholder = { Text("Nama protokol (mis. Protokol Malam)", color = palette.textDim, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = hudFieldColors(palette)
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = protoPhrase,
            onValueChange = { protoPhrase = it },
            placeholder = { Text("Frasa pemicu (mis. protokol malam)", color = palette.textDim, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = hudFieldColors(palette)
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = protoSteps,
            onValueChange = { protoSteps = it },
            placeholder = { Text("Langkah — satu perintah per baris", color = palette.textDim, fontSize = 12.sp) },
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
// TAB 4 — HUD: tema mark 2x2 + efek tampilan
// ============================================================

@Composable
private fun TampilanTab(vm: AviViewModel, settings: AviSettings, palette: AviPalette) {
    SystemHeader(name = "TAMPILAN", active = true, palette = palette)
    HoloPanel(palette = palette) {
        PanelTitle("TEMA MARK", settings.themeMark, palette)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MarkCard(
                markKey = "MK1", name = "MK-I", tagline = "Cyan Inti Ark",
                selected = settings.themeMark == "MK1",
                onSelect = { vm.updateSettings { it.copy(themeMark = "MK1") } },
                modifier = Modifier.weight(1f)
            )
            MarkCard(
                markKey = "MK2", name = "MK-II", tagline = "Hot Rod Merah",
                selected = settings.themeMark == "MK2",
                onSelect = { vm.updateSettings { it.copy(themeMark = "MK2") } },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MarkCard(
                markKey = "MK3", name = "MK-III", tagline = "Aureus Emas",
                selected = settings.themeMark == "MK3",
                onSelect = { vm.updateSettings { it.copy(themeMark = "MK3") } },
                modifier = Modifier.weight(1f)
            )
            MarkCard(
                markKey = "MK4", name = "MK-IV", tagline = "Eidolon Hologram",
                selected = settings.themeMark == "MK4",
                onSelect = { vm.updateSettings { it.copy(themeMark = "MK4") } },
                modifier = Modifier.weight(1f)
            )
        }
        HudDivider(palette)
        PanelTitle("EFEK HUD", null, palette)
        ReactorToggle(
            label = "Boot sequence sinematik",
            checked = settings.bootEnabled,
            onChange = { v -> vm.updateSettings { it.copy(bootEnabled = v) } },
            palette = palette
        )
        ReactorToggle(
            label = "Parallax giroskop",
            checked = settings.parallaxEnabled,
            onChange = { v -> vm.updateSettings { it.copy(parallaxEnabled = v) } },
            palette = palette
        )
        ReactorToggle(
            label = "Getaran khas status",
            checked = settings.hapticsEnabled,
            onChange = { v -> vm.updateSettings { it.copy(hapticsEnabled = v) } },
            palette = palette
        )
        ReactorToggle(
            label = "Diagnostik orbit",
            checked = settings.diagnosticsOrbit,
            onChange = { v -> vm.updateSettings { it.copy(diagnosticsOrbit = v) } },
            palette = palette
        )
        // K19 — bingkai HUD kelas Jarvis (bracket sudut + rel + sapuan hologram)
        ReactorToggle(
            label = "Bingkai HUD kelas Jarvis",
            checked = settings.hudFrame,
            onChange = { v -> vm.updateSettings { it.copy(hudFrame = v) } },
            palette = palette
        )
        ReactorToggle(
            label = "Mode hemat daya (matikan efek berat)",
            checked = settings.powerSave,
            onChange = { v -> vm.updateSettings { it.copy(powerSave = v) } },
            palette = palette
        )
    }
}

// ============================================================
// TAB 5 — SISTEM: kuota, aksi cepat, modul antrean, tentang
// ============================================================

@Composable
private fun SistemTab(
    vm: AviViewModel,
    settings: AviSettings,
    palette: AviPalette,
    onGoTab: (HudTab) -> Unit,
    onTestResult: (String) -> Unit,
    onOpenAiStudio: () -> Unit
) {
    val context = LocalContext.current
    val memories by vm.memories.collectAsState()
    val protocols by vm.protocols.collectAsState()
    val usageToday by vm.usageToday.collectAsState()
    val modelTest by vm.modelTest.collectAsState()
    val models by vm.models.collectAsState()
    val chatModels = models.filter { it.canChat && it.isPureChat }
    val siap = modelTest.values.count { it.ok }
    val versiAvi = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }
    }

    // ---- kuota & aktivitas ----
    SystemHeader(name = "SISTEM", active = true, palette = palette)
    HoloPanel(palette = palette) {
        PanelTitle("KUOTA & AKTIVITAS", null, palette)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            GaugeRing(
                label = "DARI ±1500",
                valueText = "$usageToday REQ",
                progress = usageToday / 1500f,
                palette = palette
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                InfoRow("Model siap", if (chatModels.isEmpty()) "-" else "$siap / ${chatModels.size}", palette = palette)
                InfoRow("Memori tersimpan", "${memories.size}", palette = palette)
                InfoRow("Protokol", "${protocols.size}", palette = palette)
                InfoRow(
                    "Penyedia",
                    if (settings.provider == AiProvider.GEMINI) "Gemini" else "OpenAI-compat",
                    palette = palette
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Kuota gratis berubah-ubah mengikut model — angka ini perkiraan hitung lokal.",
            color = palette.textDim.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontFamily = AviHud,
            lineHeight = 12.sp
        )
    }
    Spacer(Modifier.height(12.dp))

    // ---- aksi cepat ----
    HoloPanel(palette = palette) {
        PanelTitle("AKSI CEPAT", null, palette)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton(
                icon = Icons.Default.DeleteSweep,
                label = "RIWAYAT",
                danger = true,
                onClick = { vm.clearHistory() },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.VolumeUp,
                label = "UJI SUARA",
                onClick = { vm.speakTest("Hay pak, semua sistem normal. AVI siap membantu.") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton(
                icon = Icons.Default.Hearing,
                label = "TES DENGAR",
                onClick = {
                    onGoTab(HudTab.SUARA)
                    vm.listenOnce { result -> onTestResult(result) }
                },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.Troubleshoot,
                label = "DIAGNOSA",
                onClick = {
                    onGoTab(HudTab.OTAK)
                    vm.runDiagnosis()
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton(
                icon = Icons.Default.Memory,
                label = "MUAT MODEL",
                onClick = {
                    onGoTab(HudTab.OTAK)
                    vm.fetchModels()
                },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.OpenInNew,
                label = "AI STUDIO",
                onClick = onOpenAiStudio,
                modifier = Modifier.weight(1f)
            )
        }
    }
    Spacer(Modifier.height(12.dp))

    // ---- modul antrean (fitur kaya, SEGERA) ----
    HoloPanel(palette = palette) {
        PanelTitle("MODUL BERIKUTNYA", "ANTREAN", palette)
        Spacer(Modifier.height(4.dp))
        Text(
            "Kerangka modul lanjutan sudah terpasang di markas — menunggu giliran dibangun.",
            color = palette.textDim,
            fontSize = 10.sp,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModuleCard(
                icon = Icons.Default.RecordVoiceOver,
                name = "Wake Word",
                desc = "Bangun AVI dengan \u201CHey AVI\u201D tanpa sentuhan.",
                modifier = Modifier.weight(1f)
            )
            ModuleCard(
                icon = Icons.Default.WbSunny,
                name = "Cuaca Langsung",
                desc = "Prakiraan cuaca setempat tiap pagi.",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModuleCard(
                icon = Icons.Default.Newspaper,
                name = "Berita Harian",
                desc = "Ringkas berita pilihan tiap pagi.",
                modifier = Modifier.weight(1f)
            )
            ModuleCard(
                icon = Icons.Default.Translate,
                name = "Penerjemah",
                desc = "Terjemah lisan dua arah seketika.",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModuleCard(
                icon = Icons.Default.CalendarMonth,
                name = "Kalender",
                desc = "Baca & ingatkan jadwal harian.",
                modifier = Modifier.weight(1f)
            )
            ModuleCard(
                icon = Icons.Default.HomeWork,
                name = "Rumah Pintar",
                desc = "Kendalikan lampu & alat via suara.",
                modifier = Modifier.weight(1f)
            )
        }
    }
    Spacer(Modifier.height(12.dp))

    // ---- tentang markas ----
    HoloPanel(palette = palette) {
        PanelTitle("TENTANG MARKAS", null, palette)
        Spacer(Modifier.height(6.dp))
        InfoRow("Versi AVI", "v$versiAvi", palette = palette)
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

/**
 * K11 — Kartu DIAGNOSA KONEKSI: membongkar akar masalah koneksi ke Google dalam
 * hitungan detik dan menampilkan JALAN KELUAR yang bisa dijalankan (tautan aktif),
 * bukan sekadar pesan gagal. Tampil menyatu dengan gaya HUD Pusat Kendali.
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
            // ---- vonak ----
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
