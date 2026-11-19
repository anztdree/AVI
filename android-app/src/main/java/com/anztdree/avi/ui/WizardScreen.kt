package com.anztdree.avi.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anztdree.avi.AviViewModel
import com.anztdree.avi.R
import com.anztdree.avi.data.AiProvider
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.ui.theme.AviBody
import com.anztdree.avi.ui.theme.AviCode
import com.anztdree.avi.ui.theme.AviDisplay
import com.anztdree.avi.ui.theme.AviHead
import com.anztdree.avi.ui.theme.AviHud
import com.anztdree.avi.ui.theme.AviPalette
import com.anztdree.avi.ui.theme.LocalAviPalette
import kotlinx.coroutines.delay

/**
 * P50 — WIZARD PERTAMA KALI INSTALL (umpan Pak: "saat aplikasi pertama kali
 * di install harusnya muncul wizard kan? dan isi wizard itu mohon cari dari
 * berbagai sumber referensi").
 *
 * Isi wizard dirancang dari referensi (diriset sebelum ditulis):
 *  - NN/g (education-immersion-engagement)  : sapa → kenali → siap.
 *  - UserPilot (progressive + benefit first): 1 layar 1 aksi, manfaat dulu
 *    sebelum teknis, progres terlihat, selalu bisa di-skip.
 *  - Instabug (a-ha moment + personalisasi + streamline signup) : langkah
 *    nama panggilan, API key dibuat seminimal mungkin, boleh ditunda.
 *  - Material/Android permission priming : IZIN mikrofon minta dengan
 *    penjelasan dulu (in-context), BUKAN dialog sistem begitu buka.
 *  - developer.android.com splash screen : identitas brand sejak jendela
 *    pertama (emblem AVI) — splash sistem sudah memakai emblem yang sama.
 *
 * 5 LANGKAH (dengan indikator progres + lewati di tiap langkah):
 *  0 SELAMAT DATANG  — siapa AVI & apa manfaatnya (education/benefit)
 *  1 KALIBRASI PEMILIK — nama panggilan (typing dibai: draft lokal, tak
 *     melawan jari) + catatan pemilik ownerNote (P51, opsional)
 *  2 IZIN SUARA      — priming mikrofon lalu dialog sistem (RECORD_AUDIO)
 *  3 OTAK AI         — Gemini (multi kunci) / NVIDIA NIM + muat & pilih model
 *  4 SIAP            — ringkasan + AKTIFKAN AVI (langsung mulai mendengar)
 */
@Composable
fun WizardScreen(vm: AviViewModel) {
    val settings by vm.settings.collectAsState()
    val palette = LocalAviPalette.current
    val context = LocalContext.current

    var step by rememberSaveable { mutableStateOf(0) }

    // P51 — KALIBRASI PEMILIK: draft diangkat ke level wizard supaya bisa
    // di-flush saat pindah langkah — tidak ada ketikan yang hilang.
    // (Umpan Pak: "Buat ganti nama di wizard aja susah!! jangan lu bikin
    // ribet lah" — dulu TIAP HURUF langsung trim + ifBlank("Pak") + tulis
    // DataStore: spasi ditelan, hapus semua = "Pak" balik sendiri.)
    var namaDraft by rememberSaveable { mutableStateOf(settings.userName) }
    var noteDraft by rememberSaveable { mutableStateOf(settings.ownerNote) }
    fun flushKalibrasi() {
        vm.updateSettings {
            it.copy(
                userName = namaDraft.trim().take(24).ifBlank { "Pak" },
                ownerNote = noteDraft.trim().take(400)
            )
        }
    }
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> micGranted = granted }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        // latar sinematik sama dengan splash/boot — identitas satu keluarga
        Image(
            painter = painterResource(R.drawable.bg_splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.55f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(palette.bg.copy(alpha = 0.30f), palette.bg.copy(alpha = 0.88f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 18.dp)
        ) {
            // ---- bar atas: balik + langkah + titik progres ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) {
                    IconButton(onClick = { step -= 1 }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Langkah sebelumnya",
                            tint = palette.textDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                } else {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(palette.panelLight.copy(alpha = 0.5f))
                            .border(1.dp, palette.gold.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.splash_emblem),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "PENGATURAN AWAL",
                    color = palette.textDim,
                    fontSize = 10.sp,
                    fontFamily = AviHud,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "LANGKAH ${step + 1}/5",
                    color = palette.accent,
                    fontSize = 10.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(5) { i ->
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        i == step -> palette.accent
                                        i < step -> palette.gold
                                        else -> palette.panelLight
                                    }
                                )
                        )
                    }
                }
            }

            // ---- isi langkah (scrollable) ----
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 18.dp)
            ) {
                when (step) {
                    0 -> WizardWelcome(palette, vm)
                    1 -> WizardKalibrasi(vm, palette, namaDraft, noteDraft,
                        onNama = { namaDraft = it }, onNote = { noteDraft = it })
                    2 -> WizardIzin(palette, micGranted, vm) { permLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    3 -> WizardOtak(vm, settings, palette)
                    4 -> WizardSiap(vm, settings, palette, micGranted)
                }
            }

            // ---- bar bawah: aksi utama + lewati ----
            Spacer(Modifier.height(10.dp))
            WizardFooter(step, palette, micGranted,
                onNext = { flushKalibrasi(); step += 1 },
                onLewati = { flushKalibrasi(); step += 1 },
                onSelesai = { flushKalibrasi(); vm.completeWizard(micGranted) }
            )
        }
    }
}

