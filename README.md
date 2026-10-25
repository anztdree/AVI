# AVI — Asisten Virtual Indonesia

> Asisten suara pribadi ala Jarvis, jiwa Indonesia: **"Hay pak… Siap pak!"**

**100% Native Android** (Kotlin + Jetpack Compose) — bukan wrapper, bukan WebView.
**AI 100% cloud** (Google Gemini) — HP spek rendah pun kuat, nol model di perangkat.
**Model dinamis** — daftar model di-*fetch* langsung dari Google; pemilik pilih sendiri yang paling up-to-date.

**Status: PERENCANAAN.** Kode dasar v1.0.0 sudah jalan (chat suara). Pengembangan berikutnya **menunggu persetujuan pemilik** — dokumen tunggal proyek adalah README ini.

---

## 1. Prinsip Wajib

1. **Native Android** — Kotlin + Compose. Tanpa wrapper/PWA/Capacitor.
2. **Nol LLM lokal** — STT/TTS pakai layanan sistem Android (ringan); semua AI lewat cloud API.
3. **Model dari Google, bukan hardcode** — aplikasi men-*fetch* `ListModels`; model aktif dipilih pemilik dari daftar terbaru.
4. **Repo higienis** — hanya source AVI + README ini. Tanpa scaffold web/infra lain.
5. **Setiap milestone di-push ke GitHub** — mitigasi sandbox reset.
6. **Tidak ada implementasi sebelum pemilik memerintahkan.**

---

## 2. Peta Fitur & Fungsi (curahan seluruh ide)

Keterangan status: **✅ sudah jalan (v1.0.0)** · **🔧 direncanakan, menunggu OK** · **💡 usulan baru — untuk didiskusikan**

### A. Otak & Percakapan

| Fitur | Status | Catatan |
|---|---|---|
| Chat pintar, jawaban streaming | ✅ | Gemini SSE — teks muncul kata-per-kata |
| Kepribadian pelayan Indonesia | ✅ | "Hay pak… Siap pak!" — rinci di §3 |
| **Pilih model sendiri dari daftar terbaru Google** | 🔧 | Pengaturan →「Muat Daftar Model」→ `GET /v1beta/models` → pilih → dipakai semua fitur. Hardcode `gemini-2.0-flash` dihapus |
| Multi-provider | ✅ | Gemini (utama) atau API kompatibel OpenAI (OpenRouter/Groq) |
| Info terkini (search) | ✅ | Grounding `google_search` — cuaca, berita, harga, jadwal |
| Memori jangka panjang | 💡 | Bilang *"Ingat ini pak: …"* → tersimpan permanen, otomatis diingat di semua obrolan berikutnya; bisa dilihat/hapus di Pengaturan |
| Perintah cepat (chips) | 💡 | Tombol pintas di atas kolom ketik: 「Cuaca」「Berita hari ini」「Buat gambar…」「Ingat…」 |
| Multi-bahasa jawaban | 💡 | Default Indonesia; bisa diminta jawab Inggris/lainnya |

### B. Suara

| Fitur | Status | Catatan |
|---|---|---|
| Mendengar (STT) id-ID | ✅ | SpeechRecognizer sistem + HUD kustom, hasil parsial tampil live |
| Berbicara (TTS) id-ID | ✅ | TextToSpeech sistem |
| Percakapan beruntun | ✅ | Setelah menjawab → langsung mendengar lagi; sentuh reaktor untuk berhenti |
| Atur kecepatan & nada suara | ✅ | Slider di Pengaturan |
| Pilih voice dari daftar perangkat | 💡 | Tes suara per voice sebelum dipilih |
| Kata bangun "Hay AVI" | 💡 | ⚠️ Android membatasi mikrofon latar belakang — solusi realistis: widget/tile (di bawah), bukan mic 24 jam |

### C. Kreatif

| Fitur | Status | Catatan |
|---|---|---|
| Buat gambar dari suara/teks | ✅ | Tag `[GAMBAR:]` → Pollinations (gratis) |
| Mesin gambar via Google | 🔧/💡 | **Perlu keputusan pemilik (K1)**: model gambar Google (konsisten 1 API, kualitas bagus) vs Pollinations (gratis tanpa kuat kuota) vs keduanya (pilihan di Pengaturan) |
| Galeri gambar dalam aplikasi | 💡 | Riwayat hasil gambar, simpan ke galeri HP, share langsung |

### D. Data & Riwayat

| Fitur | Status | Catatan |
|---|---|---|
| Riwayat chat tersimpan lokal | ✅ | File JSON |
| Migrasi ke database Room + sesi | 🔧/💡 | Pecah per sesi, ganti judul, cari chat lama (K3) |
| Ekspor / backup | 💡 | Simpan riwayat + pengaturan ke file, impor di HP baru |

### E. HUD & Tampilan

