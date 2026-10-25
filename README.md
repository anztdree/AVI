# AVI — Asisten Virtual Indonesia

> Asisten suara pribadi ala Jarvis, jiwa Indonesia: **"Hay pak… Siap pak!"**

**100% Native Android** (Kotlin + Jetpack Compose) — bukan wrapper, bukan WebView.
**AI 100% cloud** (Google Gemini) — HP spek rendah pun kuat, nol model di perangkat.
**Model dinamis** — daftar model di-*fetch* langsung dari Google; pemilik pilih sendiri yang paling up-to-date.

**Status: RENCANA FINAL — menunggu perintah eksekusi.** Kode dasar v1.0.0 sudah jalan (chat suara). README ini = dokumen tunggal proyek + log keputusan pemilik.

---

## 1. Prinsip Wajib

1. **Native Android** — Kotlin + Compose. Tanpa wrapper/PWA/Capacitor.
2. **Nol LLM lokal** — STT/TTS pakai layanan sistem Android (ringan); semua AI lewat cloud API.
3. **Model dari Google, bukan hardcode** — aplikasi men-*fetch* `ListModels`; model aktif dipilih pemilik dari daftar terbaru.
4. **Repo higienis** — hanya source AVI + README ini. Tanpa scaffold web/infra lain.
5. **Setiap milestone di-push ke GitHub** — mitigasi sandbox reset.
6. **Tidak ada implementasi sebelum pemilik memerintahkan.**

---

## 2. Peta Fitur & Fungsi

Status: **✅ sudah jalan (v1.0.0)** · **🔧 disetujui, masuk roadmap** · **💡 cadangan (belum diputuskan)** · **❌ ditolak/dihapus**

### A. Otak & Percakapan

| Fitur | Status | Catatan |
|---|---|---|
| Chat pintar, jawaban streaming | ✅ | Gemini SSE — teks muncul kata-per-kata |
| Kepribadian pelayan Indonesia | ✅ | "Hay pak… Siap pak!" — rinci di §3 |
| **Pilih model sendiri dari daftar terbaru Google** | 🔧 F1 | Pengaturan →「Muat Daftar Model」→ `GET /v1beta/models` → pilih → dipakai semua fitur. Hardcode `gemini-2.0-flash` dihapus |
| Multi-provider | ✅ | Gemini (utama) atau API kompatibel OpenAI (OpenRouter/Groq) |
| Info terkini (search) | ✅ | Grounding `google_search` — cuaca, berita, harga, jadwal |
| **Memori jangka panjang** | 🔧 F3 | *"Ingat ini pak: …"* → tersimpan permanen, otomatis diingat di semua obrolan; kelola (lihat/hapus) di Pengaturan |
| **Perintah cepat (chips)** | 🔧 F4 | Tombol pintas di atas kolom ketik: 「Cuaca」「Berita hari ini」「Kondisi HP」「Buka aplikasi」「Ingat…」 |
| Multi-bahasa jawaban | 💡 | Default Indonesia; bisa diminta bahasa lain |

### B. Aksi Perangkat (baru — disetujui pemilik)

Semua dibaca/dijalankan **lokal di HP** — instan, tanpa internet; AVI tinggal merangkai jawabannya dengan gayanya.

| Fitur | Status | Catatan |
|---|---|---|
| **Buka aplikasi** | 🔧 F4 | *"Buka YouTube pak"* → AVI mencocokkan nama dengan daftar aplikasi terpasang (PackageManager, pencocokan pintar) → luncurkan. Ambigu → tampilkan pilihan; tidak ada → "Tidak ada aplikasi itu, pak." |
| **Kondisi HP / info sistem** | 🔧 F4 | Baterai (% + status mengisi), penyimpanan (sisa/total), RAM, jaringan (WiFi/seluler/offline), model & versi Android. Contoh: *"AVI, baterai berapa?"* → "Baterai 68% pak, lagi diisi. Santai." |

### C. Suara

| Fitur | Status | Catatan |
|---|---|---|
| Mendengar (STT) id-ID | ✅ | SpeechRecognizer sistem + HUD kustom, hasil parsial tampil live |
| Berbicara (TTS) id-ID | ✅ | TextToSpeech sistem |
| Percakapan beruntun | ✅ | Setelah menjawab → langsung mendengar lagi; sentuh reaktor untuk berhenti |
| Atur kecepatan & nada suara | ✅ | Slider di Pengaturan |
| Pilih voice dari daftar perangkat | 💡 | Cadangan |
| Kata bangun "Hay AVI" | 💡 | Cadangan — Android membatasi mic latar belakang |

### D. Data & Riwayat

