package com.anztdree.avi.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anztdree.avi.AviState
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.ui.theme.AviTheme
import com.anztdree.avi.ui.theme.LocalAviPalette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(vm: AviViewModel, onOpenSettings: () -> Unit) {
    val settings by vm.settings.collectAsState()
    // Live preview tema — mark diambil langsung dari settings
    AviTheme(mark = settings.themeMark) {
        ChatScreenContent(vm = vm, settings = settings, onOpenSettings = onOpenSettings)
    }
}

@Composable
private fun ChatScreenContent(vm: AviViewModel, settings: AviSettings, onOpenSettings: () -> Unit) {
    val palette = LocalAviPalette.current
    val state by vm.state.collectAsState()
    val messages by vm.messages.collectAsState()
    val streamingText by vm.streamingText.collectAsState()
    val partialText by vm.partialText.collectAsState()
    val rmsLevel by vm.rmsLevel.collectAsState()
    val voiceLevel by vm.voiceLevel.collectAsState()
    val error by vm.error.collectAsState()
    val diagnostics by vm.diagnostics.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // ---- izin mikrofon ----
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.onMicPermissionGranted()
    }
    fun ensureMicThenToggle() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) vm.toggleMic() else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // ---- jam untuk diagnostik orbit (update tiap ± menit) ----
    var clockText by remember { mutableStateOf("--.--") }
    LaunchedEffect(Unit) {
        val fmt = SimpleDateFormat("HH.mm", Locale.getDefault())
        while (true) {
            clockText = fmt.format(Date())
            delay(10_000)
        }
    }

    val setupNeeded = settings.provider == AiProvider.GEMINI &&
        (settings.geminiKey.isBlank() || settings.geminiModel.isBlank())

    val statusLabel = when (state) {
        AviState.IDLE -> "SIAGA"
        AviState.LISTENING -> "MENDENGAR"
        AviState.THINKING -> "BERPIKIR"
        AviState.SPEAKING -> "MENJAWAB"
        AviState.LIVE -> "LIVE"
    }
    val statusColor = when (state) {
        AviState.IDLE -> palette.accent
        AviState.LISTENING -> palette.accent
        AviState.THINKING -> palette.gold
        AviState.SPEAKING -> palette.goldSoft
        AviState.LIVE -> palette.danger
    }
    val orbLabel = when (state) {
        AviState.IDLE -> "SIAGA — SENTUH REAKTOR UNTUK BICARA"
        AviState.LISTENING -> "MENDENGAR… SILAKAN BICARA PAK"
        AviState.THINKING -> "MEMPROSES…"
        AviState.SPEAKING -> "AVI MENJAWAB…"
        AviState.LIVE -> "MODE LIVE — SUARA LANGSUNG"
    }
    val modelChip = settings.geminiModel.removePrefix("models/").ifBlank { "PILIH MODEL" }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, state, streamingText) {
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    Box(Modifier.fillMaxSize().background(palette.bg)) {
        HexGridBackground(
            palette = palette,
            enabled = settings.parallaxEnabled && !settings.powerSave,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 14.dp)
        ) {
            // ============ BAR ATAS ============
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Text(
                    "AVI",
                    color = palette.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(8.dp))
                StatusChip(text = statusLabel, color = statusColor)
                Spacer(Modifier.weight(1f))
                TopBarChip(
                    label = modelChip,
                    color = if (settings.geminiModel.isBlank()) palette.textDim else palette.accent,
                    onClick = onOpenSettings
                )
                IconButton(
                    onClick = { vm.updateSettings { it.copy(translatorMode = !it.translatorMode) } },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Default.Translate,
                        contentDescription = "Mode penerjemah",
                        tint = if (settings.translatorMode) palette.accent else palette.textDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { vm.updateSettings { it.copy(autoSpeak = !it.autoSpeak) } },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        if (settings.autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Suara balasan",
                        tint = if (settings.autoSpeak) palette.accent else palette.textDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(38.dp)) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = palette.textDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            error?.let { msg ->
                ErrorBanner(message = msg, onDismiss = { vm.dismissError() })
                Spacer(Modifier.height(6.dp))
            }

            // ============ PESAN ============
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (setupNeeded) {
                    item { SetupCard(onOpenSettings = onOpenSettings) }
                }
                items(messages) { msg ->
                    MessageBubble(role = msg.role, text = msg.text)
                }
                if (state == AviState.THINKING) {
                    item { MessageBubble(role = "avi", text = streamingText, streaming = true) }
                }
            }

            // ============ TEKS PARSIAL STT ============
            if (partialText.isNotBlank()) {
                Text(
                    "\u201C$partialText\u201D",
                    color = palette.accent,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // ============ AREA REAKTOR ============
            Box(Modifier.fillMaxWidth().height(212.dp)) {
                DiagnosticsOrbit(
                    time = clockText,
                    battery = diagnostics?.let { "${it.batteryPct}%${if (it.charging) "⚡" else ""}" } ?: "—",
                    network = diagnostics?.network ?: "OFFLINE",
                    model = settings.geminiModel.removePrefix("models/").ifBlank { "—" },
                    palette = palette,
                    enabled = settings.diagnosticsOrbit && !settings.powerSave,
                    modifier = Modifier.matchParentSize()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    ReactorOrb(
                        state = state,
                        micLevel = rmsLevel,
                        voiceLevel = voiceLevel,
                        palette = palette,
                        powerSave = settings.powerSave,
                        showVisor = true,
                        modifier = Modifier.size(170.dp),
                        onTap = { ensureMicThenToggle() }
                    )
                    Text(
                        orbLabel,
                        color = palette.textDim,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ============ PERINTAH CEPAT ============
            ChipRow(
                chips = listOf("Cuaca", "Berita hari ini", "Kondisi HP", "Buka aplikasi", "Ingat…"),
                enabled = state != AviState.THINKING,
                onChip = { chip ->
                    when (chip) {
                        "Cuaca" -> vm.sendText("Bagaimana cuaca hari ini, pak?")
                        "Berita hari ini" -> vm.sendText("Berikan 3 berita utama Indonesia hari ini.")
                        "Kondisi HP" -> vm.sendText("Kondisi HP sekarang, pak")
                        "Buka aplikasi" -> inputText = "Buka "
                        "Ingat…" -> inputText = "Ingat ini pak: "
                    }
                },
                palette = palette
            )
            Spacer(Modifier.height(6.dp))

            // ============ BANNER MODE LIVE ============
            if (state == AviState.LIVE) {
                Text(
                    "MODE LIVE — bicara langsung, sentuh reaktor untuk berhenti",
                    color = palette.danger,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.danger.copy(alpha = 0.10f))
                        .padding(vertical = 6.dp)
                )
                Spacer(Modifier.height(6.dp))
            }

            // ============ BILAH INPUT ============
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 4.dp)) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ketik pesan ke AVI…", color = palette.textDim, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = palette.panelLight,
                        cursorColor = palette.accent,
                        focusedTextColor = palette.text,
                        unfocusedTextColor = palette.text
                    )
                )
                Spacer(Modifier.width(6.dp))
                InputButton(
                    icon = Icons.Default.Podcasts,
                    contentDescription = "Mode live",
                    bg = if (state == AviState.LIVE) palette.danger else palette.panelLight,
                    tint = if (state == AviState.LIVE) Color.White else palette.textDim,
                    onClick = { if (state == AviState.LIVE) vm.stopLive() else vm.startLive() }
                )
                Spacer(Modifier.width(6.dp))
                InputButton(
                    icon = if (state == AviState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Mikrofon",
                    bg = if (state == AviState.LISTENING) palette.accent else palette.panelLight,
                    tint = if (state == AviState.LISTENING) palette.bg else palette.accent,
                    onClick = { ensureMicThenToggle() }
                )
                Spacer(Modifier.width(6.dp))
                InputButton(
                    icon = Icons.Default.Send,
                    contentDescription = "Kirim",
                    bg = palette.gold,
                    tint = palette.bg,
                    onClick = {
                        val t = inputText.trim()
                        if (t.isNotEmpty()) {
                            vm.sendText(t)
                            inputText = ""
                        }
                    }
                )
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun InputButton(
    icon: ImageVector,
    contentDescription: String,
    bg: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, tint.copy(alpha = 0.4f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}
