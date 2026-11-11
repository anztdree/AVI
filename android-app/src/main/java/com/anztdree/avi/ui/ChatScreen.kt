package com.anztdree.avi.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anztdree.avi.AviState
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.agent.AgentEvent
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.sound.UiSounds
import com.anztdree.avi.ui.theme.AviBody
import com.anztdree.avi.ui.theme.AviDisplay
import com.anztdree.avi.ui.theme.AviHud
import com.anztdree.avi.ui.theme.AviPalette
import com.anztdree.avi.ui.theme.AviTheme
import com.anztdree.avi.ui.theme.LocalAviPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ============================================================
// LAYAR OBROLAN — satu panggung HUD yang seluruhnya hidup:
// nebula bernafas + medan bintang berkelip + grid parallax +
// reaktor berlapis + obrolan masuk ber-spring. Tanpa gambar
// mati yang ditempel (keputusan pemilik K13).
// P43 (sinkron preview yang disetujui Pak): header bersih 1 tombol
// Pengaturan, BAR MODEL AKTIF + TES di bawah header, dock = bar
// hasil suara (kb via tekan-lama), visor dimatikan, latar diperjelas.
// P44 (umpan Pak): CINCIN TEKS TELEMETRI DI SEKELILING ORB DIHAPUS —
// panggung orb murni bentuk MK-XIX + aura; Pengaturan dirancang ulang
// (VOKAL JAWABAN + SUARA LANGSUNG nyata, MARKAS = diagnostik perangkat).
// ============================================================