| Fitur | Status | Catatan |
|---|---|---|
| Riwayat chat tersimpan lokal | ✅ | File JSON — **tetap dipakai** (keputusan pemilik; migrasi Room tidak diminta) |
| Ekspor / backup | 💡 | Cadangan |

### E. HUD & Tampilan

| Fitur | Status | Catatan |
|---|---|---|
| Orb arc reactor 4 state | ✅ | Siaga / mendengar / berpikir / bicara |
| Tema gelap Iron Man | ✅ | Hitam kebiruan + cyan + emas |
| Widget homescreen | 💡 | Cadangan |
| Layar siaga ala Jarvis | 💡 | Cadangan |
| Quick Settings Tile (panel notifikasi) | ❌ | **Ditolak pemilik — tidak perlu sama sekali** |

### F. Dihapus

| Fitur | Status | Catatan |
|---|---|---|
| Mesin gambar (Google / Pollinations) | ❌ | **Dihapus total sesuai keputusan pemilik.** Kode v1 (`image/ImageGen.kt`, tag `[GAMBAR:]` di prompt & UI) dibersihkan di F2 |

---

## 3. Kepribadian & Aturan Jawaban

| Situasi | Kalimat wajib |
|---|---|
| Menyapa / mulai mendengar | "Hay pak, ada yang bisa AVI bantu?" |
| Menerima perintah | "Siap pak." |
| Proses lama | "Sebentar pak, AVI proses dulu." |
| Selesai | "Sudah selesai, pak." |
| Tidak paham / tidak terdengar | "Mohon maaf pak, bisa diulang?" |
| Koneksi putus | "Koneksi ke markas terputus, pak." |
| API key bermasalah | "Kunci markasnya belum terpasang, pak. Buka Pengaturan ya." |
| Aplikasi tidak ditemukan | "Tidak ada aplikasi itu di HP, pak." |

Aturan:
1. **Mode suara**: 1–3 kalimat, langsung ke inti (akan diucapkan — hindari daftar panjang).
2. **Mode teks**: boleh rinci dan berformat.
3. Info terkini → otomatis search, sebut sumber singkat.
4. Indonesia santun; panggil "Pak" secara wajar, tidak di setiap kalimat.

Contoh:

```
User : "Avi, cuaca Jakarta gimana?"
AVI  : "Sebentar pak, AVI cek dulu."
AVI  : "Cuaca Jakarta siang ini cerah berawan, 28–33 derajat, pak. Aman buat berangkat."

User : "Avi, baterai berapa?"
AVI  : "Baterai 68% pak, lagi diisi. Santai."

User : "Buka YouTube pak"
AVI  : "Siap pak."  → (YouTube terbuka)
```

---

## 4. HUD & Visual

| Elemen | Nilai |
|---|---|
| Latar | `#070B12` |
| Cyan reaktor | `#22D3EE` |
| Emas | `#F5B942` |
| Teks | Putih / abu kebiruan |
| Label status | Monospace, uppercase |
| Target sentuh | Minimal 48dp |
| Animasi | 150–400 ms, hemat baterai |

Siklus suara & state:

```
SIAGA ──(sentuh reaktor)──► MENYAPA ──► MENDENGAR ──► BERPIKIR ──► MENJAWAB ──┐
  ▲                                                                          │
  └──────────(reaktor disentuh / 2× gagal dengar)────(auto-listen)◄──────────┘
```

| State | Orb | Mic | TTS |
|---|---|---|---|
| SIAGA | Pulsa pelan | off | off |
| MENDENGAR | Pulsa cepat + gelombang | **on** | off |
| BERPIKIR | Ring berputar | off | off |
| MENJAWAB | Glow penuh | off | **on** |

---

## 5. Arsitektur

```
┌───────────────── PERANGKAT ANDROID (native Kotlin) ─────────────────┐
│  UI Compose (HUD): Obrolan (+chips) · Pengaturan (+pilih model,     │
│                    kelola memori)                                   │
│  Data lokal: DataStore (key, MODEL TERPILIH, preferensi)            │
│              Riwayat chat (JSON) · Memori jangka panjang            │
│  Voice Engine (layanan sistem — ringan, BUKAN LLM):                 │
│    STT android.speech.SpeechRecognizer (id-ID)                      │
│    TTS android.speech.tts.TextToSpeech (id-ID)                      │
│  Aksi Perangkat (lokal, instan):                                    │
│    PackageManager (buka aplikasi) · BatteryManager · StatFs ·       │
│    ActivityManager (RAM) · ConnectivityManager (jaringan)           │
└──────────────┬──────────────────────────────────────────────────────┘
               │ HTTPS (OkHttp + kotlinx.serialization)
               ▼
   Google Generative Language API — SEMUA AI DI CLOUD
   ├─ GET  /v1beta/models                       → daftar model terbaru (pemilik pilih)
   └─ POST /v1beta/models/{terpilih}:streamGenerateContent
         ├─ systemInstruction: kepribadian AVI + memori
         └─ tools: google_search                → info terkini
```

