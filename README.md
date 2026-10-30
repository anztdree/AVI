# AVI — Asisten Virtual Indonesia

> Asisten suara pribadi ala Jarvis, jiwa Indonesia: **"Hay pak… Siap pak!"**

**100% Native Android** (Kotlin + Jetpack Compose) — bukan wrapper, bukan WebView.
**AI 100% cloud** (Google Gemini) — HP spek rendah pun kuat, nol model di perangkat.
**Model dinamis** — daftar model di-*fetch* langsung dari Google; pemilik pilih sendiri yang paling up-to-date.

**Status: v1.1.0 HUD MARK IV — RILIS.** Build CI hijau, APK terbaru selalu ada di [Releases v1.0.0](https://github.com/anztdree/AVI/releases/tag/v1.0.0) (nama file unik per build: `AVI-v<versi>-<build>-<hash>.apk`). Seluruh rencana (HUD Mark II, Pusat Kendali, fetch model dinamis, memori, aksi perangkat, flagship X1–X8) dikerjakan sekaligus dalam satu rilis sesuai keputusan pemilik. Revisi lanjutan dilakukan sedikit demi sedikit.

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

### G. Fitur Flagship & Menu Ide — pilih nomornya untuk masuk roadmap

> Tanda ⚠️ = butuh izin Android (diminta saat dipakai, dijelaskan alasannya, bukan saat install).

**FLAGSHIP — level WAH (X):**

| ID | Fitur | Kenapa WAH | Catatan realistis |
|---|---|---|---|
| X1 | **Live Voice Mode** — obrolan suara↔suara langsung, bisa disela di tengah kalimat, tanpa jeda "teks dulu" | Pengalaman Jarvis sejati | Model audio real-time diambil otomatis dari daftar `ListModels` (WebSocket); fallback: mode suara standar |
| X2 | **Suara natural cloud** — suara Indonesia natural dari model TTS Google, pilihan karakter suara | Lompatan kualitas dari TTS robot | Model TTS muncul otomatis di daftar model; fallback TTS Android |
| X3 | **Protokol** — makro kustom: satu frase → rangkaian aksi ("protokol kerja" = agenda + buka WA + cuaca) | Ala *House Party Protocol* | Dibuat sendiri di Pusat Kendali |
| X4 | **Briefing pagi proaktif** — buka aplikasi: sapa + cuaca + berita utama + kondisi HP, tanpa diminta | AVI berinisiatif seperti staf pribadi | Search + aksi perangkat |
| X5 | **Mode penerjemah percakapan** — bicara Indonesia → AVI ucapkan Inggris (atau sebaliknya) | Berguna & keren saat perjalanan | LLM |
| X6 | **Agen riset** — "riset berita AI hari ini, rangkum 3 teratas" → cari–saring–rangkum otomatis | Kerja multi-langkah | Search grounding + LLM |
| X7 | **Kesinambungan topik** — ingat pembahasan sesi-sesi lalu & bisa melanjutkan ("kemarin kita bahas X, lanjut?") | Di atas memori biasa: ringkasan sesi otomatis | LLM + penyimpanan lokal |
| X8 | **Visor AVI** — "wajah" visor yang hidup: melirik saat mendengar, berdenyut saat bicara | AVI punya karakter, bukan sekadar orb | Canvas 2D ringan |

**Ide level dasar (I) — opsional:**

**Aksi Perangkat Lanjutan** (paling terasa "Jarvis"):

| ID | Ide | Catatan teknis |
|---|---|---|
| I1 | **Alarm & timer suara** — "bangunin jam 5 pak", "timer 10 menit" | Intent alarm sistem, tanpa izin khusus |
| I2 | **Telepon & SMS** — "telpon Budi", "SMS Budi: ..." | ⚠️ izin kontak/telepon; AVI selalu konfirmasi sebelum eksekusi |
| I3 | **Senter, volume, kecerahan** — "nyalain senter pak", "kecilin volume" | CameraManager + panel pengaturan |
| I4 | **Buka website** — "buka tokopedia.com" | Intent browser |
| I5 | **Cuaca otomatis sesuai lokasi** — tidak perlu sebut kota | ⚠️ izin lokasi; hanya kota yang dikirim ke API |
| I6 | **Pengingat & catatan** — "ingetin meeting jam 2", "catet: beli kopi" | ⚠️ izin notifikasi; daftar catatan di dalam aplikasi |

**Produktivitas:**

| ID | Ide | Catatan teknis |
|---|---|---|
| I7 | **Rangkum link/berita** — tempel URL → AVI baca & rangkum | Fetch artikel + LLM |
| I8 | **Draft pesan** — "bikinin pesan izin sakit ke bos" → kirim ke WA/Telegram | Share intent |
| I9 | **Terjemahan & penulisan** — terjemah, rapikan bahasa, caption, email formal | LLM |
| I10 | **Hitung & konversi** — "1 dolar berapa rupiah" | LLM + search kurs |

**Personalitas & Suara:**

| ID | Ide | Catatan teknis |
|---|---|---|
| I11 | **Sapaan sesuai waktu + status markas** — "Selamat pagi pak", "Semua sistem normal, pak" | Jam perangkat, gratis |
| I12 | **Efek suara HUD** — bunyi kecil ala Jarvis saat state berubah | Bisa dimatikan di Pengaturan |
| I13 | **Gaya bicara** — pilihan: sopan / santai / jedai | Variasi system prompt |
| I14 | **Interupsi** — sentuh orb saat AVI bicara → langsung diam & dengar | Kontrol TTS |

**Sistem & Kualitas:**

| ID | Ide | Catatan teknis |
|---|---|---|
| I15 | **Jawaban instan offline** — jam, tanggal, hitung cepat: tanpa API, <100 ms | Template lokal, hemat kuota |
| I16 | **Onboarding wizard** — key → muat model → pilih → tes suara, sekali jalan | UI |
| I17 | **Pemantau pemakaian API** — hitung request/token per hari | Lokal |
| I18 | **Cek pembaruan in-app** — baca release GitHub → tawarkan APK baru | GitHub API |
| I19 | **Riwayat per sesi** — judul otomatis, cari chat lama | UI + file JSON |

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

## 4. Tampilan: HUD Mark II — wajib WAH & mewah, tetap ringan

> Keputusan pemilik: tampilan & pengaturan **tidak boleh terasa biasa**. Seluruh efek
> digambar dengan Compose Canvas (vektor + GPU, tanpa video/GIF berat) — target 60 fps
> di HP kentang, hemat baterai. Ada **Mode Hemat** yang mematikan efek berat otomatis.

### 4.1 Lapisan Tampilan

| Lapisan | Detail |
|---|---|
| **Boot Sequence** | Sekali per buka aplikasi (±2,5 dtk, bisa dimatikan): reaktor menyala → ring spin-up → sapuan scan → teks `AVI ONLINE` → elemen HUD meluncur masuk |
| **Living Core** | Orb reaktor 4 state + **gelombang suara NYATA dari mikrofon** yang mengitar ring saat mendengar; saat berpikir: sapuan scan + dua ring orbit berlawanan arah; saat bicara: glow mengikuti amplitudo TTS |
| **Diagnostik Orbit** | Readout hidup mengelilingi orb: jam, baterai, jaringan, nama model aktif — ala JARVIS |
| **Latar Hex Grid** | Grid heksagonal samar + parallax halus mengikuti giroskop (mati otomatis di Mode Hemat) |
| **Transisi Hologram** | Antar layar: efek scan-line + elemen muncul bertahap — bukan slide kaku |
| **Haptic Khas** | Pola getar berbeda per state (mendengar / berpikir / menjawab) |

### 4.2 Tema "Mark" (live preview, tanpa restart)

| Tema | Palet |
|---|---|
| **MK-I — default** | Cyan `#22D3EE` + emas `#F5B942` di latar `#070B12` |
| MK-II Hot Rod | Merah `#FF3B30` + emas |
| MK-III Aureus | Emas dominan + aksen putih |

### 4.3 Jaminan Ringan

- Semua animasi = Compose Canvas + vector; tanpa aset gambar/video besar
- Partikel terbatas & di-pool; animasi berbasis durasi (bukan frame)
- **Mode Hemat**: deteksi otomatis (atau manual) → matikan parallax, partikel, boot sequence — HUD tetap WAH versi ringan

### 4.4 Pusat Kendali — Misi Kendali 5 Tab (K20, kelas Jarvis)

> Pengaturan BUKAN daftar datar & bukan satu gulungan panjang. Ini "ruang kendali markas" dengan navigasi tab misi:

| Komponen | Detail |
|---|---|
| **Header Sinematik** | Inti plasma hidup (cincin tick emas/aksen berputar + denyut), judul PUSAT KENDALI, **jam markas WIB per detik**, badge panggilan pemilik |
| **Strip Status** | 4 petak hidup: OTAK (SIAP/OFF) · SUARA (HIDUP/SENYAP) · PEMILIK (panggilan) · TAB MISI aktif |
| **5 Tab Misi** | `OTAK` (kartu inti + provider/kunci + daftar model + UJI SEMUA + diagnosa) · `SUARA` (percakapan, dial vokal + preset, cloud TTS, tes dengar) · `GAYA` (panggilan custom, gaya bicara, memori, protokol) · `HUD` (tema Mark 2×2 + efek) · `SISTEM` (kuota, aksi cepat, modul antrean, tentang) — indikator meluncur, transisi hologram, getar halus |
| **Toggle Reaktor & Dial** | Saklar beranimasi gaya reaktor + slider "dial tenaga" + preset vokal cepat |
| **Kartu Model** | Pemilih model tampil sebagai kartu inti daya: nama, ukuran konteks, kemampuan, status uji ✓ SIAP / ✗ GAGAL + alasan |
| **Ring Kuota** | Gauge arc pemakaian API harian (perkiraan ±1500/hari, hitung lokal) + statistik model siap/memori/protokol |
| **Aksi Cepat** | 6 petak: RIWAYAT · UJI SUARA · TES DENGAR · DIAGNOSA · MUAT MODEL · AI STUDIO — aksi diagnosa/muat otomatis lompat ke tab OTAK |
| **Modul Antrean** | 6 kartu badge SEGERA: Wake Word · Cuaca Langsung · Berita Harian · Penerjemah · Kalender · Rumah Pintar |
| **Live Preview** | Ganti tema/efek langsung terlihat — nebula per-Mark + hex grid + bingkai HUD ikut berganti, tanpa restart |

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

## 6. Kondisi Kode Saat Ini (audit v1.0.0 — pasca koreksi)

**Sudah ada**: proyek Gradle (minSdk 26), HUD Mark II (boot sequence, living core + visor, halo reaktor, dock status, tema Mark, haptic, mode hemat), Pusat Kendali (panel OTAK/SUARA/TAMPILAN/SISTEM/MEMORI/PROTOKOL, kartu model dinamis, ruang tes suara), Gemini streaming + grounding + fallback config, fetch model dinamis (ListModels), Live Voice Mode (BidiGenerateContent), Cloud TTS, STT/TTS id-ID + mode beruntun, memori jangka panjang `[INGAT:]`, protokol, aksi perangkat (buka aplikasi, kondisi HP, briefing), riwayat JSON, DataStore + SecureStore terenkripsi, CI build APK release (R8).

**Fitur dihapus permanen**: mesin gambar (K1), tile notifikasi (K2), tombol translate & sound di layar utama (K7 — AVI selalu membacakan jawaban; terjemahan cukup lewat perintah suara biasa).

**Koreksi performa & tampilan (K7)**:

| # | Koreksi | Penanganan |
|---|---|---|
| C1 | Jawaban Gemini kadang kosong — `maxOutputTokens` 2048 habis dipakai thinking model 2.5 | `thinkingConfig(budget 0)` untuk keluarga 2.5-flash + budget 8192 + tangga fallback 2 tahap + pesan error jelas (kode HTTP) |
| C2 | Tombol translate & sound dilarang | Dihapus dari bar atas & Pusat Kendali; `speakReply` selalu bersuara |
| C3 | Bubble menutupi animasi bawah | Hanya 3 pesan terakhir ditampilkan, makin tua makin pudar |
| C4 | Label orbit menumpuk reaktor | Orbit teks diganti halo murni canvas + dock status statis di bawah (nol tumpang tindih) |
| C5 | 15 MB terasa lag | Hex parallax via GPU layer (path tak digambar ulang), RMS throttled 30 Hz, kurangi draw per frame (tick 32, waveform 48, partikel 8), loop smoothing tidur saat stabil, APK release R8 + shrinkResources |

---

## 7. Roadmap (pasca v1.0.0 — revisi sedikit demi sedikit)

| Fase | Isi | Status |
|---|---|---|
| v1.0.0 | Full fitur + HUD Mark II + Pusat Kendali + koreksi K7 (C1–C5) | **Selesai** |
| F-next | Uji coba pak di HP → catat bug & rasa di §10 | Berjalan |
| F-next+1 | Gradle wrapper agar build lokal memungkinkan | Menunggu |
| F-next+2 | Polish lanjutan hasil pemakaian harian | Menunggu |

Setiap perubahan = 1 push ke GitHub → CI otomatis menghasilkan APK.

---

## 8. Build APK & Setup

**Dapat APK tanpa Android Studio**: tab **Actions** → workflow *Build AVI APK* → run hijau → **Artifacts** → `AVI-release-apk` → ekstrak (isinya `app-release.apk`) → install di HP.

> ⚠️ Karena APK ditandatangani debug key sementara dari CI, **uninstall dulu AVI lama** sebelum memasang APK baru (tanda tangan beda → Android menolak pasang di atas yang lama).

**Setup otak AVI**: https://aistudio.google.com/apikey → Create API key → aplikasi ⚙ Pengaturan → tempel key.

**Struktur repo** (saat ini):

```
AVI/
├── app/src/main/java/com/anztdree/avi/
│   ├── MainActivity.kt · AviViewModel.kt   # orkestrasi state chat+suara+HUD
│   ├── ai/      # Gemini SSE + ListModels + prompt kepribadian
│   ├── data/    # DataStore + riwayat
│   ├── speech/  # AviTts + AviStt + MicStreamer
│   └── ui/      # HUD Mark II + Pusat Kendali
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
| K5 | Fitur flagship & menu ide §2-G | **Semua flagship X1–X8 masuk** (pemilik: "semua itu wajib ada di versi 1.0.0, kerjakan semua sekaligus") | X1–X8 diimplementasi di v1.0.0 |
| K6 | Prioritas pengalaman | **Tampilan & pengaturan harus WAH, mewah, tetap ringan** | HUD Mark II + Pusat Kendali diimplementasi di v1.0.0 |
| K7 | Koreksi rasa pakai: API Gemini tak jalan, tombol translate/sound dilarang, bubble menutupi animasi, animasi bawah tumpang tindih, 15 MB terasa lag | **Perintah koreksi langsung** | C1–C5 dieksekusi; AVI wajib membacakan jawaban; terjemahan cukup lewat perintah suara |
| K8 | APK hasil R8 jadi 1,5 MB + tampilan terasa kaku | **"Saya mau tetap estetik bagus HD! Bukan dipangkas!"** | Optimasi TIDAK boleh mengurangi visual. Ring teks melingkar di sekeliling orb dipulihkan sebagai Halo Mark II (3 pita geometris anti-tumpang tindih); ukuran APK kecil = hasil R8, bukan pemangkasan fitur |
| K9 | Logika model: semua model gagal ("limit"/"no longer available") walau daftar model sukses di-fetch | **"Logika anda pasti salah"** | Diperbaiki: thinkingConfig hanya untuk keluarga 2.5-flash (2.0 & pro tidak boleh), fallback minimal untuk 400/404/500/503 dengan model yang sama, pesan 404 menjelaskan model dipensiunkan, daftar model diurutkan (stabil dulu, uji-coba bawah), dan fitur **Uji Model** — setiap model yang dipilih di-ping 1× dan statusnya ditampilkan (✓ SIAP / ✗ GAGAL + alasan asli Google) |
| K10 | Dock status statis & chip saran (cuaca/berita/dll) tidak estetik; diagnostik yang mengorbit reaktor dulu jauh lebih bagus; uji model terasa lambat dan gagal semua | **"Hapus total!! Full animasi HD, bukan minimalis"** | StatusDock + ChipRow **dihapus total**; diagnostik (JAM · BAT · RAM · NET · OTAK) kembali **mengorbit reaktor** sebagai Halo Mark III (3 pita geometris anti-tumpang tindih, zona reaktor diperbesar 232dp); Uji Model dipercepat (thinking off di keluarga flash, timeout ketat 10–35 dtk, saat 429 menunggu retryDelay Google lalu ulang 1×, alasan gagal tampil 4 baris) |
| K11 | Semua model tetap gagal ("loading lama, akhirnya gagal semua") — bukti: screenshot Uji Model di repo. Diuji langsung dengan key pemilik: Google menolak dengan **HTTP 403 "The Gemini API free tier is not available for this project"** untuk SEMUA model, dan model lama (2.5) ditutup untuk key baru (404, disarankan era 3.x). Artinya: **project API key pemilik tidak diberi akses gratis oleh Google — bukan bug daftar model** | **"Model anda gagal semua!! Lihat github saya sudah kirim gambarnya!!"** | (1) **DIAGNOSA KONEKSI** di Pusat Kendali: menguji kunci → daftar model → hak pakai dalam ±15 dtk, lalu menampilkan vonak + panduan perbaikan bertaut aktif (aktifkan penagihan aistudio.google.com/billing, buat key baru/project baru, atau ENABLE API via console). (2) Pesan 403/400/404 kini **menjelaskan akar masalah + jalan keluar** dalam bahasa Indonesia (bukan sekadar "gagal"). (3) App **berhenti cepat** saat penolakan tingkat-project (tidak mengulang-ulang model yang sama → tidak ada lagi loading panjang). (4) Auth selalu lewat **header `x-goog-api-key`** (mendukung key format baru `AQ.`). (5) Urutan model **sadar-generasi** (nomor generasi di-parse, 3.x otomatis di atas 2.x) + **pemilihan otomatis** model terbaik saat model lama tak ada di daftar. |
| K12 | APK 1,5 MB dianggap "setengah-setengah" | **"Kenapa full animasi cuma 1.5MB? Saya gk mau proyek ini setengah-setengah… 20 atau 50 MB!!"** | (1) **R8/minify & shrink resource dimatikan** — seluruh kode & pustaka ikut utuh. (2) **Konten HD dibundel**: 8 latar/tekstur AI-HD (nebula galaksi per tema MK1/MK2/MK3, nyala reaktor 2 varian, grid HUD, inti plasma, medan bintang), 6 font premium (Orbitron, Share Tech Mono, Rajdhani, Exo 2, JetBrains Mono), 6 efek suara HUD (boot sinematik, kirim, terima, galat, wake, live). (3) UI memakai semuanya: boot screen sinematik bersuara sadar-tema, latar nebula + grid di balik HUD, badge inti plasma di Pusat Kendali. Hasil: APK ±20 MB yang tetap ringan jalan (hemat daya tetap melucuti lapisan berat). |
| K13 | Key API baru (format `AQ.`) tetap gagal semua model di app + hasil koreksi UI chat terlihat "tambal sulam" seperti potongan gambar yang ditempel | **"API saya update… kenyataan juga sama aja error!! Koreksi antar muka bagian chat terlihat seperti tambal sulam jelek, saya mau full animasi HD bukan seperti potongan gambar yang ditempel"** | (1) **Jalur Gemini disamakan 100% dengan curl terbukti milik pemilik**: POST **non-streaming `:generateContent`** + header `x-goog-api-key` — jalur SSE/streaming (terindikasi penyebab "loading lama lalu gagal" pada sebagian jaringan seluler) **dihapus total**. (2) Uji sandbox: key `AQ.` terbukti **valid** (lolos auth Google; uji sandbox hanya berhenti di cek lokasi IP sandbox). (3) Alias `gemini-flash-latest`/`-lite-latest`/`-pro-latest` diungkit ke **puncak daftar model** (selalu terbuka untuk key baru). (4) **UI chat ditulis ulang total sebagai satu panggung hidup**: nebula ken-burns + vignette bernafas (gambar tidak lagi ditempel mati), 96 bintang berkelip 3 lapis paralaks, piringan reaktor berputar di balik orb, **riwayat obrolan penuh** (bukan 3 bubble terakhir) dengan bubble masuk ber-spring + tag peran PAK/AVI + tetesan mengetik + kursor kedip, dock input kaca bercahaya saat fokus, tombol mic beriak ganda, banner galat/live masuk-keluar beranimasi, chip status berdenyut. (5) Blokir filter Google (`promptFeedback.blockReason`) kini dilaporkan eksplisit. |
| K14 | Masih error + "diagnosa percuma, cuma lihat model 2.5" + bukti pemilik: nanobot & alat lokal lain work dengan key yang sama | **"dah lah masih aja error.. bikin diagnosa jugaaa percuma cuma lihat di versi model 2.5 doang.. tidak ada revisi duluu. teliti yg jelas — saya pakai nanobot atau yg lainnya yg butuh api key local smua bisa work"** | **MATRIKS UJI DARI SERVER AS (GitHub Actions, key pemilik sbg secret terenkripsi — bukan diagnosa dari HP lagi): SEMUA 46 model chat diuji satu per satu dgn curl spesifikasi pemilik.** Hasil kebenaran: (a) key `AQ.` **VALID** — ListModels OK; (b) **keluarga 2.5 DITUTUP Google utk key baru** (404 no-longer-available); (c) **alias `gemini-flash-latest` MENGHANG >40 dtk bahkan dr server AS** — dan kode lama mengungkit alias ini ke puncak + auto-pick memilihnya → INI AKAR "semua model gagal"; (d) **7 model terbukti hidup**: `gemini-3.6-flash`, `gemini-3.5-flash-lite`, `gemini-3.1-flash-lite(-preview)`, `gemini-3-flash-preview`, `gemini-flash-lite-latest`, `gemini-robotics-er`; (e) 22 model 429 kuota (pro/gambar/musik), 3.7-flash 503 kelebihan beban. **Revisi yg dijalankan:** (1) ungkit alias `-latest` DIHAPUS — urutan jujur per generasi; (2) **auto-pick MENGUJI dulu**: app ping turun daftar (maks 12) dan memilih model pertama ✓ SIAP; (3) **tombol UJI SEMUA MODEL** — matriks penuh di HP dgn progres x/y + ringkasan SIAP; (4) daftar obrolan difilter **murni-chat** (TTS/gambar/musik/live dikeluarkan — dulu ikut tampil & pasti 400); (5) **tangga adaptif 3 bentuk** per model: penuh → tanpa tools → **bentuk curl bukti pemilik tanpa `generationConfig`** (DTO diperbaiki: default `generationConfig` dulu menyelundup ke JSON krn `encodeDefaults`), turun tangga juga saat jawaban kosong (token dilahap thinking); (6) uji-model sadar-thinking utk 2.5/alias + timeout ketat 20 dtk (yang hang >20 dtk hampir pasti bermasalah); (7) pesan 500/503 dijelaskan (sisi Google); (8) SecureStore: key terenkripsi rusak → kosong bersih, bukan terkirim sbg key; (9) CloudTts pindah ke header auth; (10) **v1.0.1 (versionCode 6)** + versi tampil di HUD Pusat Kendali — pemilik bisa memastikan build terbaru terpasang. |
| K15 | "saya tidak lihat apk anda di update di github" — pembaruan APK tidak terlihat | **"saya tidak lihat apk anda di update di github"** | Fakta: APK sebenarnya **sudah terganti** di Release v1.0.0 (asset 22,9 MB, tag digeser ke commit terbaru) — tetapi **nama file selalu sama** (`AVI-v1.0.0.apk`) sehingga sulit dibedakan & halaman lama kerap tersangkut cache browser. **Revisi:** (1) **Nama file APK kini UNIK per build**: `AVI-v<versi>-<build>-<hash-commit>.apk` — jika nama berarti ada versi baru; (2) APK lama di release **dihapus otomatis** — hanya tersisa satu build terbaru; (3) **Catatan rilis ditulis ulang otomatis** pada tiap build: tabel "Build Terbaru" berisi nama file, versi+build, hash commit, **waktu build WIB**, dan ukuran; (4) Instruksi cek pembaruan tertulis di catatan rilis (refresh keras `Ctrl+F5` bila halaman tampak lama). |
| K16 | Status Uji Model "SIAP" tapi chat selalu **HTTP 429**, sementara nanobot/alat lokal jalan dgn key sama — pemilik minta BERHENTI revisi buta & cari akar masalah sampai ketemu | **"gemini model status siap .. waktu di chat error 429. pake nanobot dulu saja bisa .. stop revisi cari akar masalah sampai ketemu .. tanpa revisi sampai benar-benar jelas !! dan work dari sisi anda"** | **AKAR MASALAH DIBUKTIKAN (bukan teori) — matriks diferensial `DIAG-429` dari server AS (request identik dgn app): 4/4 model hidup menunjukkan pola telak** — S0 bentuk Uji Model (tanpa tools) = **200 OK**; S1 persis anak-tangga-1 chat (dengan `tools:[{"google_search":{}}]`) = **429 RESOURCE_EXHAUSTED instan (0,2 dtk)**; S2 persis S1 **tanpa tools** = **200 OK** dgn jawaban utuh. Artinya: **kuota grounding Google Search free tier = 0** — request ber-tools selalu ditolak instan, request tanpa-tools selalu hidup. Rantai sebab di app: default `webSearch=true` → tangga-1 selalu ber-tools → 429 → **429 tidak masuk daftar turun-tangga** (`400/404/500/503`) → chat berhenti total. Ini menjelaskan SEMUA gejala: Uji Model SIAP (tanpa tools), nanobot jalan (tanpa tools), chat selalu 429 (ber-tools). **Perbaikan minimal terarah (K16):** 429 pada request ber-tools kini WAJIB turun tangga ke bentuk tanpa-tools yang terbukti 200; 429 tanpa-tools tetap dilaporkan sbg kuota sungguhan. Tidak ada perubahan lain. Versi naik ke **v1.0.2 (build 7)**. |
| K17 | Riset web lewat Gemini DIHAPUS TOTAL — key gratis tidak akan pernah punya kuota grounding, jadi fitur itu hanya membuang penolakan; fitur utama wajib jalan: obrolan berjalan + buka aplikasi | **"hapus riset web total. lebih baik nanti pake provider lain seperti DDG. untuk saat ini wajib jalan fitur utama asisten saling obrolan yg berjalan. bisa buka aplikasi yg di suruh itu hal dasar wajib"** | **(1) RISET WEB DIHAPUS TOTAL**: field `webSearch`+`researchMode` & key DataStore-nya dihapus, DTO `GeminiTool`+field `tools` dihapus, tangga permintaan dipangkas dari 3 bentuk → **2 bentuk** (lengkap tanpa-tools → curl bukti), perintah suara "riset…" dihapus, MODE RISET & saran "gunakan pencarian web" dihapus dari prompt kepribadian (diganti: jawab jujur + sebut data bisa jadi bukan terbaru), pesan 429 tidak lagi menyarankan kurangi riset. Hasil: **setiap request chat kini SELALU jalur tanpa-tools yang terbukti 200 oleh matriks server AS** — tidak ada lagi pemborosan penolakan instan. Pencarian web di masa depan: provider lain (mis. DDG), bukan lewat Gemini. **(2) BUKA APLIKASI (hal dasar) DIKUNCI WAJIB-JALAN**: `<queries>` LAUNCHER sudah terpasang di manifest (wajib utk Android 11+ — tanpa itu daftar app tersembunyi), kata kerja diperluas (buka/open/**jalankan/bukakan**), pembersih kata diberi **word-boundary** (nama app seperti "buku" tidak rusak lg oleh potongan kata "bu"), jalur cadangan tag `[BUKA: ...]` dr model tetap hidup. **(3) Obrolan berjalan**: STT id-ID → tangga 2 bentuk → TTS selalu membacakan — seluruh rantai kini bebas tools. Versi naik ke **v1.0.3 (build 8)**. |
| K18 | Panggilan custom WAJIB ada (bukan cuma "Pak"); bagian Status Sistem di Pusat Kendali dihapus; teks muter di orb kembali GAK BERATURAN seperti versi awal | **"panggilan custom harus ada dong, jangan cuma pak doang, lalu bagian status sistem di pengaturan itu hapus aja. teks muter di orb bagus di versi awal muter nya gak beraturan"** | **(1) PANGGILAN CUSTOM**: panel baru **PEMILIK → PANGGILAN SAYA** di Pusat Kendali (tersimpan di DataStore; bawaan "Pak"; kosong otomatis kembali "Pak"). Seluruh lapisan bicara AVI kini mengikuti panggilan itu: prompt kepribadian (contoh "Siap <panggilan>!" dibuat dinamis + larangan memanggil "Pak" bila bukan panggilannya), sapaan boot, greeting per waktu, semua jawaban lokal instan (jam, tanggal, baterai, RAM, penyimpanan, jaringan, kondisi markas), balasan buka-aplikasi, fallback jawaban kosong, dan konfirmasi hapus riwayat. Pembersih kata perintah buka-app juga mengenal panggilan custom — hanya dibuang bila berdiri di awal/akhir kalimat (posisi sapaan wajar) supaya nama aplikasi yang kebetulan sama tidak ikut terbuang. **(2) STATUS SISTEM DIHAPUS** dari panel SISTEM Pusat Kendali (gauge BATERAI/RAM/PENYIMPANAN/JARINGAN) — telemetri perangkat tetap hidup mengorbit reaktor di layar utama; panel SISTEM kini berisi versi APK + info perangkat + pemakaian API + tombol hapus riwayat. **(3) TEKS MUTER ORB GAK BERATURAN**: orbit teks diagnostik kini berkecepatan berombak organik (superposisi dua gelombang sinus periodik terhadap siklus animasi) — kadang melaju, kadang melambat, tanpa lompatan & tanpa alokasi per-frame. Bonus: header Pusat Kendali menampilkan **versi build dinamis** (tidak lagi tercetak manual "v1.0.0"). Versi naik ke **v1.0.4 (build 9)**. |
| K19 | Fitur dasar sudah work; pemilik minta fokus ke TEMA UI & TATA LETAK agar SEKELAS JARVIS, plus FITUR KAYA walau belum work | **"nah ini baru work fitur dasarnya.. tapi lebih baik fokus tema UI dan tata letak agar sekelas seperti jarvis . tambahkan fitur yg kaya walaupun fitur itu belum work"** | **(1) BINGKAI HUD KELAS JARVIS** (lapisan layar utama, bisa dimatikan): 4 bracket sudut bercahaya dgn titik sudut emas, rel samping ber-tick merambat, sapuan hologram melintasi layar tiap ±11 dtk, denyut sudut — murni Canvas tanpa alokasi per frame, mati otomatis di hemat daya. **(2) BAR KOMANDO BARU**: wordmark AVI ber-glow, chip status, **jam hidup** telemetri, tombol **DECK**; chip model pindah ke deretan chip pintasan di atas dock. **(3) DECK MODUL — fitur kaya**: panel holo meluncur dari bawah berisi 8 modul — WAKTU (jam+tanggal berjalan per detik), OTAK (provider/model/pemakaian + pintasan atur), DIAGNOSTIK MARKAS (4 gauge arc nyata: baterai/RAM/penyimpanan/jaringan), PINTASAN MARKAS (6 perintah lokal instan), PROTOKOL (jalankan dr deck), MEMORI AVI, plus 2 modul **badge SEGERA** yang tampilannya sudah hidup: MODUL CUACA & PEMUTAR MEDIA — sesuai pesanan pemilik: fitur kaya dipajang walau belum work, antrean modul berikutnya tertera. **(4) TEMA BARU MK-IV \"EIDOLON\"** (hijau-hologram) melengkapi MK-I/II/III. **(5) CHIP PINTASAN NYATA** di layar utama (jam/tanggal/baterai/kondisi HP/penyimpanan) — semua dijawab lokal instan tanpa kuota. Semua modul nyata memakai data yang SUDAH TERBUKTI (DeviceActions/DataStore) — jalur obrolan yang barusan work TIDAK disentuh. Versi naik ke **v1.1.0 (build 10)**. |
| K20 | Menu pengaturan terasa TERLALU SEDERHANA — minta rombak total tata letak + perkaya fitur | **"Menu pengaturan terlalu sederhana.. bisa rombak total pengaturan, baik tata letak, perkaya fitur disana juga"** | **PUSAT KENDALI DIRAMBAK TOTAL — dari satu gulungan panjang jadi MISI KENDALI 5 TAB** (widget baru di `SettingsWidgets.kt`): **(1) HEADER SINEMATIK**: inti plasma hidup (cincin tick emas/aksen berputar + denyut napas), jam markas WIB hidup per detik, badge panggilan pemilik, + STRIP STATUS 4 petak berindikator (OTAK SIAP/OFF · SUARA HIDUP/SENYAP · PEMILIK · TAB MISI). **(2) NAVIGASI 5 TAB MISI** — OTAK · SUARA · GAYA · HUD · SISTEM — ikon+label, garis indikator aksen MELUNCUR antar tab, getar halus, transisi hologram geser-fade; state lintas tab tidak hilang. **(3) TAB OTAK**: kartu INTI OTAK (model aktif, penyedia, pemakaian hari ini, jumlah model siap), provider & kunci (key tersembunyi + muat daftar + Buka AI Studio), daftar model + UJI SEMUA, DIAGNOSA KONEKSI. **(4) TAB SUARA**: toggle percakapan beruntun, dial kecepatan/nada + **4 PRESET VOKAL** (LEMBUT/NORMAL/CEPAT/DALAM), cloud TTS dgn **deskripsi karakter 8 suara** (Kore — tegas & netral, dsb.), tes suara, ruang tes mendengar. **(5) TAB GAYA**: panggilan custom + **chip cepat** (Pak/Mas/Bang/Kak/Om/Cak) yang ikut menyinkronkan kolom, gaya bicara + deskripsi karakter tiap gaya, MEMORI & PROTOKOL pindah ke sini dgn badge jumlah. **(6) TAB HUD**: kartu tema MARK 2×2 (bola inti pratinjau per palet + julukan: Cyan Inti Ark / Hot Rod Merah / Aureus Emas / Eidolon Hologram) + 6 toggle efek. **(7) TAB SISTEM**: **RING KUOTA API** (perkiraan ±1500/hari, hitung lokal) + statistik model siap/memori/protokol, **AKSI CEPAT 6 petak** (RIWAYAT/UJI SUARA/TES DENGAR/DIAGNOSA/MUAT MODEL/AI STUDIO — diagnosa & muat model otomatis lompat ke tab OTAK, tes dengar lompat ke tab SUARA), **MODUL BERIKUTNYA** 6 kartu badge SEGERA (Wake Word "Hey AVI", Cuaca Langsung, Berita Harian, Penerjemah, Kalender, Rumah Pintar — dipajang sesuai kebijakan fitur-kaya), TENTANG MARKAS (versi/perangkat/Android + tautan repo GitHub). Latar Pusat Kendali kini SATU KELUARGA dgn layar obrolan: nebula per-Mark + hex grid parallax + bingkai HUD. Seluruh fungsi lama dipertahankan 1:1 — jalur obrolan yang work TIDAK disentuh. Versi naik ke **v1.2.0 (build 11)**. |
