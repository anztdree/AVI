# AVI — Asisten Virtual Indonesia

> Asisten suara pribadi ala Jarvis, jiwa Indonesia: **"Hay pak… Siap pak!"**

**100% Native Android** (Kotlin + Jetpack Compose) — bukan wrapper, bukan WebView.
**AI 100% cloud** (Google Gemini) — HP spek rendah pun kuat, nol model di perangkat.
**Model dinamis** — daftar model di-*fetch* langsung dari Google; pemilik pilih sendiri yang paling up-to-date.

**Status: v1.0.0 FULL FEATURE — RILIS.** Build CI hijau, APK terpasang di [Releases](https://github.com/anztdree/AVI/releases/download/v1.0.0/AVI-v1.0.0.apk). Seluruh rencana (HUD Mark II, Pusat Kendali, fetch model dinamis, memori, aksi perangkat, flagship X1–X8) dikerjakan sekaligus dalam satu rilis sesuai keputusan pemilik. Revisi lanjutan dilakukan sedikit demi sedikit.

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

### 4.4 Pusat Kendali — Pengaturan ala Mission Control

> Pengaturan BUKAN daftar datar. Ini "ruang kendali markas":

| Komponen | Detail |
|---|---|
| **Panel Sistem** | Disusun per sistem: `OTAK` · `SUARA` · `TAMPILAN` · `MEMORI` · `SISTEM` — dengan indikator status hidup per sistem |
| **Toggle Reaktor** | Saklar beranimasi gaya reaktor + slider gaya "dial tenaga" |
| **Kartu Model** | Pemilih model tampil sebagai kartu inti daya: nama, ukuran konteks, kemampuan — pemilik "memasang reaktor" |
| **Ruang Tes Suara** | Tes STT & TTS instan dengan feedback visual; dengar contoh voice sebelum memilih |
| **Gauge Diagnostik** | Baterai / RAM / penyimpanan / jaringan sebagai gauge HUD hidup + pemakaian API harian |
| **Live Preview** | Ganti tema/efek langsung terlihat di belakang panel — tanpa restart |

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
| K12 | APK 1,5 MB dianggap "setengah-setengah" | **"Kenapa full animasi cuma 1.5MB? Saya gk mau proyek ini setengah-setengah… 20 atau 50 MB!!"** | (1) **R8/minify & shrink resource dimatikan** — seluruh kode & pustaka ikut utuh. (2) **Konten HD dibundel**: 5 latar/tekstur AI-HD (nebula galaksi, nyala reaktor, grid HUD, inti plasma, medan bintang), 6 font premium (Orbitron, Share Tech Mono, Rajdhani, Exo 2, JetBrains Mono), 6 efek suara HUD (boot sinematik, kirim, terima, galat, wake, live). (3) UI memakai semuanya: boot screen sinematik bersuara, latar nebula + bintang di balik HUD, badge inti plasma di Pusat Kendali. Hasil: APK puluhan MB yang tetap ringan jalan (hemat daya tetap melucuti lapisan berat). |
