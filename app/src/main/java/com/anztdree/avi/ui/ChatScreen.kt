package com.anztdree.avi.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
// ============================================================

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
                        BubbleEntrance(key = -1L) {
                            MessageBubble(role = "avi", text = streamingText, streaming = true)
                        }
                    }
                }
            }

            // ============ TEKS PARSIAL STT — chip dengar ============
            if (partialText.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .background(palette.accent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "\u201C$partialText\u201D",
                        color = palette.accent,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = AviBody,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

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
                    showVisor = true,
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

            // ============ DOCK INPUT KACA — glow fokus, mic beriak, kirim beranimasi ============
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 4.dp)) {
                var focused by remember { mutableStateOf(false) }
                val focusGlow by animateFloatAsState(
                    targetValue = if (focused) 1f else 0.35f,
                    animationSpec = tween(280),
                    label = "focusGlow"
                )
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
                            palette.accent.copy(alpha = 0.22f + 0.5f * focusGlow),
                            RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp)
                            .onFocusChanged { focused = it.isFocused },
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
                }
                Spacer(Modifier.width(6.dp))
                PulseButton(
                    icon = Icons.Default.Podcasts,
                    contentDescription = "Mode live",
                    active = state == AviState.LIVE,
                    bg = if (state == AviState.LIVE) palette.danger else palette.panelLight,
                    tint = if (state == AviState.LIVE) Color.White else palette.textDim,
                    onClick = { if (state == AviState.LIVE) vm.stopLive() else vm.startLive() }
                )
                Spacer(Modifier.width(6.dp))
                PulseButton(
                    icon = if (state == AviState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Mikrofon",
                    active = state == AviState.LISTENING,
                    level = if (state == AviState.LISTENING) rmsLevel else 0f,
                    bg = if (state == AviState.LISTENING) palette.accent else palette.panelLight,
                    tint = if (state == AviState.LISTENING) palette.bg else palette.accent,
                    onClick = { ensureMicThenToggle() }
                )
                Spacer(Modifier.width(6.dp))
                val canSend = inputText.isNotBlank()
                val sendScale by animateFloatAsState(if (canSend) 1f else 0.86f, label = "sendScale")
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = sendScale
                            scaleY = sendScale
                            alpha = if (canSend) 1f else 0.45f
                        }
                        .clip(CircleShape)
                        .background(palette.gold)
                        .clickable(enabled = canSend) {
                            vm.sendText(inputText.trim())
                            inputText = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Kirim",
                        tint = palette.bg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
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
            alpha = 0.48f
        )
        // vignette bernafas — teks selalu terbaca, latar tetap berdenyut
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 0.55f + 0.14f * sin(breathe * PI.toFloat()) }
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.bg.copy(alpha = 0.42f),
                            palette.bg.copy(alpha = 0.70f),
                            palette.bg.copy(alpha = 0.86f)
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
                    alpha = 0.10f * tw
                )
            }
            drawCircle(
                color = col,
                radius = s.size.dp.toPx(),
                center = Offset(x, y),
                alpha = (0.22f + 0.45f * s.depth) * (0.45f + 0.55f * tw)
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
// Tombol bulat dengan cincin denyut saat aktif + denyut level mic
// ============================================================

@Composable
private fun PulseButton(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean,
    bg: Color,
    tint: Color,
    level: Float = 0f,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "pulseBtn")
    val p by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "p"
    )
    Box(contentAlignment = Alignment.Center) {
        if (active) {
            // dua cincin memuai bergantian
            val p2 = (p + 0.5f) % 1f
            Box(
                Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = 1f + p * 0.75f
                        scaleY = 1f + p * 0.75f
                        alpha = (1f - p) * 0.55f
                    }
                    .border(2.dp, bg.copy(alpha = 0.7f), CircleShape)
            )
            Box(
                Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = 1f + p2 * 0.75f
                        scaleY = 1f + p2 * 0.75f
                        alpha = (1f - p2) * 0.55f
                    }
                    .border(2.dp, bg.copy(alpha = 0.7f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    val s = 1f + level.coerceIn(0f, 1f) * 0.09f
                    scaleX = s
                    scaleY = s
                }
                .clip(CircleShape)
                .background(bg)
                .border(1.dp, tint.copy(alpha = 0.4f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(21.dp))
        }
    }
}
