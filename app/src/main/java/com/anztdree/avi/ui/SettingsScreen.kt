package com.anztdree.avi.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.anztdree.avi.ui.theme.AviDisplay
import com.anztdree.avi.ui.theme.AviHud
import com.anztdree.avi.ui.theme.AviPalette
import com.anztdree.avi.ui.theme.AviTheme
import com.anztdree.avi.ui.theme.LocalAviPalette
import com.anztdree.avi.ui.theme.MarkPalettes
import java.util.Locale
import java.util.UUID

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
    val models by vm.models.collectAsState()
    val modelsLoading by vm.modelsLoading.collectAsState()
    val modelsError by vm.modelsError.collectAsState()
    val modelTest by vm.modelTest.collectAsState()
    val batchTest by vm.batchTest.collectAsState()   // K14 — progres uji semua model
    val pickNote by vm.pickNote.collectAsState()     // K14 — catatan auto-pick
    val diagnosis by vm.diagnosis.collectAsState()
    val memories by vm.memories.collectAsState()
    val protocols by vm.protocols.collectAsState()
    val usageToday by vm.usageToday.collectAsState()
    val ttsReady by vm.ttsReady.collectAsState()
    val context = LocalContext.current

    // K18 — panggilan custom: AVI tidak lagi menyapa “Pak” terus-menerus
    var inputPanggilan by remember { mutableStateOf(settings.userName) }
    // K18 — versi build ditampilkan dinamis (ikut build, bukan teks manual)
    val versiApp = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }
    }

    var showKey by remember { mutableStateOf(false) }
    var inputMem by remember { mutableStateOf("") }
    var protoName by remember { mutableStateOf("") }
    var protoPhrase by remember { mutableStateOf("") }
    var protoSteps by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf<String?>(null) }
    var ttsModelMenu by remember { mutableStateOf(false) }
    var voiceMenu by remember { mutableStateOf(false) }

    val brainActive = if (settings.provider == AiProvider.GEMINI) {
        settings.geminiKey.isNotBlank() && settings.geminiModel.isNotBlank()
    } else {
        settings.ocKey.isNotBlank() && settings.ocModel.isNotBlank()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // K12 — latar bintang HD halus di seluruh Pusat Kendali
        Image(
            painter = painterResource(R.drawable.bg_stars),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.30f
        )
        Box(
            Modifier
                .matchParentSize()
                .background(palette.bg.copy(alpha = 0.74f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
        // ============ HEADER ============
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = palette.text
                )
            }
            // K12 — badge inti plasma HD
            Image(
                painter = painterResource(R.drawable.tex_core),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.dp, palette.accent.copy(alpha = 0.5f), CircleShape),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(
                    "PUSAT KENDALI",
                    color = palette.text,
                    fontSize = 17.sp,
                    fontFamily = AviDisplay,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text(
                    "SISTEM MARKAS — v$versiApp",
                    color = palette.textDim,
                    fontSize = 9.sp,
                    fontFamily = AviHud,
                    letterSpacing = 1.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        // ============ A. OTAK ============
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
                Spacer(Modifier.height(8.dp))
                // K14 — hanya model MURNI-OBROLAN (bukan TTS/gambar/live dsb.)
                val chatModels = models.filter { it.canChat && it.isPureChat }
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
                Spacer(Modifier.height(8.dp))

                // ============ K14 — UJI SEMUA MODEL (semua generasi, bukan cuma 2.5) ============
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
                    val siap = modelTest.values.count { it.ok }
                    Text(
                        "Hasil uji terakhir: $siap SIAP dari ${modelTest.size} model dicoba",
                        color = if (siap > 0) palette.accent else palette.danger,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
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
                    }
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

                // ============ K11 — DIAGNOSA KONEKSI ============
                Spacer(Modifier.height(10.dp))
                DiagnosisCard(
                    diagnosis = diagnosis,
                    onRun = { vm.runDiagnosis() },
                    palette = palette
                )

                Spacer(Modifier.height(2.dp))
                Text(
                    "GAYA BICARA",
                    color = palette.textDim,
                    fontSize = 10.sp,
                    fontFamily = AviHud,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("sopan" to "Sopan", "santai" to "Santai", "jedai" to "Jedai").forEach { pair ->
                        val value = pair.first
                        val label = pair.second
                        val sel = settings.style == value
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (sel) palette.accent.copy(alpha = 0.18f) else palette.panelLight)
                                .border(
                                    1.dp,
                                    if (sel) palette.accent else palette.panelLight,
                                    RoundedCornerShape(50)
                                )
                                .clickable { vm.updateSettings { it.copy(style = value) } }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                label,
                                color = if (sel) palette.accent else palette.textDim,
                                fontSize = 12.sp
                            )
                        }
                    }
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
        Spacer(Modifier.height(14.dp))

        // ============ K18 — PEMILIK (panggilan custom) ============
        SystemHeader(name = "PEMILIK", active = true, palette = palette)
        HoloPanel(palette = palette) {
            Text(
                "PANGGILAN SAYA",
                color = palette.textDim,
                fontSize = 10.sp,
                fontFamily = AviHud,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = inputPanggilan,
                onValueChange = { v ->
                    inputPanggilan = v
                    val bersih = v.trim()
                    vm.updateSettings { it.copy(userName = bersih.ifBlank { "Pak" }) }
                },
                label = { Text("AVI memanggil saya sebagai…", color = palette.textDim, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = hudFieldColors(palette)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Contoh: Pak, Mas, Bang, Kak — atau nama Anda sendiri. " +
                    "AVI memakai panggilan ini di sapaan, greeting, dan seluruh jawabannya.",
                color = palette.textDim.copy(alpha = 0.75f),
                fontSize = 10.sp,
                fontFamily = AviHud
            )
        }
        Spacer(Modifier.height(14.dp))

        // ============ B. SUARA ============
        SystemHeader(name = "SUARA", active = ttsReady, palette = palette)
        HoloPanel(palette = palette) {
            Text(
                "AVI selalu membacakan setiap jawaban — wajib, tanpa tombol",
                color = palette.textDim,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(Modifier.height(6.dp))
            ReactorToggle(
                label = "Mode percakapan beruntun",
                checked = settings.autoListen,
                onChange = { v -> vm.updateSettings { it.copy(autoListen = v) } },
                palette = palette
            )
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
            Spacer(Modifier.height(8.dp))
            SectionTitle("SUARA NATURAL CLOUD", palette)
            ReactorToggle(
                label = "Gunakan suara natural cloud (model TTS Google)",
                checked = settings.cloudTtsEnabled,
                onChange = { v -> vm.updateSettings { it.copy(cloudTtsEnabled = v) } },
                palette = palette
            )
            if (settings.cloudTtsEnabled) {
                Spacer(Modifier.height(6.dp))
                val ttsModels = models.filter { it.canTts }
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
                                        v,
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
                OutlinedButton(
                    onClick = { vm.speakTest("Hay pak, semua sistem normal. AVI siap membantu.") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "TES SUARA AVI",
                        color = palette.accent,
                        fontSize = 12.sp,
                        fontFamily = AviHud,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            SectionTitle("RUANG TES MENDENGAR", palette)
            OutlinedButton(
                onClick = { vm.listenOnce { result -> testResult = result } },
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
        Spacer(Modifier.height(14.dp))

        // ============ C. TAMPILAN ============
        SystemHeader(name = "TAMPILAN", active = true, palette = palette)
        HoloPanel(palette = palette) {
            Text(
                "TEMA MARK",
                color = palette.textDim,
                fontSize = 10.sp,
                fontFamily = AviHud,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("MK1" to "MK-I", "MK2" to "MK-II", "MK3" to "MK-III", "MK4" to "MK-IV").forEach { pair ->
                    val key = pair.first
                    val label = pair.second
                    val markPal = MarkPalettes[key] ?: MarkPalettes.getValue("MK1")
                    val sel = settings.themeMark == key
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(markPal.accent, markPal.gold, markPal.panel))
                                )
                                .border(
                                    if (sel) 2.dp else 1.dp,
                                    if (sel) markPal.accent else palette.textDim.copy(alpha = 0.35f),
                                    CircleShape
                                )
                                .clickable { vm.updateSettings { it.copy(themeMark = key) } }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            label,
                            color = if (sel) palette.accent else palette.textDim,
                            fontSize = 10.sp,
                            fontFamily = AviHud
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
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
        Spacer(Modifier.height(14.dp))

        // ============ D. MEMORI ============
        SystemHeader(name = "MEMORI", active = memories.isNotEmpty(), palette = palette)
        HoloPanel(palette = palette) {
            Text(
                "Fakta yang AVI ingat permanen di setiap obrolan, pak.",
                color = palette.textDim,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(8.dp))
            if (memories.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
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
            Spacer(Modifier.height(12.dp))
            SectionTitle("PROTOKOL", palette)
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
            Spacer(Modifier.height(4.dp))
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
        Spacer(Modifier.height(14.dp))

        // ============ E. SISTEM ============
        SystemHeader(name = "SISTEM", active = true, palette = palette)
        HoloPanel(palette = palette) {
            // K18 — bagian STATUS SISTEM (gauge baterai/RAM/penyimpanan/jaringan)
            // DIHAPUS dari Pusat Kendali sesuai permintaan pemilik; telemetri
            // perangkat tetap hidup mengorbit reaktor di layar utama.
            // K14 — versi APK terlihat jelas: pemilik bisa memastikan build terbaru terpasang
            val versiAvi = remember {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
                } catch (_: Exception) { "?" }
            }
            Text(
                "AVI v$versiAvi · ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} · " +
                    "Android ${android.os.Build.VERSION.RELEASE}",
                color = palette.textDim,
                fontSize = 11.sp,
                fontFamily = AviHud
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Pemakaian API hari ini: $usageToday permintaan",
                color = palette.textDim,
                fontSize = 11.sp,
                fontFamily = AviHud
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { vm.clearHistory() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.danger.copy(alpha = 0.16f),
                    contentColor = palette.danger
                )
            ) {
                Text(
                    "HAPUS RIWAYAT PERCAKAPAN",
                    fontSize = 12.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "AVI — Asisten Virtual Indonesia · Semua data tersimpan lokal di HP Anda.",
            color = palette.textDim,
            fontSize = 10.sp,
            fontFamily = AviHud,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )
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
