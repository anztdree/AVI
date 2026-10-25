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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anztdree.avi.AviState
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.ui.theme.AviAviBorder
import com.anztdree.avi.ui.theme.AviBg
import com.anztdree.avi.ui.theme.AviCyan
import com.anztdree.avi.ui.theme.AviCyanSoft
import com.anztdree.avi.ui.theme.AviDanger
import com.anztdree.avi.ui.theme.AviGold
import com.anztdree.avi.ui.theme.AviGoldSoft
import com.anztdree.avi.ui.theme.AviPanel
import com.anztdree.avi.ui.theme.AviPanelLight
import com.anztdree.avi.ui.theme.AviText
import com.anztdree.avi.ui.theme.AviTextDim

@Composable
fun ChatScreen(
    vm: AviViewModel,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val messages by vm.messages.collectAsState()
    val state by vm.state.collectAsState()
    val streamingText by vm.streamingText.collectAsState()
    val partialText by vm.partialText.collectAsState()
    val rmsLevel by vm.rmsLevel.collectAsState()
    val error by vm.error.collectAsState()
    val settings by vm.settings.collectAsState()

    var inputText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.onMicPermissionGranted()
        else vm.dismissError() // biarkan error izin muncul dari toggleMic? tampilkan manual:
    }

    val listState = rememberLazyListState()
    val itemCount = messages.size + if (state == AviState.THINKING) 1 else 0
    LaunchedEffect(itemCount, streamingText) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    val statusLabel = when (state) {
        AviState.IDLE -> "SIAGA"
        AviState.LISTENING -> "MENDENGARKAN"
        AviState.THINKING -> "BERPIKIR"
        AviState.SPEAKING -> "BERBICARA"
    }
    val statusColor = when (state) {
        AviState.IDLE -> AviCyan
        AviState.LISTENING -> AviCyanSoft
        AviState.THINKING -> AviGold
        AviState.SPEAKING -> AviGoldSoft
    }
    val brainConfigured = when (settings.provider) {
        AiProvider.GEMINI -> settings.geminiKey.isNotBlank()
        AiProvider.OPENAI_COMPAT -> settings.ocKey.isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AviBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ===== BAR ATAS =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "AVI",
                color = AviText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.width(10.dp))
            StatusChip("● $statusLabel", statusColor)
            Spacer(Modifier.width(8.dp))
            TopBarChip(
                label = if (brainConfigured)
                    (if (settings.provider == AiProvider.GEMINI) "GEMINI" else "CUSTOM AI")
                else "SETUP PERLU",
                color = if (brainConfigured) AviCyan else AviGold,
                onClick = onOpenSettings
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                vm.updateSettings { it.copy(autoSpeak = !it.autoSpeak) }
            }) {
                Icon(
                    imageVector = if (settings.autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Suara balasan ${if (settings.autoSpeak) "aktif" else "mati"}",
                    tint = if (settings.autoSpeak) AviCyan else AviTextDim
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Pengaturan", tint = AviTextDim)
            }
        }

        error?.let { ErrorBanner(message = it, onDismiss = { vm.dismissError() }) }

        // ===== DAFTAR PESAN =====
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!brainConfigured) {
                item { SetupCard(onOpenSettings = onOpenSettings) }
            }
            messages.forEachIndexed { idx, m ->
                item(key = "${m.timestamp}-$idx") {
                    MessageBubble(role = m.role, text = m.text, imageUrl = m.imageUrl)
                }
            }
            if (state == AviState.THINKING) {
                item {
                    MessageBubble(
                        role = "avi",
                        text = streamingText.ifBlank { "..." },
                        imageUrl = null,
                        streaming = true
                    )
                }
            }
        }

        // ===== REAKTOR =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            ReactorOrb(
                state = state,
                rms = rmsLevel,
                modifier = Modifier.size(150.dp)
            )
            Text(
                text = statusLabel,
                color = statusColor.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 0.dp)
            )
        }

        // ===== TEKS PARSIAL SUARA =====
        if (partialText.isNotBlank()) {
            Text(
                text = "“$partialText”",
                color = AviCyanSoft,
                fontSize = 13.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(6.dp))
        }

        // ===== BILAH INPUT =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AviPanel.copy(alpha = 0.6f))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                placeholder = {
                    Text("Ketik pesan ke AVI…", color = AviTextDim, fontSize = 14.sp)
                },
                enabled = state != AviState.THINKING,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AviCyan.copy(alpha = 0.6f),
                    unfocusedBorderColor = AviAviBorder,
                    focusedTextColor = AviText,
                    unfocusedTextColor = AviText,
                    cursorColor = AviCyan
                ),
                maxLines = 2
            )
            Spacer(Modifier.width(8.dp))

            // Tombol Mikrofon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        when (state) {
                            AviState.LISTENING -> AviDanger.copy(alpha = 0.25f)
                            else -> AviPanelLight
                        }
                    )
                    .border(
                        width = if (state == AviState.LISTENING) 2.dp else 1.dp,
                        color = when (state) {
                            AviState.LISTENING -> AviDanger
                            AviState.IDLE -> AviGold.copy(alpha = 0.7f)
                            else -> AviTextDim.copy(alpha = 0.4f)
                        },
                        shape = CircleShape
                    )
                    .clickable(enabled = state != AviState.THINKING) {
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) vm.toggleMic()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state == AviState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (state == AviState.LISTENING) "Hentikan mendengarkan" else "Bicara ke AVI",
                    tint = if (state == AviState.LISTENING) AviDanger else AviGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Tombol Kirim
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AviGold.copy(alpha = if (state == AviState.THINKING) 0.25f else 0.9f))
                    .clickable(enabled = state != AviState.THINKING) {
                        vm.sendText(inputText)
                        inputText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Kirim",
                    tint = Color(0xFF0A0E14),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