// ============================================================
// Footer — tombol utama per langkah + LEWATI
// ============================================================

@Composable
private fun WizardFooter(
    step: Int,
    palette: AviPalette,
    micGranted: Boolean,
    onNext: () -> Unit,
    onLewati: () -> Unit,
    onSelesai: () -> Unit
) {
    val label = when (step) {
        0 -> "MULAI"
        1 -> "LANJUT"
        2 -> if (micGranted) "LANJUT — MIKROFON SIAP" else "LANJUT"
        3 -> "LANJUT"
        else -> "AKTIFKAN AVI"
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        if (step == 3) {
            Text(
                "Bisa dilewati — semuanya juga bisa diatur ulang di PUSAT KENDALI kapan pun.",
                color = palette.textDim.copy(alpha = 0.85f),
                fontSize = 9.5.sp,
                fontFamily = AviHud,
                lineHeight = 13.sp
            )
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = { if (step == 4) onSelesai() else onNext() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.gold,
                contentColor = Color(0xFF161006)
            )
        ) {
            Icon(
                Icons.Default.RocketLaunch,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                fontSize = 14.sp,
                fontFamily = AviHead,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
        if (step < 4) {
            TextButtonHud(
                text = "LEWATI DULU — AVI TETAP TERPASANG",
                palette = palette,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                onClick = onLewati
            )
        }
    }
}

@Composable
private fun TextButtonHud(
    text: String,
    palette: AviPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = palette.textDim,
            fontSize = 10.sp,
            fontFamily = AviHud,
            letterSpacing = 1.2.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================
// Kepala seksi — kicker + judul + deskripsi
// ============================================================

@Composable
private fun WizardHeader(
    kicker: String,
    judul: String,
    deskripsi: String,
    palette: AviPalette
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(14.dp).background(palette.gold))
        Spacer(Modifier.width(8.dp))
        Text(
            kicker,
            color = palette.gold,
            fontSize = 10.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.5.sp
        )
    }
    Spacer(Modifier.height(10.dp))
    Text(
        judul,
        color = palette.text,
        fontSize = 27.sp,
        fontFamily = AviDisplay,
        fontWeight = FontWeight.Bold,
        lineHeight = 31.sp
    )
    Spacer(Modifier.height(8.dp))
    Text(
        deskripsi,
        color = palette.textDim,
        fontSize = 13.sp,
        fontFamily = AviBody,
        lineHeight = 19.sp
    )
}

// ============================================================
// Chip "DENGARKAN" — AVI membacakan penjelasan langkah (TTS nyata)
// ============================================================

@Composable
private fun DengarkanChip(palette: AviPalette, narasi: String, vm: AviViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.accent.copy(alpha = 0.10f))
            .border(1.dp, palette.accent.copy(alpha = 0.35f), RoundedCornerShape(50))
            .clickable { vm.speakTest(narasi) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            Icons.Default.VolumeUp,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "DENGARKAN",
            color = palette.accent,
            fontSize = 9.5.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}

// ============================================================
// LANGKAH 0 — SELAMAT DATANG (education & benefit)
// ============================================================

@Composable
private fun WizardWelcome(palette: AviPalette, vm: AviViewModel) {
    WizardHeader(
        kicker = "SELAMAT DATANG",
        judul = "Halo, Saya AVI",
        deskripsi = "Asisten virtual Indonesia yang hidup dari suara Anda. " +
            "Tanpa papan ketik, tanpa ribet — Anda bicara, AVI bekerja dan menjawab dengan suara.",
        palette = palette
    )
    Spacer(Modifier.height(6.dp))
    DengarkanChip(
        palette = palette,
        narasi = "Halo, saya AVI, asisten virtual Indonesia. Saya bekerja dari suara Anda: " +
            "Anda bicara, saya jawab, dan saya bisa membantu mengatur perangkat Anda.",
        vm = vm
    )
    Spacer(Modifier.height(14.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.panel.copy(alpha = 0.75f))
            .border(1.dp, palette.panelLight, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            WizardCapability(
                icon = { Icon(Icons.Default.GraphicEq, contentDescription = null, tint = palette.accent, modifier = Modifier.size(17.dp)) },
                judul = "PERINTAH SUARA",
                sub = "Bicara saja — papan ketik sengaja dimatikan. Apa yang AVI dengar tampil di bar bawah.",
                palette = palette
            )
            WizardDividerHud(palette)
            WizardCapability(
                icon = { Icon(Icons.Default.Psychology, contentDescription = null, tint = palette.gold, modifier = Modifier.size(17.dp)) },
                judul = "OTAK GANDA · GEMINI & NVIDIA NIM",
                sub = "Dua tipe otak, banyak model. Ganti model kapan pun — riwayat obrolan tetap utuh.",
                palette = palette
            )
            WizardDividerHud(palette)
            WizardCapability(
                icon = { Icon(Icons.Default.Widgets, contentDescription = null, tint = palette.accent, modifier = Modifier.size(17.dp)) },
                judul = "KENDALI PERANGKAT",
                sub = "Buka aplikasi, kirim WhatsApp & SMS, simpan kontak, buka browser ke alamat tertentu — cukup bilang.",
                palette = palette
            )
        }
    }
    Spacer(Modifier.height(14.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.accent.copy(alpha = 0.06f))
            .border(1.dp, palette.accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "5 langkah singkat ± 1 menit — semua bisa dilewati dan diatur ulang nanti.",
            color = palette.accentSoft,
            fontSize = 11.sp,
            fontFamily = AviBody,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun WizardCapability(
    icon: @Composable () -> Unit,
    judul: String,
    sub: String,
    palette: AviPalette
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.panelLight.copy(alpha = 0.7f))
                .border(1.dp, palette.panelLight, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                judul,
                color = palette.text,
                fontSize = 12.sp,
                fontFamily = AviHead,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                sub,
                color = palette.textDim,
                fontSize = 11.sp,
                fontFamily = AviBody,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun WizardDividerHud(palette: AviPalette) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, palette.accent.copy(alpha = 0.22f), Color.Transparent)
                )
            )
    )
}