---

## 6. Kondisi Kode Saat Ini (audit v1.0.0)

**Sudah ada**: proyek Gradle (minSdk 26), HUD orb + chat + pengaturan (Compose), Gemini streaming + search grounding + fallback, OpenAI-compat client, STT/TTS id-ID + mode beruntun, gambar Pollinations (→ dihapus di F2), riwayat JSON, DataStore, CI build APK, Release v1.0.0.

**Masalah tercatat**:

| # | Masalah | Lokasi | Penanganan |
|---|---|---|---|
| M1 | Model hardcode `"gemini-2.0-flash"` | `ai/GeminiClient.kt:47` | F1 |
| M2 | Tidak ada `ListModels` / pilihan model | `ai/`, `data/` | F1 |
| M3 | Fitur gambar harus dihapus (keputusan pemilik) | `image/`, prompt `[GAMBAR:]`, UI | F2 |
| M4 | Riwayat file JSON | `data/ChatHistoryRepository.kt` | Tetap — keputusan pemilik |
| M5 | Tidak ada Gradle Wrapper | root | F5 |
| M6 | API key plaintext | `data/SettingsRepository.kt` | F5 |

---

## 7. Roadmap Final (hasil keputusan pemilik)

| Fase | Isi | Status |
|---|---|---|
| **F1** | **Fetch model dinamis dari Google** + hapus hardcode (M1, M2) | **Siap eksekusi — menunggu "lanjut"** |
| F2 | Hapus total fitur gambar: `ImageGen.kt`, tag `[GAMBAR:]` di prompt/UI (M3) | Disetujui |
| F3 | Memori jangka panjang: simpan/lookup/hapus + injeksi ke prompt | Disetujui |
| F4 | Aksi perangkat: buka aplikasi + info kondisi HP + chips perintah cepat | Disetujui |
| F5 | Gradle wrapper (M5) + key terenkripsi (M6) + polish + rilis **v1.1.0** | Disetujui |

Setiap fase = 1 milestone + 1 push ke GitHub.

---

## 8. Build APK & Setup

**Dapat APK tanpa Android Studio**: tab **Actions** → workflow *Build AVI APK* → run hijau → **Artifacts** → `AVI-debug-apk` → ekstrak → install di HP. Rilis stabil: tab **Releases**.

**Setup otak AVI**: https://aistudio.google.com/apikey → Create API key → aplikasi ⚙ Pengaturan → tempel key.

**Struktur repo** (saat ini):

```
AVI/
├── app/src/main/java/com/anztdree/avi/
│   ├── MainActivity.kt · AviViewModel.kt   # orkestrasi state chat+suara+HUD
│   ├── ai/      # Gemini SSE + OpenAI-compat + prompt kepribadian
│   ├── data/    # DataStore + riwayat
│   ├── speech/  # AviTts + AviStt
│   ├── image/   # (akan dihapus di F2)
│   └── ui/      # HUD orb, chat, pengaturan
├── .github/workflows/build-android.yml       # CI APK otomatis
├── build.gradle.kts · settings.gradle.kts · gradle.properties
└── README.md                                 # ← dokumen tunggal proyek
```

---

## 9. Keamanan

- `.env` lama sudah keluar dari repo & riwayat `main`; tidak ada kredensial di repo/workflow.
- API key Gemini hanya di perangkat pemilik (ditingkatkan ke terenkripsi di F5).
- ⚠️ **PAT GitHub pernah terekspos di chat — regenerate setelah proyek stabil.**

---

## 10. Log Keputusan Pemilik

| # | Keputusan | Jawaban pemilik | Akibat |
|---|---|---|---|
| K1 | Mesin gambar | **Tidak pakai sama sekali** | Fitur dihapus total (F2); galeri & tag `[GAMBAR:]` ikut dibersihkan |
| K2 | Ide pengembangan | **Ya**: memori jangka panjang, perintah cepat, buka aplikasi, info sistem HP. **Tidak**: panel notifikasi (tile) | Masuk F3–F4; tile ditandai ❌ |
| K3 | Riwayat chat | (tidak diminta diubah) | Tetap file JSON |
| K4 | Urutan fase | F1 → F5 sesuai §7 | Eksekusi setelah perintah |
