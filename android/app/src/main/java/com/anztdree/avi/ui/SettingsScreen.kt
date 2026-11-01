package com.anztdree.avi.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.ui.theme.AviAviBorder
import com.anztdree.avi.ui.theme.AviBg
import com.anztdree.avi.ui.theme.AviCyan
import com.anztdree.avi.ui.theme.AviDanger
import com.anztdree.avi.ui.theme.AviGold
import com.anztdree.avi.ui.theme.AviPanel
import com.anztdree.avi.ui.theme.AviPanelLight
import com.anztdree.avi.ui.theme.AviText
import com.anztdree.avi.ui.theme.AviTextDim

@Composable
fun SettingsScreen(vm: AviViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current
    var showKey by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AviCyan.copy(alpha = 0.6f),
        unfocusedBorderColor = AviAviBorder,
        focusedTextColor = AviText,
        unfocusedTextColor = AviText,
        cursorColor = AviCyan
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AviBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = AviText)
            }
            Text("Pengaturan AVI", color = AviText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))

        // ===== OTAK AI =====
        SectionTitle("🧠 Otak AI")
        ProviderOption(
            title = "Google Gemini (gratis, direkomendasikan)",
            desc = "Cepat, pintar, bisa cari info web. Butuh API key gratis dari Google.",
            selected = settings.provider == AiProvider.GEMINI,
            onClick = { vm.updateSettings { it.copy(provider = AiProvider.GEMINI) } }
        )
        ProviderOption(
            title = "API Kompatibel OpenAI",
            desc = "Untuk OpenRouter / Groq / OpenAI / server sendiri.",
            selected = settings.provider == AiProvider.OPENAI_COMPAT,
            onClick = { vm.updateSettings { it.copy(provider = AiProvider.OPENAI_COMPAT) } }
        )
        Spacer(Modifier.height(10.dp))

        if (settings.provider == AiProvider.GEMINI) {
            OutlinedTextField(
                value = settings.geminiKey,
                onValueChange = { v -> vm.updateSettings { it.copy(geminiKey = v.trim()) } },
                label = { Text("API Key Gemini", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = AviTextDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Cara dapat key gratis:\n" +
                    "1. Buka aistudio.google.com/apikey\n" +
                    "2. Login Google → Create API key\n" +
                    "3. Tempel kuncinya di sini",
                color = AviTextDim, fontSize = 12.sp, lineHeight = 17.sp
            )
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                        )
                    } catch (_: Exception) {
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AviGold, contentColor = Color(0xFF0A0E14))
            ) {
                Icon(Icons.Default.OpenInNew, null, Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Buka Google AI Studio", fontSize = 12.sp)
            }
        } else {
            OutlinedTextField(
                value = settings.ocBaseUrl,
                onValueChange = { v -> vm.updateSettings { it.copy(ocBaseUrl = v.trim()) } },
                label = { Text("Base URL API", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = settings.ocKey,
                onValueChange = { v -> vm.updateSettings { it.copy(ocKey = v.trim()) } },
                label = { Text("API Key", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = AviTextDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = settings.ocModel,
                onValueChange = { v -> vm.updateSettings { it.copy(ocModel = v.trim()) } },
                label = { Text("Nama Model", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors
            )
        }

        Spacer(Modifier.height(18.dp))

        // ===== SUARA =====
        SectionTitle("🔊 Suara")
        ToggleRow(
            label = "AVI menjawab dengan suara",
            checked = settings.autoSpeak,
            onChange = { v -> vm.updateSettings { it.copy(autoSpeak = v) } }
        )
        ToggleRow(
            label = "Lanjut mendengarkan otomatis (mode percakapan)",
            checked = settings.autoListen,
            onChange = { v -> vm.updateSettings { it.copy(autoListen = v) } }
        )
        Spacer(Modifier.height(8.dp))
        Text("Kecepatan bicara: ${"%.1f".format(settings.speechRate)}x", color = AviTextDim, fontSize = 12.sp)
        Slider(
            value = settings.speechRate,
            onValueChange = { v -> vm.updateSettings { it.copy(speechRate = v) } },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(thumbColor = AviGold, activeTrackColor = AviGold)
        )
        Text("Nada bicara: ${"%.1f".format(settings.pitch)}", color = AviTextDim, fontSize = 12.sp)
        Slider(
            value = settings.pitch,
            onValueChange = { v -> vm.updateSettings { it.copy(pitch = v) } },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(thumbColor = AviGold, activeTrackColor = AviGold)
        )

        Spacer(Modifier.height(18.dp))

        // ===== KEMAMPUAN =====
        SectionTitle("⚡ Kemampuan")
        ToggleRow(
            label = "Pencarian web untuk info terkini (Gemini)",
            checked = settings.webSearch,
            onChange = { v -> vm.updateSettings { it.copy(webSearch = v) } }
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = settings.userName,
            onValueChange = { v -> vm.updateSettings { it.copy(userName = v.ifBlank { "Pak" }) } },
            label = { Text("AVI memanggil saya…", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors
        )

        Spacer(Modifier.height(18.dp))

        // ===== DATA =====
        SectionTitle("🗄️ Data")
        Button(
            onClick = { vm.clearHistory() },
            colors = ButtonDefaults.buttonColors(containerColor = AviDanger.copy(alpha = 0.85f), contentColor = Color(0xFF0A0E14))
        ) {
            Text("Hapus Riwayat Percakapan", fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Semua pengaturan & riwayat tersimpan lokal di HP Anda. API key hanya dikirim ke penyedia AI yang Anda pilih.",
            color = AviTextDim,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = AviGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ProviderOption(title: String, desc: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AviPanel else AviPanel.copy(alpha = 0.4f))
            .border(
                1.dp,
                if (selected) AviCyan.copy(alpha = 0.6f) else AviAviBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AviCyan)
        )
        Spacer(Modifier.width(6.dp))
        Column {
            Text(title, color = AviText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = AviTextDim, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AviText, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AviGold,
                checkedTrackColor = AviGold.copy(alpha = 0.4f)
            )
        )
    }
}
