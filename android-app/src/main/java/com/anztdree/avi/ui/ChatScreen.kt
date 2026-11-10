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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.anztdree.avi.R
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
import kotlinx.coroutines.delay
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
    val diagnostics by vm.diagnostics.collectAsState()
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

    val listState = rememberLazyListState()
    // P43 — hasil tombol TES di bar MODEL AKTIF
    val barTest by vm.barTest.collectAsState()
    LaunchedEffect(messages.size, streamingText) {
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) listState.scrollToItem(total - 1)
    }

    Box(Modifier.fillMaxSize().background(palette.bg)) {
        // ---- K13 — nebula HD yang HIDUP: ken-burns pelan + vignette bernafas ----
        if (!settings.powerSave) {
            AnimatedNebula(
                resId = when (settings.themeMark) {
                    "MK2" -> R.drawable.bg_nebula_mk2
                    "MK3" -> R.drawable.bg_nebula_mk3
                    else -> R.drawable.bg_nebula
                },
                palette = palette,
                modifier = Modifier.matchParentSize()
            )
        }
        // ---- medan bintang berkelip berlapis kedalaman ----
        StarfieldCanvas(
            palette = palette,
            enabled = settings.parallaxEnabled && !settings.powerSave,
            modifier = Modifier.matchParentSize()
        )
        HexGridBackground(
            palette = palette,
            enabled = settings.parallaxEnabled && !settings.powerSave,
            modifier = Modifier.matchParentSize()
        )
        // ---- K19 — bingkai HUD kelas Jarvis: bracket sudut + rel + sapuan hologram ----
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
                .imePadding()
                .padding(horizontal = 14.dp)
        ) {
            // ============ P43 — BAR ATAS BERSIH: mark + brand, pojok kanan CUMA 1 tombol PENGATURAN
            //   (umpan Pak: chip SIAGA & jam DIHAPUS dari depan — jam tetap hidup di telemetri orbit;
            //    Deck pindah ke ketukan MARK logo — depan tetap bersih) ============
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                HeaderMark(
                    palette = palette,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { showDeck = true }
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "AVI",
                        color = palette.text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = AviDisplay,
                        letterSpacing = 4.sp,
                        lineHeight = 16.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = palette.accent.copy(alpha = 0.85f),
                                blurRadius = 16f
                            )
                        )
                    )
                    Text(
                        "ASISTEN PRIBADI",
                        color = palette.textDim,
                        fontSize = 7.5.sp,
                        fontFamily = AviHud,
                        letterSpacing = 3.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(38.dp)) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = palette.textDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                        MessageBubble(role = msg.role, text = msg.text)
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

            // ============ ZONA REAKTOR (piringan hidup · halo telemetri · orb) ============
            Box(Modifier.fillMaxWidth().height(232.dp)) {
                ReactorPlate(
                    palette = palette,
                    enabled = !settings.powerSave,
                    glowLevel = maxOf(rmsLevel, voiceLevel),
                    modifier = Modifier.matchParentSize()
                )
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
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 4.dp)) {
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
                        .border(
                            1.dp,
                            if (state == AviState.LISTENING) palette.accent.copy(alpha = 0.55f)
                            else palette.accent.copy(alpha = 0.22f),
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
                                else palette.panelLight
                            )
                            .border(
                                1.dp,
                                if (state == AviState.LISTENING) palette.accent
                                else palette.accent.copy(alpha = 0.30f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Suara",
                            tint = if (state == AviState.LISTENING) palette.accent else palette.textDim,
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
// Lapisan latar sinematik
// ============================================================

/**
 * Nebula HD yang TIDAK PERNAH diam: ken-burns (zoom + geser sinusoidal
 * 26 detik) via graphicsLayer murni GPU + vignette yang bernafas.
 * K13 — jawaban atas keluhan "potongan gambar yang ditempel".
 */
@Composable
private fun AnimatedNebula(resId: Int, palette: AviPalette, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "nebula")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "drift"
    )
    val breathe by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "breathe"
    )
    Box(modifier) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val TAU = (2.0 * PI).toFloat()
                    scaleX = 1.08f + 0.03f * sin(drift * TAU)
                    scaleY = scaleX
                    translationX = size.width * 0.014f * cos(drift * TAU)
                    translationY = size.height * 0.018f * sin(drift * TAU * 0.7f)
                },
            contentScale = ContentScale.Crop,
            alpha = 0.62f   // P42: latar diperjelas (umpan Pak: animasi kurang jelas)
        )
        // vignette bernafas — teks selalu terbaca, latar tetap berdenyut (P42: diringankan)
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 0.55f + 0.14f * sin(breathe * PI.toFloat()) }
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.bg.copy(alpha = 0.32f),
                            palette.bg.copy(alpha = 0.56f),
                            palette.bg.copy(alpha = 0.78f)
                        )
                    )
                )
        )
    }
}

/** Satu bintang: posisi ternormalisasi, kedalaman (paralaks), fase kelip */
private data class Star(
    val x: Float,
    val y: Float,
    val depth: Float,
    val size: Float,
    val phase: Float,
    val gold: Boolean,
    val big: Boolean
)

