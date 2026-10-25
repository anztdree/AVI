package com.anztdree.avi.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.ai.ModelCatalog
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.Protocol
import com.anztdree.avi.device.DeviceDiagnostics
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
    val memories by vm.memories.collectAsState()
    val protocols by vm.protocols.collectAsState()
    val usageToday by vm.usageToday.collectAsState()
    val diagnostics by vm.diagnostics.collectAsState()
    val ttsReady by vm.ttsReady.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.refreshDiagnostics() }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
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
            Column {
                Text(
                    "PUSAT KENDALI",
                    color = palette.text,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text(
                    "SISTEM MARKAS — v1.0.0",
                    color = palette.textDim,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
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
                fontFamily = FontFamily.Monospace,
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
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                modelsError?.let { err ->
                    Spacer(Modifier.height(4.dp))
                    Text(err, color = palette.danger, fontSize = 11.sp, lineHeight = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
                val chatModels = models.filter { it.canChat }
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
                Spacer(Modifier.height(2.dp))
                Text(
                    "GAYA BICARA",
                    color = palette.textDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
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
                            fontFamily = FontFamily.Monospace,
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
                                        fontFamily = FontFamily.Monospace
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
                            fontFamily = FontFamily.Monospace,
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
                                        fontFamily = FontFamily.Monospace
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
                        fontFamily = FontFamily.Monospace,
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
                    fontFamily = FontFamily.Monospace,
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
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("MK1" to "MK-I", "MK2" to "MK-II", "MK3" to "MK-III").forEach { pair ->
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
                            fontFamily = FontFamily.Monospace
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
                    fontFamily = FontFamily.Monospace
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
                        fontFamily = FontFamily.Monospace,
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
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "${p.steps.size} langkah",
                                color = palette.textDim,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        TextButton(onClick = { vm.runProtocol(p) }) {
                            Text(
                                "JALANKAN",
                                color = palette.accent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
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
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        // ============ E. SISTEM ============
        SystemHeader(name = "SISTEM", active = true, palette = palette)
        HoloPanel(palette = palette) {
            val d = diagnostics ?: DeviceDiagnostics()
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                GaugeRing(
                    label = "BATERAI",
                    valueText = "${d.batteryPct}%",
                    progress = d.batteryPct / 100f,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
                GaugeRing(
                    label = "RAM",
                    valueText = "${d.ramUsedPct}%",
                    progress = d.ramUsedPct / 100f,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                val storProg = if (d.storageTotalGb > 0f) {
                    (1f - d.storageFreeGb / d.storageTotalGb).coerceIn(0f, 1f)
                } else 0f
                GaugeRing(
                    label = "PENYIMPANAN",
                    valueText = String.format(Locale.US, "sisa %.1f G", d.storageFreeGb),
                    progress = storProg,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
                GaugeRing(
                    label = "JARINGAN",
                    valueText = d.network,
                    progress = 1f,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${d.device} · ${d.androidVersion}",
                color = palette.textDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Pemakaian API hari ini: $usageToday permintaan",
                color = palette.textDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
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
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "AVI — Asisten Virtual Indonesia · Semua data tersimpan lokal di HP Anda.",
            color = palette.textDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )
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