@Composable
fun ChatScreen(vm: AviViewModel, onOpenSettings: () -> Unit) {
    val settings by vm.settings.collectAsState()
    // Live preview tema — mark diambil langsung dari settings
    AviTheme(mark = settings.themeMark) {
        ChatScreenContent(vm = vm, settings = settings, onOpenSettings = onOpenSettings)
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
    // K22 — timeline aksi mesin agen
    val agentEvents by vm.agentEvents.collectAsState()
    var inputText by remember { mutableStateOf("") }
    // K19 — deck modul kelas Jarvis
    var showDeck by remember { mutableStateOf(false) }
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

    // P44 — jam telemetri orbit DIHAPUS (umpan Pak: cincin teks jelek);
    // jam hanya hidup sebagai stempel waktu pesan
    // format jam chip di bubble (mmeta .mts preview)
    val msgTimeFmt = remember { SimpleDateFormat("HH.mm", Locale.getDefault()) }

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

    val listState = rememberLazyListState()
    // P43 — hasil tombol TES di bar MODEL AKTIF
    val barTest by vm.barTest.collectAsState()
    LaunchedEffect(messages.size, streamingText) {
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) listState.scrollToItem(total - 1)
    }

    Box(Modifier.fillMaxSize().background(palette.bg)) {
        // ---- P43 LATAR PANGGUNG (setia preview): 3 pendaran + aurora +
        //      9 debu energi + 1 bintang jatuh + vignette — TANPA foto/grid ----
        AuroraStage(
            palette = palette,
            enabled = !settings.powerSave,
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
            // ============ P43 — BAR ATAS BERSIH: mark + brand, pojok kanan CUMA 1 tombol PENGATURAN
            //   (umpan Pak: chip SIAGA & jam DIHAPUS dari depan; P44: cincin telemetri ikut dihapus —
            //    jam tak lagi hidup di orbit; Deck pindah ke ketukan MARK logo — depan tetap satu
            //    tombol Pengaturan, keputusan Pak).
            //   Porting setia preview: bar kaca + RAMBUT GRADIEN emas-sian di bawahnya +
            //   brand bergradasi teks + tombol Pengaturan berbingkai emas dua lapis. ============
            Box(Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.045f), Color.Transparent)
                            )
                        )
                        .padding(top = 6.dp, bottom = 12.dp)
                ) {
                    HeaderMark(
                        palette = palette,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { showDeck = true }
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "AVI",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = AviDisplay,
                            letterSpacing = 5.sp,
                            lineHeight = 16.sp,
                            style = TextStyle(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        palette.text,
                                        palette.accent,
                                        palette.gold
                                    )
                                )
                            )
                        )
                        Text(
                            "ASISTEN PRIBADI",
                            color = palette.textDim,
                            fontSize = 8.sp,
                            fontFamily = AviHud,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 3.4.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    // PENGATURAN — bingkai emas dua lapis + ring putus-putus dalam (preview #btnSet)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        palette.gold.copy(alpha = 0.17f),
                                        palette.gold.copy(alpha = 0.05f),
                                        palette.accent.copy(alpha = 0.09f)
                                    )
                                )
                            )
                            .border(1.dp, palette.gold.copy(alpha = 0.40f), RoundedCornerShape(13.dp))
                            .drawBehind {
                                val r = 11.dp.toPx()
                                val rc = Offset(size.width / 2f - r, size.height / 2f - r)
                                for (k in 0 until 24) {
                                    drawArc(
                                        color = palette.gold.copy(alpha = 0.30f),
                                        startAngle = k * 15f, sweepAngle = 3.6f,
                                        useCenter = false,
                                        topLeft = rc,
                                        size = androidx.compose.ui.geometry.Size(r * 2f, r * 2f),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }
                            }
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = palette.gold,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                // RAMBUT GRADIEN emas-sian di bawah header (header::after preview)
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.04f to Color.Transparent,
                                0.26f to palette.gold.copy(alpha = 0.50f),
                                0.52f to Color(0xFF94B2E0).copy(alpha = 0.22f),
                                0.80f to palette.accent.copy(alpha = 0.55f),
                                0.96f to Color.Transparent,
                                1f to Color.Transparent
                            )
                        )
                )
            }

            // ============ P43 — BAR MODEL AKTIF: model terkini dari PUSAT KENDALI + tombol TES ============
            ModelBar(
                palette = palette,
                model = settings.geminiModel.removePrefix("models/"),
                test = barTest,
                onTest = { vm.testActiveModel() },
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // ============ BANNER GALAT — masuk & keluar beranimasi ============
            AnimatedVisibility(
                visible = error != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    ErrorBanner(message = error.orEmpty(), onDismiss = { vm.dismissError() })
                    Spacer(Modifier.height(6.dp))
                }
            }

            // ============ PESAN — RIWAYAT PENUH, tiap bubble masuk ber-spring ============
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (setupNeeded) {
                    item { SetupCard(onOpenSettings = onOpenSettings) }
                }
                itemsIndexed(messages) { _, msg ->
                    BubbleEntrance(key = msg.timestamp) {
                        MessageBubble(
                            role = msg.role,
                            text = msg.text,
                            time = msgTimeFmt.format(Date(msg.timestamp))
                        )
                    }
                }
                if (state == AviState.THINKING) {
                    item {
                        // K22 — timeline aksi agen: pikir → aksi → hasil
                        if (agentEvents.isNotEmpty()) {
                            AgentTimeline(events = agentEvents, palette = palette)
                            Spacer(Modifier.height(6.dp))
                        }
                        BubbleEntrance(key = -1L) {
                            MessageBubble(role = "avi", text = streamingText, streaming = true)
                        }
                    }
                }
            }

            // P43 — teks parsial STT kini hidup DI DALAM bar hasil suara (dock)

            // ============ ZONA REAKTOR (P44: piringan hidup · orb — cincin
            //               teks telemetri DIHAPUS, panggung murni bentuk + aura) ============
            Box(Modifier.fillMaxWidth().height(232.dp)) {
                ReactorPlate(
                    palette = palette,
                    enabled = !settings.powerSave,
                    glowLevel = maxOf(rmsLevel, voiceLevel),
                    modifier = Modifier.matchParentSize()
                )
                ReactorOrb(
                    state = state,
                    micLevel = rmsLevel,
                    voiceLevel = voiceLevel,
                    palette = palette,
                    powerSave = settings.powerSave,
                    showVisor = false,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(160.dp),
                    onTap = { ensureMicThenToggle() }
                )
            }

            // ============ BANNER MODE LIVE ============
            AnimatedVisibility(
                visible = state == AviState.LIVE,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val livePulse = rememberInfiniteTransition(label = "liveBanner")
                val lp by livePulse.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
                    label = "lp"
                )
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
                        .border(1.dp, palette.danger.copy(alpha = 0.25f + 0.45f * lp), RoundedCornerShape(8.dp))
                        .padding(vertical = 6.dp)
                )
                Spacer(Modifier.height(6.dp))
            }

            // P43 — chip pintasan depan DIHAPUS (umpan Pak: depan bersih):
            //   perintah lokal tetap hidup di Deck → PINTASAN MARKAS,
            //   model terkini kini terbaca di BAR MODEL AKTIF di atas.

            // ============ DOCK P43 — BAR HASIL SUARA (vmic): suara lewat ORB,
            //   papan ketik via tekan-lama/ikon; LIVE pindah ke Deck ============
            var kbMode by remember { mutableStateOf(false) }
            var voiceSaid by remember { mutableStateOf("") }
            LaunchedEffect(partialText) { if (partialText.isNotBlank()) voiceSaid = partialText }
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(kbMode) { if (kbMode) focusRequester.requestFocus() }
            // HORIZON sian-emas di atas komposer (composer::before preview) — panggung bawah hidup
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.26f to palette.accent.copy(alpha = 0.42f),
                            0.70f to palette.gold.copy(alpha = 0.38f),
                            1f to Color.Transparent
                        )
                    )
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    palette.panel.copy(alpha = 0.92f),
                                    palette.panelLight.copy(alpha = 0.72f)
                                )
                            )
                        )
                        // CINCIN GRADIEN emas-sian (cmd::before / --gline preview)
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        palette.accent.copy(alpha = 0.50f),
                                        palette.accent.copy(alpha = 0.10f),
                                        Color(0xFF94B2E0).copy(alpha = 0.18f),
                                        palette.gold.copy(alpha = 0.45f)
                                    )
                                )
                            ),
                            RoundedCornerShape(22.dp)
                        )
                        .combinedClickable(
                            onClick = { if (!kbMode) kbMode = true },
                            onLongClick = { kbMode = true }
                        )
                        .padding(start = 6.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    // vmic — lingkaran mikrofon kecil; mengiup sian mengikuti suara
                    Box(
                        Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                val s = 1f + if (state == AviState.LISTENING) rmsLevel * 0.10f else 0f
                                scaleX = s
                                scaleY = s
                            }
                            .clip(CircleShape)
                            .background(
                                if (state == AviState.LISTENING) palette.accent.copy(alpha = 0.20f)
                                else palette.accent.copy(alpha = 0.10f)
                            )
                            .border(
                                1.dp,
                                if (state == AviState.LISTENING) palette.accent
                                else palette.accent.copy(alpha = 0.32f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Suara",
                            tint = if (state == AviState.LISTENING) palette.accent else palette.accent.copy(alpha = 0.85f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    if (kbMode) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = palette.text,
                                fontSize = 14.sp,
                                fontFamily = AviBody
                            ),
                            cursorBrush = SolidColor(palette.accent),
                            maxLines = 4,
                            decorationBox = { inner ->
                                Box {
                                    if (inputText.isEmpty()) {
                                        Text(
                                            "Ketik pesan ke AVI…",
                                            color = palette.textDim,
                                            fontSize = 13.sp,
                                            fontFamily = AviBody
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        val canSend = inputText.isNotBlank()
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (canSend) palette.gold else palette.panelLight)
                                .clickable(enabled = canSend) {
                                    kbMode = false
                                    vm.sendText(inputText.trim())
                                    inputText = ""
                                    voiceSaid = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Kirim",
                                tint = if (canSend) palette.bg else palette.textDim,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        val mirror = when {
                            state == AviState.LISTENING && partialText.isNotBlank() -> "\u201C$partialText\u201D"
                            state == AviState.LISTENING -> "Mendengar… silakan bicara, Pak"
                            voiceSaid.isNotBlank() -> voiceSaid
                            else -> "Hasil suara tampil di sini — ketuk ORB untuk bicara, Pak"
                        }
                        Text(
                            mirror,
                            color = when {
                                state == AviState.LISTENING -> palette.accent
                                voiceSaid.isNotBlank() -> palette.text
                                else -> palette.textDim
                            },
                            fontSize = 13.sp,
                            fontStyle = if (state == AviState.LISTENING) FontStyle.Italic else FontStyle.Normal,
                            fontFamily = AviBody,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Keyboard,
                            contentDescription = "Papan ketik",
                            tint = palette.textDim,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { kbMode = true }
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        // ============ K19 — DECK MODUL (panel modul kaya ala markas) ============
        AnimatedVisibility(
            visible = showDeck,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180))
        ) {
            DeckPanel(
                vm = vm,
                settings = settings,
                onDismiss = { showDeck = false },
                onOpenSettings = onOpenSettings
            )
        }
    }
}

// ============================================================
// LATAR PANGGUNG (P43 — porting setia preview .bgFx):
// 3 pendaran lembut (gA sian · gB emas · gD lantai sian) + pita
// aurora sian-emas bergoyang + 9 debu energi naik + 1 bintang
// jatuh + vignette. TANPA foto nebula, TANPA grid, TANPA bingkai
// — semua itu dipensiunkan di preview yang disetujui mata Pak.
// Satu Canvas — murah, hidup, dan MURNI sama seperti HTML.
// ============================================================

@Composable
private fun AuroraStage(palette: AviPalette, enabled: Boolean, modifier: Modifier = Modifier) {
    val time = remember { mutableStateOf(0f) }
    if (enabled) {
        LaunchedEffect(Unit) {
            var last = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                time.value += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }
    Canvas(modifier = modifier) { drawAuroraStage(time.value, palette) }
}

// 9 debu energi — parameter persis dari HTML preview (x, ukuran, warna,
// durasi, delay negatif, opacity, hanyutan horizontal)
private data class Mote(
    val x: Float, val s: Float, val cyan: Boolean, val mint: Boolean,
    val d: Float, val dl: Float, val o: Float, val dx: Float
)

private val MOTES = listOf(
    Mote(0.04f, 3.0f, true, false, 14f, -2f, 0.60f, 18f),
    Mote(0.14f, 2.2f, false, false, 18f, -7f, 0.52f, -14f),
    Mote(0.25f, 3.6f, false, true, 15f, -4f, 0.58f, 24f),
    Mote(0.37f, 2.4f, true, false, 19f, -11f, 0.55f, -18f),
    Mote(0.49f, 3.2f, false, false, 16f, -6f, 0.60f, 16f),
    Mote(0.61f, 2.2f, true, false, 21f, -13f, 0.50f, -12f),
    Mote(0.72f, 3.8f, false, true, 14.5f, -8f, 0.60f, 26f),
    Mote(0.84f, 2.6f, false, false, 17.5f, -3.5f, 0.58f, -20f),
    Mote(0.93f, 2.4f, true, false, 20f, -9.5f, 0.52f, 14f)
)

private fun DrawScope.drawAuroraStage(t: Float, palette: AviPalette) {
    val w = size.width
    val h = size.height
    val TAU = (2.0 * PI).toFloat()

    // ping-pong halus 0..1 (animation alternate CSS)
    fun ping(dur: Float): Float {
        val f = (t / dur) % 2f
        return if (f < 1f) f else 2f - f
    }
    fun soft(p: Float) = 0.5f - 0.5f * cos(p * PI.toFloat()) // ease-in-out

    // ---- gA: pendaran sian kiri-atas (driftA 17dtk) ----
    val pa = soft(ping(17f))
    val gaCx = w * 0.13f + 66f * pa * (w / 390f)
    val gaCy = h * 0.09f + 52f * pa * (w / 390f)
    val gaR = w * 0.29f * (1f + 0.18f * pa)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(palette.accent.copy(alpha = 0.26f), palette.accent.copy(alpha = 0.12f), Color.Transparent),
            center = Offset(gaCx, gaCy), radius = gaR
        ),
        radius = gaR, center = Offset(gaCx, gaCy)
    )
    // ---- gB: pendaran emas kanan-bawah (driftB 21dtk) ----
    val pb = soft(ping(21f))
    val gbCx = w * 0.87f - 60f * pb * (w / 390f)
    val gbCy = h * 0.92f - 46f * pb * (w / 390f)
    val gbR = w * 0.31f * (1f + 0.14f * pb)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(palette.gold.copy(alpha = 0.21f), palette.gold.copy(alpha = 0.09f), Color.Transparent),
            center = Offset(gbCx, gbCy), radius = gbR
        ),
        radius = gbR, center = Offset(gbCx, gbCy)
    )
    // ---- gD: pendaran lantai sian tengah-bawah (driftB 19dtk reverse) ----
    val pd = soft(ping(19f))
    val gdR = w * 0.36f * (1f + 0.14f * pd)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(palette.accent.copy(alpha = 0.23f), palette.accent.copy(alpha = 0.10f), Color.Transparent),
            center = Offset(w * 0.50f, h * 1.02f), radius = gdR
        ),
        radius = gdR, center = Offset(w * 0.50f, h * 1.02f)
    )

    // ---- AURORA: pita sian-emas bergoyang (16dtk, translateX ±4% + kemiringan) ----
    rotate(degrees = -4f + 8f * soft(ping(16f)), pivot = Offset(w * 0.5f, h * 0.22f)) {
        translate(left = w * (-0.04f + 0.08f * soft(ping(16f)))) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.14f to Color.Transparent,
                    0.40f to palette.accent.copy(alpha = 0.24f),
                    0.68f to palette.gold.copy(alpha = 0.17f),
                    0.88f to Color.Transparent,
                    1f to Color.Transparent
                ),
                topLeft = Offset(w * 0.06f, h * 0.09f),
                size = Size(w * 0.88f, h * 0.26f)
            )
        }
    }

    // ---- 9 DEBU ENERGI: naik dari bawah + pendaran + hanyut ----
    for (m in MOTES) {
        val cycF = ((t + m.dl) / m.d % 1f + 1f) % 1f
        val mx = m.x * w + m.dx * (w / 390f) * cycF
        val my = h * (1.02f - 1.06f * cycF)
        val mo = when {
            cycF < 0.09f -> m.o * 1.5f * (cycF / 0.09f)
            cycF < 0.58f -> m.o * 1.5f - (m.o * 0.38f) * ((cycF - 0.09f) / 0.49f)
            else -> m.o * 1.12f * (1f - (cycF - 0.58f) / 0.42f)
        }
        if (mo <= 0f) continue
        val col = when {
            m.cyan -> palette.accent
            m.mint -> Color(0xFF5EEAD4)
            else -> palette.gold
        }
        val r = m.s * (w / 390f)
        drawCircle(col.copy(alpha = mo * 0.25f), radius = r * 3f, center = Offset(mx, my))
        drawCircle(col.copy(alpha = mo * 0.45f), radius = r * 1.8f, center = Offset(mx, my))
        drawCircle(col.copy(alpha = mo), radius = r, center = Offset(mx, my))
    }

    // ---- 1 BINTANG JATUH: garis cahaya miring sapu cepat (10.5dtk, mulai 2.2dtk) ----
    val mp = ((t - 2.2f) / 10.5f % 1f + 1f) % 1f
    if (mp in 0.0f..0.16f) {
        val mo2 = when {
            mp < 0.04f -> mp / 0.04f * 0.9f
            mp < 0.16f -> 0.9f * (1f - (mp - 0.04f) / 0.12f)
            else -> 0f
        }
        val prog = (mp / 0.16f).coerceIn(0f, 1f)
        val len = 150f * (w / 390f)
        val mAng = 24f * PI.toFloat() / 180f
        val cxm = w * 0.13f + 280f * prog * (w / 390f) * cos(mAng)
        val cym = h * 0.08f + 280f * prog * (w / 390f) * sin(mAng)
        val tail = Offset(
            cxm - len * cos(mAng),
            cym - len * sin(mAng)
        )
        drawLine(
            brush = Brush.linearGradient(
                listOf(Color.Transparent, palette.accent.copy(alpha = 0.55f * mo2), Color(0xFFF0FDFF).copy(alpha = 0.95f * mo2))
            ),
            start = tail, end = Offset(cxm, cym),
            strokeWidth = 2f, cap = StrokeCap.Round
        )
    }

    // ---- VIGNETTE (inset shadow .48) — tepi gelap halus, pusat jernih ----
    drawRect(
        brush = Brush.radialGradient(
            0f to Color.Transparent,
            0.62f to Color.Transparent,
            0.88f to Color.Black.copy(alpha = 0.30f),
            1f to Color.Black.copy(alpha = 0.48f),
            center = Offset(w / 2f, h / 2f), radius = maxOf(w, h) * 0.72f
        ),
        topLeft = Offset.Zero, size = size
    )
}