| Fitur | Status | Catatan |
|---|---|---|
| Orb arc reactor 4 state | ✅ | Siaga / mendengar / berpikir / bicara — animasi berbeda per state |
| Tema gelap Iron Man | ✅ | Hitam kebiruan + cyan + emas |
| Widget homescreen | 💡 | Tombol reaktor langsung di layar utama — sentuh → AVI langsung mendengar |
| Quick Settings Tile | 💡 | "AVI, dengar" di panel notifikasi — akses 1 usap |
| Layar siaga ala Jarvis | 💡 | Layar penuh: jam besar + orb + status — dipakai saat HP di dock/meja |

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
│  UI Compose (HUD): Obrolan · Galeri · Pengaturan                    │
│  Data lokal: DataStore (key, MODEL TERPILIH, preferensi) + riwayat  │
│  Voice Engine (layanan sistem — ringan, BUKAN LLM):                 │
│    STT android.speech.SpeechRecognizer (id-ID)                      │
│    TTS android.speech.tts.TextToSpeech (id-ID)                      │
└──────────────┬──────────────────────────────────────────────────────┘
               │ HTTPS (OkHttp + kotlinx.serialization)
               ▼
   Google Generative Language API — SEMUA AI DI CLOUD
   ├─ GET  /v1beta/models                       → daftar model terbaru (pemilik pilih)
   ├─ POST /v1beta/models/{terpilih}:streamGenerateContent
   │     ├─ systemInstruction: kepribadian AVI
   │     └─ tools: google_search                → info terkini
   └─ model gambar (dari daftar, bila dipilih)  → gambar
```

---

## 6. Kondisi Kode Saat Ini (audit v1.0.0)

**Sudah ada**: proyek Gradle (minSdk 26), HUD orb + chat + pengaturan (Compose), Gemini streaming + search grounding + fallback, OpenAI-compat client, STT/TTS id-ID + mode beruntun, gambar Pollinations, riwayat JSON, DataStore, CI build APK, Release v1.0.0.

**Masalah tercatat** (perbaikan menunggu perintah):

| # | Masalah | Lokasi |
|---|---|---|
| M1 | Model hardcode `"gemini-2.0-flash"` | `ai/GeminiClient.kt:47` |
| M2 | Tidak ada `ListModels` / field model Gemini di pengaturan | `ai/`, `data/` |
| M3 | Mesin gambar Pollinations — perlu keputusan (K1) | `image/ImageGen.kt` |
| M4 | Riwayat file JSON, bukan Room | `data/ChatHistoryRepository.kt` |
| M5 | Tidak ada Gradle Wrapper (`gradlew`) | root |
| M6 | API key disimpan plaintext | `data/SettingsRepository.kt` |

---

## 7. Roadmap

| Fase | Isi | Status |
|---|---|---|
| **F1** | Fetch model dinamis dari Google + hapus hardcode (M1, M2) | **Menunggu OK** |
| F2 | Gradle wrapper (M5) + key terenkripsi (M6) | Menunggu OK |
| F3 | Riwayat Room + sesi (M4 — tergantung K3) | Menunggu OK |
| F4 | Mesin gambar final (M3 — tergantung K1) | Menunggu OK |
| F5 | Ide 💡 terpilih hasil diskusi (K2) + polish + rilis v1.1.0 | Menunggu diskusi |

---

## 8. Build APK & Setup

**Dapat APK tanpa Android Studio**: tab **Actions** → workflow *Build AVI APK* → run hijau → **Artifacts** → `AVI-debug-apk` → ekstrak → install di HP. Rilis stabil: tab **Releases**.

**Setup otak AVI**: https://aistudio.google.com/apikey → Create API key → aplikasi ⚙ Pengaturan → tempel key.

**Struktur repo**:

```
AVI/
├── app/src/main/java/com/anztdree/avi/
│   ├── MainActivity.kt · AviViewModel.kt   # orkestrasi state chat+suara+HUD
│   ├── ai/      # Gemini SSE + OpenAI-compat + prompt kepribadian
│   ├── data/    # DataStore + riwayat
│   ├── speech/  # AviTts + AviStt
│   ├── image/   # ImageGen
│   └── ui/      # HUD orb, chat, pengaturan
├── .github/workflows/build-android.yml       # CI APK otomatis
├── build.gradle.kts · settings.gradle.kts · gradle.properties
└── README.md                                 # ← dokumen tunggal proyek
```

---

## 9. Keamanan

- `.env` lama sudah keluar dari repo & riwayat `main`; tidak ada kredensial di repo/workflow.
- API key Gemini hanya di perangkat pemilik (akan ditingkatkan ke terenkripsi di F2).
- ⚠️ **PAT GitHub pernah terekspos di chat — regenerate setelah proyek stabil.**

---

## 10. Keputusan yang Menunggu Jawaban Pemilik

| # | Keputusan | Opsi |
|---|---|---|
| **K1** | Mesin gambar | (a) Google saja · (b) Pollinations saja · (c) keduanya, pilihan di Pengaturan |
| **K2** | Ide 💡 mana yang masuk roadmap | widget · tile · layar siaga · memori jangka panjang · chips · galeri · pilih voice · ekspor/backup — pilih yang diinginkan |
| **K3** | Riwayat chat | (a) migrasi Room + sesi · (b) biarkan file JSON |
| **K4** | Urutan fase | Setuju F1→F5 atau ubah? |