// ============================================================
// LANGKAH 1 — KALIBRASI PEMILIK (P51).
// Umpan Pak: "Buat ganti nama di wizard aja susah!! jangan lu bikin
// ribet lah" + "lalu gk ada kalibrasi pemilik?" → dua-duanya dieksekusi:
//  1) NAMA — dulu TIAP HURUF langsung trim + ifBlank("Pak") + ditulis ke
//     DataStore: kolom "melawan" jari pemilik (spasi ditelan saat mengetik,
//     hapus semua = "Pak" balik sendiri, kursor melompat, lambat). Sekarang
//     draft murni lokal: ketikan BEBAS, tersimpan debounce + di-flush saat
//     pindah langkah.
//  2) KALIBRASI PEMILIK — ownerNote (opsional): fakta penting tentang
//     pemilik yang ditanam ke SEMUA prompt AVI (SystemPrompts.build).
// ============================================================

@Composable
private fun WizardKalibrasi(
    vm: AviViewModel,
    palette: AviPalette,
    namaDraft: String,
    noteDraft: String,
    onNama: (String) -> Unit,
    onNote: (String) -> Unit
) {
    // simpan DEBOUNCE — DataStore tidak ditulis per huruf (tak ada lag,
    // kursor tak melompat); flush final tetap dijalankan saat pindah langkah
    LaunchedEffect(namaDraft) {
        delay(450)
        vm.updateSettings { it.copy(userName = namaDraft.trim().take(24).ifBlank { "Pak" }) }
    }
    LaunchedEffect(noteDraft) {
        delay(450)
        vm.updateSettings { it.copy(ownerNote = noteDraft.trim().take(400)) }
    }

    WizardHeader(
        kicker = "KALIBRASI PEMILIK",
        judul = "Aku Panggil Apa?",
        deskripsi = "Satu langkah personal: nama panggilan Anda, plus fakta penting tentang Anda bila mau. " +
            "AVI memakai ini di setiap jawaban — bisa diubah kapan pun di PUSAT KENDALI.",
        palette = palette
    )
    Spacer(Modifier.height(6.dp))
    DengarkanChip(
        palette = palette,
        narasi = "Boleh tahu, saya panggil apa? Kalau mau, tuliskan juga fakta penting tentang Anda, " +
            "supaya setiap jawaban saya langsung pas dengan kebutuhan Anda.",
        vm = vm
    )
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = namaDraft,
        // P51 — ketikan BEBAS: tidak ada trim/ifBlank per huruf yang
        // menelan spasi atau memaksa "Pak" balik sendiri
        onValueChange = { v -> onNama(v.take(24)) },
        label = { Text("Nama panggilan Anda", color = palette.textDim, fontSize = 13.sp) },
        leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = palette.gold, modifier = Modifier.size(18.dp))
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = wizardFieldColors(palette)
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = noteDraft,
        onValueChange = { v -> onNote(v.take(400)) },
        label = { Text("Fakta tentang Anda — opsional", color = palette.textDim, fontSize = 13.sp) },
        leadingIcon = {
            Icon(Icons.Default.Psychology, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
        },
        placeholder = {
            Text(
                "Contoh: Saya pelajar, suka jawaban singkat.",
                color = palette.textDim.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
        colors = wizardFieldColors(palette)
    )
    Text(
        "Isi ini = kalibrasi pemilik: AVI menyesuaikan semua jawaban dengan catatan Anda " +
            "(pekerjaan, selera, cara bicara). Kosongkan bila tidak perlu.",
        color = palette.textDim.copy(alpha = 0.75f),
        fontSize = 10.sp,
        fontFamily = AviHud,
        lineHeight = 13.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
    Spacer(Modifier.height(14.dp))

    // pratinjau sapaan langsung — ikut ketikan SEKETIKA (pakai draft, bukan settings)
    val namaTampil = namaDraft.trim().ifBlank { "Pak" }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.aviBubble.copy(alpha = 0.85f))
            .border(1.dp, palette.aviBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                "PRATINJAU SAPAAN",
                color = palette.accent,
                fontSize = 9.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "\u201CHay $namaTampil! AVI online — semua sistem normal. " +
                    "Sentuh reaktor untuk bicara, $namaTampil.\u201D",
                color = palette.text,
                fontSize = 13.sp,
                fontFamily = AviBody,
                lineHeight = 18.sp
            )
        }
    }
}