/**
 * Medan bintang HD — 90 bintang 3 lapis kedalaman, hanyut turun pelan
 * (paralaks) + kelip sinus fase acak. Murni Canvas tanpa alokasi per frame.
 */
@Composable
private fun StarfieldCanvas(palette: AviPalette, enabled: Boolean, modifier: Modifier = Modifier) {
    if (!enabled) return
    val stars = remember {
        val rnd = kotlin.random.Random(2077)
        List(96) {
            val big = rnd.nextFloat() < 0.07f
            Star(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                depth = 0.25f + rnd.nextFloat() * 0.75f,
                size = if (big) 1.8f + rnd.nextFloat() * 1.2f else 0.7f + rnd.nextFloat() * 1.0f,
                phase = rnd.nextFloat() * 2f * PI.toFloat(),
                gold = rnd.nextFloat() < 0.28f,
                big = big
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "stars")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "t"
    )
    Canvas(modifier) {
        val TAU = (2.0 * PI).toFloat()
        val w = size.width
        val h = size.height
        for (s in stars) {
            val y = ((s.y + t * (0.04f + 0.11f * s.depth)) % 1f) * h
            val x = s.x * w
            val tw = 0.5f + 0.5f * sin(t * TAU * 3f + s.phase)
            val col = if (s.gold) palette.gold else palette.accentSoft
            if (s.big) {
                // halo lembut bintang besar
                drawCircle(
                    color = col,
                    radius = s.size.dp.toPx() * 3.2f,
                    center = Offset(x, y),
                    alpha = 0.15f * tw
                )
            }
            drawCircle(
                color = col,
                radius = s.size.dp.toPx(),
                center = Offset(x, y),
                alpha = (0.30f + 0.50f * s.depth) * (0.45f + 0.55f * tw)
            )
        }
    }
}

/**
 * Piringan reaktor — menggantikan tempelan gambar grid statis:
 * glow konsentris bernafas + dua cincin putus-putus berlawanan arah
 * + titik emas kardinal, mengikuti level suara.
 */
@Composable
private fun ReactorPlate(
    palette: AviPalette,
    enabled: Boolean,
    glowLevel: Float,
    modifier: Modifier = Modifier
) {
    if (!enabled) return
    val transition = rememberInfiniteTransition(label = "plate")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing)),
        label = "t"
    )
    Canvas(modifier = modifier) {
        val c = center
        val R = size.minDimension / 2f
        val TAU = (2.0 * PI).toFloat()

        // glow konsentris bernafas — memberi kedalaman di balik orb
        val breathe = 0.5f + 0.5f * sin(t * TAU)
        for (k in 0..2) {
            drawCircle(
                color = palette.accent.copy(alpha = (0.030f + 0.018f * k) * (0.75f + 0.5f * breathe + glowLevel * 0.6f)),
                radius = R * (0.44f + 0.11f * k),
                center = c
            )
        }
        // cincin putus berputar searah — di celah antara orb dan pita telemetri
        rotate(degrees = t * 360f, pivot = c) {
            for (seg in 0 until 12) {
                drawArc(
                    color = palette.accent.copy(alpha = 0.15f),
                    startAngle = seg * 30f,
                    sweepAngle = 17f,
                    useCenter = false,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        // cincin putus berlawanan arah
        rotate(degrees = -t * 250f, pivot = c) {
            for (seg in 0 until 8) {
                drawArc(
                    color = palette.gold.copy(alpha = 0.13f),
                    startAngle = seg * 45f + 22f,
                    sweepAngle = 24f,
                    useCenter = false,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        // titik emas kardinal yang berkedip
        for (k in 0 until 4) {
            val rad = (k * 90f + t * 360f * 0.25f) * PI.toFloat() / 180f
            val r = R * 0.685f
            drawCircle(
                color = palette.gold,
                radius = 1.6.dp.toPx(),
                center = Offset(c.x + r * cos(rad), c.y + r * sin(rad)),
                alpha = 0.35f + 0.35f * sin(t * TAU * 2f + k)
            )
        }
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
            .border(1.dp, palette.accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(start = 13.dp, end = 6.dp, top = 7.dp, bottom = 7.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .graphicsLayer { alpha = 0.35f + 0.65f * beat }
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
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AviHud,
                letterSpacing = 1.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        when {
            testing -> Text(
                "MENGUJI…",
                color = palette.accent,
                fontSize = 9.sp,
                fontFamily = AviHud,
                letterSpacing = 1.4.sp
            )
            test.phase == 2 -> Text(
                "TERHUBUNG · ${test.ms} MS",
                color = palette.accent,
                fontSize = 9.sp,
                fontFamily = AviHud,
                letterSpacing = 1.2.sp
            )
            test.phase == 3 -> Text(
                test.message,
                color = palette.danger,
                fontSize = 9.sp,
                fontFamily = AviHud,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (testing) "…" else "TES",
            color = palette.gold,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AviHud,
            letterSpacing = 2.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(palette.gold.copy(alpha = 0.10f))
                .border(1.dp, palette.gold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .clickable(enabled = !testing && model.isNotBlank()) { onTest() }
                .padding(horizontal = 13.dp, vertical = 7.dp)
        )
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