/**
 * Piringan reaktor — porting #orbDock::before preview:
 * ALAS CAHAYA di bawah panggung orb — pendaran sian+emas bernapas
 * di atas lantai gelap. Bentuk orb TIDAK disentuh oleh lapisan ini.
 */
@Composable
private fun ReactorPlate(
    palette: AviPalette,
    enabled: Boolean,
    glowLevel: Float,
    modifier: Modifier = Modifier
) {
    val time = remember { mutableStateOf(0f) }
    if (enabled) {
        LaunchedEffect(Unit) {
            var last = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                time.value += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }
    Canvas(modifier = modifier) {
        val c = center
        val R = size.minDimension / 2f
        // denyut alas 5.4dtk (dockB preview: scale 1 → 1.13, opacity .85 → 1)
        val f = (time.value / 5.4f) % 2f
        val p = 0.5f - 0.5f * cos(f * PI.toFloat())
        val sc = 1f + 0.13f * p
        val breathA = 0.85f + 0.15f * p
        val cy = c.y + R * 0.10f
        // lantai gelap dulu (radial kedua CSS), lalu pendaran sian, lalu emas
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFF03070E).copy(alpha = 0.62f * breathA),
                    Color(0xFF03070E).copy(alpha = 0.36f * breathA),
                    Color.Transparent
                ),
                center = Offset(c.x, cy), radius = R * 1.42f * sc
            ),
            radius = R * 1.42f * sc, center = Offset(c.x, cy)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    palette.accent.copy(alpha = 0.17f * breathA + 0.08f * glowLevel),
                    palette.accent.copy(alpha = 0.07f * breathA),
                    Color.Transparent
                ),
                center = Offset(c.x, cy), radius = R * 1.05f * sc
            ),
            radius = R * 1.05f * sc, center = Offset(c.x, cy)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    palette.gold.copy(alpha = 0.08f * breathA),
                    Color.Transparent
                ),
                center = Offset(c.x, cy), radius = R * 0.72f * sc
            ),
            radius = R * 0.72f * sc, center = Offset(c.x, cy)
        )
    }
}