// ============================================================
// LANGKAH 2 — IZIN SUARA (permission priming)
// ============================================================

@Composable
private fun WizardIzin(
    palette: AviPalette,
    micGranted: Boolean,
    vm: AviViewModel,
    minta: () -> Unit
) {
    WizardHeader(
        kicker = "IZIN SUARA",
        judul = "AVI Hidup dari Mic",
        deskripsi = "AVI hanya bisa mendengar lewat mikrofon. Izin ini murni untuk " +
            "mengenali ucapan Anda — tidak ada rekaman yang disimpan atau dikirim ke mana pun.",
        palette = palette
    )
    Spacer(Modifier.height(6.dp))
    DengarkanChip(
        palette = palette,
        narasi = "AVI hidup dari mikrofon. Izinkan mikrofon agar AVI bisa mendengar ucapan Anda. " +
            "AVI hanya mendengar saat sesi dengar aktif, dan tidak menyimpan rekaman apa pun.",
        vm = vm
    )
    Spacer(Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.panel.copy(alpha = 0.75f))
            .border(
                1.dp,
                if (micGranted) palette.accent.copy(alpha = 0.5f) else palette.panelLight,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (micGranted) palette.accent.copy(alpha = 0.15f)
                        else palette.panelLight.copy(alpha = 0.7f)
                    )
                    .border(
                        1.dp,
                        if (micGranted) palette.accent else palette.textDim.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (micGranted) palette.accent else palette.textDim,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    if (micGranted) "MIKROFON SIAP" else "MIKROFON BELUM DIIZINKAN",
                    color = if (micGranted) palette.accent else palette.gold,
                    fontSize = 12.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    if (micGranted)
                        "AVI bisa langsung mendengarkan begitu diaktifkan. Perintah suara langsung jalan."
                    else
                        "Tanpa izin ini AVI tidak bisa mendengar. Anda tetap bisa mengizinkan nanti lewat tombol mikrofon.",
                    color = palette.textDim,
                    fontSize = 11.sp,
                    fontFamily = AviBody,
                    lineHeight = 15.sp
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    if (!micGranted) {
        OutlinedButton(
            onClick = minta,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(13.dp)
        ) {
            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp), tint = palette.accent)
            Spacer(Modifier.width(8.dp))
            Text(
                "IZINKAN MIKROFON",
                fontSize = 12.sp,
                fontFamily = AviHead,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = palette.accent
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    Text(
        "Penjelasan dulu, dialog sistem kemudian — praktik permission priming. " +
            "AVI mendengar HANYA saat sesi dengar aktif (indikator jelas di layar).",
        color = palette.textDim.copy(alpha = 0.85f),
        fontSize = 10.sp,
        fontFamily = AviHud,
        lineHeight = 14.sp
    )
}

// ============================================================
// LANGKAH 3 — OTAK AI: GEMINI (MULTI KUNCI) / NVIDIA NIM
// ============================================================

@Composable
private fun WizardOtak(vm: AviViewModel, settings: AviSettings, palette: AviPalette) {
    val models by vm.models.collectAsState()
    val modelsLoading by vm.modelsLoading.collectAsState()
    val modelsError by vm.modelsError.collectAsState()
    val pickNote by vm.pickNote.collectAsState()
    val nimModels by vm.nimModels.collectAsState()
    val nimLoading by vm.nimLoading.collectAsState()
    val nimError by vm.nimError.collectAsState()
    val barTest by vm.barTest.collectAsState()
    var showKey by remember { mutableStateOf(false) }

    val pakaiGemini = settings.provider == AiProvider.GEMINI
    val geminiChat = models.filter { it.canChat && it.isPureChat }
    val kunciAda = if (pakaiGemini) settings.geminiKey.isNotBlank() else settings.ocKey.isNotBlank()
    val modelAda = if (pakaiGemini) settings.geminiModel.isNotBlank() else settings.ocModel.isNotBlank()

    WizardHeader(
        kicker = "OTAK AI",
        judul = "Pasang Otak AVI",
        deskripsi = "Dua tipe otak saja — GEMINI (gratis dari AI Studio Google) dan NVIDIA NIM " +
            "(kunci nvapi dari build.nvidia.com). Tempel kunci, muat model, pilih satu.",
        palette = palette
    )
    Spacer(Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HudChip("GEMINI — GRATIS", pakaiGemini, palette) {
            vm.updateSettings { it.copy(provider = AiProvider.GEMINI) }
        }
        HudChip("NVIDIA NIM", settings.provider == AiProvider.NIM, palette) {
            vm.updateSettings { it.copy(provider = AiProvider.NIM) }
        }
    }
    Spacer(Modifier.height(12.dp))

    if (pakaiGemini) {
        OutlinedTextField(
            value = settings.geminiKey,
            onValueChange = { v -> vm.updateSettings { it.copy(geminiKey = v.trim()) } },
            label = { Text("Kunci API Gemini utama", color = palette.textDim, fontSize = 12.sp) },
            singleLine = true,
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(
                        if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showKey) "Sembunyikan kunci" else "Lihat kunci",
                        tint = palette.textDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = wizardFieldColors(palette)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = settings.geminiKeysExtra,
            onValueChange = { v -> vm.updateSettings { it.copy(geminiKeysExtra = v) } },
            label = { Text("Kunci cadangan (opsional — 1 per baris, bisa 2-3 atau lebih)", color = palette.textDim, fontSize = 11.sp) },
            minLines = 2,
            maxLines = 3,
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = wizardFieldColors(palette)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Kuota kunci utama habis (429) atau ditolak → AVI otomatis lanjut ke kunci berikutnya tanpa berhenti.",
            color = palette.textDim.copy(alpha = 0.85f),
            fontSize = 9.5.sp,
            fontFamily = AviHud,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(10.dp))
        WizardAksiButton(
            text = "MUAT DAFTAR MODEL GEMINI",
            palette = palette,
            enabled = !modelsLoading && settings.geminiKey.isNotBlank(),
            loading = modelsLoading,
            onClick = { vm.fetchModels() }
        )
        modelsError?.let { err ->
            Spacer(Modifier.height(6.dp))
            Text(err, color = palette.danger, fontSize = 10.5.sp, fontFamily = AviBody, lineHeight = 14.sp)
        }
        pickNote?.let { catatan ->
            Spacer(Modifier.height(6.dp))
            Text(catatan, color = palette.accent, fontSize = 10.5.sp, fontFamily = AviBody, lineHeight = 14.sp)
        }
        if (geminiChat.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            WizardModelList(
                items = geminiChat.map { it.name },
                dipilih = settings.geminiModel,
                subtitle = { n -> if (settings.geminiModel == n) "AKTIF" else "ketuk untuk pakai" },
                palette = palette,
                onPick = { n -> vm.updateSettings { it.copy(geminiModel = n) } }
            )
        }
    } else {
        OutlinedTextField(
            value = settings.ocKey,
            onValueChange = { v -> vm.updateSettings { it.copy(ocKey = v.trim()) } },
            label = { Text("Kunci API NVIDIA (nvapi-…)", color = palette.textDim, fontSize = 12.sp) },
            singleLine = true,
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(
                        if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showKey) "Sembunyikan kunci" else "Lihat kunci",
                        tint = palette.textDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = wizardFieldColors(palette)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ambil kunci gratis di build.nvidia.com (Build → API Keys). NIM memakai endpoint resmi " +
                "https://integrate.api.nvidia.com/v1.",
            color = palette.textDim.copy(alpha = 0.85f),
            fontSize = 9.5.sp,
            fontFamily = AviHud,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(10.dp))
        WizardAksiButton(
            text = "MUAT DAFTAR MODEL NVIDIA",
            palette = palette,
            enabled = !nimLoading && settings.ocKey.isNotBlank(),
            loading = nimLoading,
            onClick = { vm.fetchNimModels() }
        )
        nimError?.let { err ->
            Spacer(Modifier.height(6.dp))
            Text(err, color = palette.danger, fontSize = 10.5.sp, fontFamily = AviBody, lineHeight = 14.sp)
        }
        if (nimModels.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            WizardModelList(
                items = nimModels,
                dipilih = settings.ocModel,
                subtitle = { n -> if (settings.ocModel == n) "AKTIF" else "ketuk untuk pakai" },
                palette = palette,
                onPick = { n -> vm.updateSettings { it.copy(ocModel = n) } }
            )
        }
    }

    if (kunciAda && modelAda) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.testActiveModel() }) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = palette.accent)
                Spacer(Modifier.width(6.dp))
                Text("TES KONEKSI", fontSize = 11.sp, fontFamily = AviHud, color = palette.accent)
            }
            when (barTest.phase) {
                1 -> {
                    CircularProgressIndicator(modifier = Modifier.size(13.dp), strokeWidth = 2.dp, color = palette.accent)
                    Text("  Menguji…", color = palette.textDim, fontSize = 11.sp, fontFamily = AviHud)
                }
                2 -> Text("TERHUBUNG · ${barTest.ms} MS", color = palette.accent, fontSize = 11.sp, fontFamily = AviHud, fontWeight = FontWeight.Bold)
                3 -> Text("GAGAL: ${barTest.message}", color = palette.danger, fontSize = 10.sp, fontFamily = AviHud, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun WizardAksiButton(
    text: String,
    palette: AviPalette,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.accent,
            contentColor = palette.bg
        )
    ) {
        if (loading) {
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
        Text(text, fontSize = 11.5.sp, fontFamily = AviHud, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun WizardModelList(
    items: List<String>,
    dipilih: String,
    subtitle: (String) -> String,
    palette: AviPalette,
    onPick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 190.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.panel.copy(alpha = 0.6f))
            .border(1.dp, palette.panelLight, RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState())
    ) {
        items.forEach { nama ->
            val aktif = dipilih == nama
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(nama) }
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Icon(
                    Icons.Default.Key,
                    contentDescription = null,
                    tint = if (aktif) palette.accent else palette.textDim.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    nama.removePrefix("models/"),
                    color = if (aktif) palette.text else palette.textDim,
                    fontSize = 11.5.sp,
                    fontFamily = AviCode,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    subtitle(nama),
                    color = if (aktif) palette.accent else palette.textDim.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontFamily = AviHud,
                    fontWeight = if (aktif) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ============================================================
// LANGKAH 4 — SIAP: ringkasan + AKTIFKAN
// ============================================================

@Composable
private fun WizardSiap(vm: AviViewModel, settings: AviSettings, palette: AviPalette, micGranted: Boolean) {
    val otak = if (settings.provider == AiProvider.GEMINI) {
        "GEMINI · " + settings.geminiModel.removePrefix("models/").ifBlank { "belum dipilih" }
    } else {
        "NVIDIA NIM · " + settings.ocModel.substringAfterLast('/').ifBlank { "belum dipilih" }
    }
    val kunci = if (settings.provider == AiProvider.GEMINI) {
        val n = settings.geminiKeyList().size
        if (n > 0) "TERPASANG · $n KUNCI (FAILOVER OTOMATIS)" else "BELUM — AVI AKAN MINTA DI PUSAT KENDALI"
    } else {
        if (settings.ocKey.isNotBlank()) "TERPASANG" else "BELUM — AVI AKAN MINTA DI PUSAT KENDALI"
    }

    WizardHeader(
        kicker = "SELAH — SISTEM SIAP",
        judul = "Semua Terpasang",
        deskripsi = "AVI sudah terkonfigurasi di perangkat ini. Kunci tersimpan aman (terenkripsi) " +
            "dan hanya dipakai untuk otak AVI. Tekan AKTIFKAN dan AVI langsung mulai mendengarkan.",
        palette = palette
    )
    Spacer(Modifier.height(14.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.panel.copy(alpha = 0.75f))
            .border(1.dp, palette.panelLight, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            InfoRow("PANGGILAN", settings.userName, palette.gold, palette)
            InfoRow("MIKROFON", if (micGranted) "SIAP — LANGSUNG MENDENGAR" else "LEWATI — AKTIFKAN VIA TOMBOL MIC", null, palette)
            InfoRow("OTAK AI", otak, palette.accent, palette)
            InfoRow("KUNCI API", kunci, if (kunci.startsWith("TERPASANG")) palette.accent else palette.danger, palette)
            InfoRow("VOKAL JAWABAN", if (settings.ttsEnabled) "AKTIF — JAWABAN DIBACAKAN" else "MATI — JAWABAN TEKS SAJA", null, palette)
            InfoRow("PAPAN KETIK", "SENDAWAK — HASIL SUARA SAJA", null, palette)
        }
    }
    Spacer(Modifier.height(12.dp))
    DengarkanChip(
        palette = palette,
        narasi = "Semua siap, ${settings.userName}. Tekan aktifkan, dan AVI akan langsung mendengarkan Anda.",
        vm = vm
    )
}

// ============================================================
// Warna field — satu keluarga dengan Pusat Kendali
// ============================================================

@Composable
private fun wizardFieldColors(palette: AviPalette) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = palette.accent,
    unfocusedBorderColor = palette.panelLight,
    cursorColor = palette.accent,
    focusedTextColor = palette.text,
    unfocusedTextColor = palette.text
)
