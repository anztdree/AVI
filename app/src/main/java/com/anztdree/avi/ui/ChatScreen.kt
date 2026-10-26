package com.anztdree.avi.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.anztdree.avi.R
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.sound.UiSounds
import com.anztdree.avi.ui.theme.AviBody
import com.anztdree.avi.ui.theme.AviDisplay
import com.anztdree.avi.ui.theme.AviHud
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

    // ---- K12 — efek suara mengikuti denyut state reaktor ----
    LaunchedEffect(state) {
        if (settings.powerSave) return@LaunchedEffect
        when (state) {
            AviState.THINKING -> UiSounds.send(context)
            AviState.SPEAKING -> UiSounds.receive(context)
            AviState.LISTENING -> UiSounds.wake(context)
            AviState.LIVE -> UiSounds.live(context)
            AviState.IDLE -> Unit
        }
    }
    LaunchedEffect(error) {
        if (error != null && !settings.powerSave) UiSounds.error(context)
    }

    val setupNeeded = settings.provider == AiProvider.GEMINI &&
        (settings.geminiKey.isBlank() || settings.geminiModel.isBlank())

    // ---- data yang mengorbit reaktor (JAM · BAT · RAM · NET · OTAK) ----
    val orbitItems = remember(clockText, diagnostics, settings.geminiModel) {
        buildList {
            add("JAM $clockText")
            diagnostics?.let { d ->
                add("BAT ${d.batteryPct}%${if (d.charging) "⚡" else ""}")
                add("RAM ${d.ramUsedPct}%")
                add("NET ${d.network}")
            }
            add("OTAK ${settings.geminiModel.removePrefix("models/").ifBlank { "—" }}")
        }
    }

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
    val modelChip = settings.geminiModel.removePrefix("models/").ifBlank { "PILIH MODEL" }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, state) {
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) listState.scrollToItem(total - 1)
    }

    Box(Modifier.fillMaxSize().background(palette.bg)) {
        // ---- K12 — latar nebula HD (dilucuti di hemat daya) ----
        if (!settings.powerSave) {
            Image(
                painter = painterResource(R.drawable.bg_nebula),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                palette.bg.copy(alpha = 0.45f),
                                palette.bg.copy(alpha = 0.72f),
                                palette.bg.copy(alpha = 0.88f)
                            )
                        )
                    )
            )
        }
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
                    fontFamily = AviDisplay,
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

            // ============ PESAN — HANYA 3 TERAKHIR (efisien, animasi bawah tetap terlihat) ============
            val visibleMessages = remember(messages) { messages.takeLast(3) }
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (setupNeeded) {
                    item { SetupCard(onOpenSettings = onOpenSettings) }
                }
                itemsIndexed(visibleMessages) { index, msg ->
                    // makin tua makin pudar — fokus ke jawaban terbaru
                    val fade = when {
                        visibleMessages.size <= 1 -> 1f
                        index == 0 && visibleMessages.size == 3 -> 0.45f
                        index == 0 -> 0.6f
                        index == 1 && visibleMessages.size == 3 -> 0.7f
                        else -> 1f
                    }
                    Box(Modifier.alpha(fade)) {
                        MessageBubble(role = msg.role, text = msg.text)
                    }
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
                    fontFamily = AviBody,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // ============ ZONA REAKTOR (Mark III: orb · diagnostik mengorbit · busur) ============
            Box(Modifier.fillMaxWidth().height(232.dp)) {
                ReactorHalo(
                    palette = palette,
                    enabled = settings.diagnosticsOrbit && !settings.powerSave,
                    glowLevel = maxOf(rmsLevel, voiceLevel),
                    orbitItems = orbitItems,
                    modifier = Modifier.matchParentSize()
                )
                ReactorOrb(
                    state = state,
                    micLevel = rmsLevel,
                    voiceLevel = voiceLevel,
                    palette = palette,
                    powerSave = settings.powerSave,
                    showVisor = true,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(160.dp),
                    onTap = { ensureMicThenToggle() }
                )
            }

            // ============ PERINTAH CEPAT & DOCK STATUS DIHAPUS TOTAL (keputusan pemilik K10) ============
            Spacer(Modifier.height(4.dp))

            // ============ BANNER MODE LIVE ============
            if (state == AviState.LIVE) {
                Text(
                    "MODE LIVE — bicara langsung, sentuh reaktor untuk berhenti",
                    color = palette.danger,
                    fontSize = 10.sp,
                    fontFamily = AviHud,
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