// ============================================================
// Animasi masuk bubble — spring naik + memudar + membesar
// ============================================================

@Composable
private fun BubbleEntrance(key: Long, content: @Composable () -> Unit) {
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        progress.animateTo(
            1f,
            spring(dampingRatio = 0.74f, stiffness = 380f)
        )
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progress.value.coerceIn(0f, 1f)
            translationY = (1f - progress.value) * 36f
            scaleX = 0.94f + 0.06f * progress.value
            scaleY = scaleX
        }
    ) { content() }
}

// ============================================================
// P43 — MARK header: mini-orb identik lambang preview — inti kaca
// gelap + cincin tick mesin + arc emas-sian berputar + hexagram
// ganda. KETUK → DECK MODUL (pintasan pindah dari header — depan
// tetap satu tombol Pengaturan, keputusan Pak).
// ============================================================

@Composable
private fun HeaderMark(palette: AviPalette, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "hMark")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "spin"
    )
    val spin2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing)),
        label = "spin2"
    )
    Canvas(modifier) {
        val c = center
        val R = size.minDimension / 2f
        val TAU = (2.0 * PI).toFloat()
        // inti kaca gelap
        drawCircle(color = palette.bg.copy(alpha = 0.9f), radius = R * 0.86f, center = c)
        drawCircle(
            color = palette.accent.copy(alpha = 0.35f),
            radius = R * 0.86f,
            center = c,
            style = Stroke(width = 1.dp.toPx())
        )
        // cincin tick mesin (24 gerigi halus, putar pelan)
        rotate(degrees = spin2 * 360f, pivot = c) {
            for (k in 0 until 24) {
                drawArc(
                    color = palette.textDim.copy(alpha = 0.5f),
                    startAngle = k * 15f,
                    sweepAngle = 4f,
                    useCenter = false,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        // arc emas-sian berputar berlawanan laju
        rotate(degrees = spin * 360f, pivot = c) {
            drawArc(
                color = palette.gold,
                startAngle = 0f,
                sweepAngle = 80f,
                useCenter = false,
                style = Stroke(width = 1.6.dp.toPx())
            )
            drawArc(
                color = palette.accent,
                startAngle = 180f,
                sweepAngle = 60f,
                useCenter = false,
                style = Stroke(width = 1.4.dp.toPx())
            )
        }
        // hexagram ganda — segitiga sian ke atas, emas ke bawah
        rotate(degrees = spin2 * 360f, pivot = c) {
            val r = R * 0.52f
            fun tri(base: Float, col: Color, w: Float) {
                val p = listOf(
                    Offset(
                        c.x + r * cos((base - 90f) * PI.toFloat() / 180f),
                        c.y + r * sin((base - 90f) * PI.toFloat() / 180f)
                    ),
                    Offset(
                        c.x + r * cos((base + 30f) * PI.toFloat() / 180f),
                        c.y + r * sin((base + 30f) * PI.toFloat() / 180f)
                    ),
                    Offset(
                        c.x + r * cos((base + 150f) * PI.toFloat() / 180f),
                        c.y + r * sin((base + 150f) * PI.toFloat() / 180f)
                    )
                )
                for (k in 0..2) {
                    drawLine(col, p[k], p[(k + 1) % 3], strokeWidth = w)
                }
            }
            tri(0f, palette.accent, 1.8.dp.toPx())
            tri(180f, palette.gold.copy(alpha = 0.9f), 1.4.dp.toPx())
        }
        // pip hidup di tepi — denyut seirama orb
        drawCircle(
            color = palette.accent,
            radius = 1.6.dp.toPx(),
            center = Offset(c.x + R * 0.66f, c.y - R * 0.66f),
            alpha = 0.55f + 0.45f * sin(spin * TAU)
        )
    }
}

// ============================================================
// P43 — BAR MODEL AKTIF: kaca tipis di bawah header — membaca
// model terkini dari PUSAT KENDALI (Pengaturan). TES = ping
// model yang SEDANG DIPAKAI (nyata — via ModelCatalog.testModel),
// latensi ditampilkan apa adanya; gagal tak dihaluskan.
// ============================================================

@Composable
private fun ModelBar(
    palette: AviPalette,
    model: String,
    test: AviViewModel.BarTest,
    onTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val testing = test.phase == 1
    val transition = rememberInfiniteTransition(label = "mDot")
    val beat by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (testing) 500 else 2400, easing = LinearEasing)
        ),
        label = "beat"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.panel.copy(alpha = 0.78f))
            .border(1.dp, palette.textDim.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            // PUNGGUNGAN EMAS di tepi kiri (modelbar::before preview) — gradien emas→transparan
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(palette.gold, palette.gold.copy(alpha = 0.05f)),
                        startY = 0f, endY = size.height
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(2.5.dp.toPx(), size.height)
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(palette.gold.copy(alpha = 0.35f), Color.Transparent),
                        startY = 0f, endY = size.height
                    ),
                    topLeft = Offset(-1.5.dp.toPx(), 0f),
                    size = Size(5.5.dp.toPx(), size.height)
                )
            }
            .padding(start = 13.dp, end = 6.dp, top = 7.dp, bottom = 7.dp)
    ) {
        // mdot — titik emas berdenyut DENGAN PENDARAN (box-shadow CSS)
        Box(
            Modifier
                .size(8.dp)
                .graphicsLayer { alpha = 0.35f + 0.65f * beat }
                .drawBehind {
                    val glowCol = when {
                        testing -> palette.accent
                        test.phase == 3 -> palette.danger
                        else -> palette.gold
                    }
                    drawCircle(glowCol.copy(alpha = 0.35f), radius = size.minDimension * 1.4f, center = center)
                    drawCircle(glowCol.copy(alpha = 0.22f), radius = size.minDimension * 2.4f, center = center)
                }
                .background(
                    when {
                        testing -> palette.accent
                        test.phase == 3 -> palette.danger
                        else -> palette.gold
                    },
                    CircleShape
                )
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "MODEL AKTIF · DARI PUSAT KENDALI",
                color = palette.textDim,
                fontSize = 7.5.sp,
                fontFamily = AviHud,
                letterSpacing = 2.2.sp
            )
            Text(
                model.ifBlank { "PILIH DI PUSAT KENDALI" }.uppercase(Locale.ROOT),
                color = if (model.isBlank()) palette.textDim else palette.gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AviHud,
                letterSpacing = 1.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        when {
            testing -> Text(
                "MENGUJI…",
                color = palette.accent,
                fontSize = 8.5.sp,
                fontFamily = AviHud,
                letterSpacing = 1.4.sp
            )
            test.phase == 2 -> Text(
                "TERHUBUNG · ${test.ms} MS",
                color = palette.goldSoft,
                fontSize = 8.5.sp,
                fontFamily = AviHud,
                letterSpacing = 1.2.sp
            )
            test.phase == 3 -> Text(
                test.message,
                color = palette.danger,
                fontSize = 8.5.sp,
                fontFamily = AviHud,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
        Spacer(Modifier.width(8.dp))
        // TES — tombol emas dengan SUDUT BIDIK (corner bracket ::before/::after preview)
        Box(Modifier.padding(3.dp)) {
            Text(
                text = if (testing) "…" else "TES",
                color = palette.gold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AviHud,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.gold.copy(alpha = 0.08f))
                    .border(1.dp, palette.gold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .drawBehind {
                        val s = 6.dp.toPx()
                        val w = 1.2.dp.toPx()
                        val col = palette.gold.copy(alpha = 0.60f)
                        // 4 sudut bidik emas — di tepi tombol
                        drawLine(col, Offset(0f, 0f), Offset(s, 0f), w)
                        drawLine(col, Offset(0f, 0f), Offset(0f, s), w)
                        drawLine(col, Offset(size.width, 0f), Offset(size.width - s, 0f), w)
                        drawLine(col, Offset(size.width, 0f), Offset(size.width, s), w)
                        drawLine(col, Offset(0f, size.height), Offset(s, size.height), w)
                        drawLine(col, Offset(0f, size.height), Offset(0f, size.height - s), w)
                        drawLine(col, Offset(size.width, size.height), Offset(size.width - s, size.height), w)
                        drawLine(col, Offset(size.width, size.height), Offset(size.width, size.height - s), w)
                    }
                    .clickable(enabled = !testing && model.isNotBlank()) { onTest() }
                    .padding(horizontal = 13.dp, vertical = 7.dp)
            )
        }
    }
}

// ============================================================
// K22 — TIMELINE MESIN AGEN: pikir → aksi → hasil.
// Satu keluarga dengan HUD: rail node + monospace + denyut
// saat AVI sedang menimbang langkah berikutnya.
// ============================================================

@Composable
private fun AgentTimeline(events: List<AgentEvent>, palette: AviPalette) {
    val pulse = rememberInfiniteTransition(label = "agentPulse")
    val glow by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "agentGlow"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.textDim.copy(alpha = 0.07f))
            .border(1.dp, palette.accent.copy(alpha = 0.3f * glow), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(7.dp)
                    .graphicsLayer { alpha = glow }
                    .background(palette.gold, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "MESIN AGEN",
                color = palette.gold,
                fontSize = 11.sp,
                fontFamily = AviHud,
                letterSpacing = 2.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        events.forEachIndexed { i, ev ->
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    when (ev) {
                        is AgentEvent.Berpikir -> "◆"
                        is AgentEvent.Aksi -> "⚙"
                        is AgentEvent.Hasil -> if (ev.ok) "✓" else "✗"
                    },
                    color = when (ev) {
                        is AgentEvent.Berpikir -> palette.accent.copy(alpha = glow)
                        is AgentEvent.Aksi -> palette.gold
                        is AgentEvent.Hasil -> if (ev.ok) palette.accent else palette.danger
                    },
                    fontSize = 13.sp,
                    fontFamily = AviHud,
                    modifier = Modifier.width(24.dp)
                )
                Column(Modifier.weight(1f)) {
                    when (ev) {
                        is AgentEvent.Berpikir -> Text(
                            "AVI berpikir…",
                            color = palette.accent.copy(alpha = 0.5f + glow * 0.5f),
                            fontSize = 12.sp,
                            fontFamily = AviBody
                        )
                        is AgentEvent.Aksi -> {
                            Text(
                                ev.label.uppercase(Locale.ROOT),
                                color = palette.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = AviHud,
                                letterSpacing = 1.sp
                            )
                            if (ev.argumen.isNotBlank()) {
                                Text(
                                    ev.argumen,
                                    color = palette.textDim,
                                    fontSize = 11.sp,
                                    fontFamily = AviHud,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        is AgentEvent.Hasil -> Text(
                            ev.pesan,
                            color = if (ev.ok) palette.textDim else palette.danger,
                            fontSize = 12.sp,
                            fontFamily = AviBody,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (i < events.lastIndex) Spacer(Modifier.height(7.dp))
        }
    }
}
