# RENCANA MASTER — AVI (Asisten Virtual Indonesia)

> Status: **MENUNGGU PERSETUJUAN PEMILIK** — tidak ada baris kode implementasi yang boleh ditulis sebelum dokumen ini disetujui.
> Acuan: instruksi pemilik di percakapan + audit source code aktual di repo ini.

---

## 1. Ringkasan Eksekutif

AVI adalah asisten suara pribadi untuk Android, native (Kotlin), dengan kepribadian pelayan
Indonesia: *"Hay pak… Siap pak!"*. Semua kecerdasan berjalan di **cloud (Google Gemini API)**;
perangkat hanya menangani UI, mikrofon, speaker, dan penyimpanan lokal yang ringan.

Dokumen ini mendefinisikan: prinsip wajib, hasil audit kondisi kode saat ini, spesifikasi
fitur **fetch model dinamis dari Google** (permintaan utama pemilik), arsitektur target,
roadmap fase, dan strategi build APK.

---

## 2. Prinsip & Batasan Wajib (dari pemilik)

| # | Aturan | Konsekuensi teknis |
|---|---|---|
| 1 | **Native Android APK** — bukan wrapper/PWA/WebView/Capacitor | Kotlin + Jetpack Compose; proyek Gradle standar di root repo |
| 2 | **Nol LLM lokal** — HP tidak kuat | Tidak ada inferensi on-device. STT/TTS = layanan sistem Android (ringan, bukan model besar). Semua AI lewat API cloud |
| 3 | **Model harus bisa di-fetch dari Google** — pemilik pilih sendiri model up-to-date | Layar Pengaturan memanggil `ListModels` Gemini; model aktif disimpan & dipakai semua fitur. **Tidak boleh ada model yang di-hardcode** |
| 4 | **Repo hanya berisi source code AVI** | Tanpa scaffold Next.js/Caddy/Prisma/infra sandbox. Riwayat branch `main` dibersihkan total |
| 5 | **Setiap milestone di-push ke GitHub** | Mitigasi sandbox yang sering reset |
| 6 | **Tidak ada implementasi sebelum disuruh** | Setiap fase dimulai hanya setelah persetujuan eksplisit pemilik |

---

## 3. Audit Kondisi Saat Ini (jujur, hasil pemeriksaan kode)

### 3.1 Yang sudah ada (pewarisan v1.0.0)

| Komponen | File | Keterangan |
|---|---|---|
| Proyek Gradle | `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties` | AGP + Kotlin 2.x, compileSdk 35, minSdk 26 (Android 8.0+) |
| UI HUD | `ui/ReactorOrb.kt`, `ui/ChatScreen.kt`, `ui/SettingsScreen.kt` | Orb arc reactor 4 state: siaga/mendengar/berpikir/bicara |
| Orkestrasi | `AviViewModel.kt` | State machine percakapan + loop tanya-jawab |
| Klien Gemini | `ai/GeminiClient.kt` | Streaming SSE, grounding `google_search`, fallback tanpa tools |
| Klien alternatif | `ai/OpenAiCompatClient.kt` | Provider kompatibel OpenAI (OpenRouter/Groq) — model diketik manual |
| Kepribadian | `ai/SystemPrompts.kt` | Gaya "Pak" / "Siap pak" |
| Suara | `speech/AviTts.kt`, `speech/AviStt.kt` | TTS id-ID + SpeechRecognizer HUD kustom |
| Gambar | `image/ImageGen.kt` | Pollinations + parser tag `[GAMBAR:]` |
| Data | `data/SettingsRepository.kt` (DataStore), `data/ChatHistoryRepository.kt` (file JSON) | Pengaturan + riwayat lokal |
| CI | `.github/workflows/build-android.yml` | Build APK otomatis tiap push → artifact `AVI-debug-apk`; Release v1.0.0 sudah ada |

### 3.2 Masalah yang ditemukan (harus dibereskan — MENUNGGU PERINTAH)

| # | Masalah | Lokasi | Dampak |
|---|---|---|---|
| M1 | **Model Gemini hardcode** `"gemini-2.0-flash"` | `GeminiClient.kt:47` | Melanggar Prinsip #3; model cepat usang, pemilik tak bisa memilih |
| M2 | Tidak ada pemanggilan `ListModels`, tidak ada field `geminiModel` di `AviSettings` | `ai/`, `data/` | Tak ada mekanisme pemilihan model sama sekali |
| M3 | Image generation pakai Pollinations (pihak ketiga), bukan dari Google | `image/ImageGen.kt` | Inkonsisten dengan prinsip "AI dari Google"; perlu keputusan pemilik |
| M4 | Riwayat chat = file JSON, bukan database | `data/ChatHistoryRepository.kt` | Renti untuk v1, tapi rawan korup & tak bisa query; idealnya Room |
| M5 | Tidak ada Gradle Wrapper (`gradlew`, `gradle/wrapper/`) | root | Build lokalbutuh install Gradle manual; CI pin versi 8.10.2 secara manual |
| M6 | API key penyimpanan biasa (DataStore plaintext) | `data/SettingsRepository.kt` | Lebih aman di EncryptedSharedPreferences |

### 3.3 Yang SUDAH dibereskan (restrukturisasi ini, tanpa menyentuh kode fitur)

- ✅ Semua sampah non-proyek dihapus dari repo & dari riwayat `main` (orphan commit): `src/`, `public/`, `prisma/`, `db/`, `examples/`, `tests/`, `download/`, `mini-services/`, `.zscripts/`, `Caddyfile`, `package.json`, `bun.lock`, konfigurasi Next/Tailwind, **`.env`** (berbahaya jika kebawa).
- ✅ Proyek Android dipindah dari `android/` ke **root repo** (struktur standar — repo bisa langsung dibuka Android Studio).
- ✅ `.gitignore` root baru (Android + kredensial + artefak sandbox).
- ✅ Workflow CI diperbarui mengikuti lokasi baru (path `android/` → root).
- ✅ Dokumentasi: `README.md`, `docs/RENCANA.md` (ini), `docs/SPESIFIKASI.md`, `docs/RIWAYAT-V1.md`.

---

## 4. SPESIFIKASI: Fetch Model Dinamis dari Google (Prioritas #1)

> Ini jawaban langsung atas permintaan: *"terkait model harus bisa fetch model untuk google,
> agar saya sendiri bisa pilih model yg up-to-date."*

### 4.1 Endpoint

```
GET https://generativelanguage.googleapis.com/v1beta/models?key={API_KEY}
```

Respons (diperingkas):

```json
{
  "models": [
    {
      "name": "models/gemini-2.5-flash",
      "displayName": "Gemini 2.5 Flash",
      "description": "…",
      "inputTokenLimit": 1048576,
      "supportedGenerationMethods": ["generateContent", "countTokens", …]
    }
  ]
}
```

### 4.2 Perilaku aplikasi (target)

1. **Pengaturan → Bagian "Model AI (Google)"** menampilkan model aktif + tombol **「Muat Daftar Model」**.
2. Saat tombol ditekan: panggil `ListModels` → tampilkan daftar **terbaru dari server** (nama tampilan + deskripsi singkat + ukuran konteks bila ada).
3. Filter & penanda:
   - Hanya model dengan `supportedGenerationMethods` memuat `generateContent` yang bisa dipilih untuk chat.
   - Model kemampuan gambar ditandai badge khusus (dipakai untuk fitur gambar bila pemilik memilih opsi "gambar via Google").
4. Pemilik memilih → disimpan ke DataStore (`geminiModel`) → **semua fitur** (chat, search grounding, gambar bila aktif) memakai model terpilih.
5. **Fallback**: jika fetch gagal (offline/key salah), tampilkan error jelas + tetap bisa mengisi nama model manual.
6. **Aturan keras: tidak boleh ada string model yang di-hardcode** di `GeminiClient.kt`. Nilai default awal boleh kosong → aplikasi memaksa pemilik memilih model saat onboarding.

### 4.3 Perubahan teknis yang akan dilakukan (saat disetujui)

| File | Perubahan |
|---|---|
| `data/SettingsRepository.kt` | Tambah field `geminiModel: String` + key DataStore |
| `ai/` baru: `ModelCatalog.kt` | DTO `ListModels` + fungsi `fetchModels(key)` |
| `ai/GeminiClient.kt` | Hapus hardcode; pakai `settings.geminiModel` |
| `ui/SettingsScreen.kt` | UI daftar model + tombol muat + status loading/error |
| `AviViewModel.kt` | Alur onboarding: key → fetch model → pilih model |

---

## 5. Arsitektur Target

```
┌───────────────── PERANGKAT ANDROID (native Kotlin) ─────────────────┐
│                                                                     │
│  UI (Jetpack Compose, HUD)          Data lokal (ringan)             │
│  ├─ Obrolan + Orb reaktor           ├─ DataStore: API key,          │
│  ├─ Galeri gambar                   │   MODEL TERPILIH, preferensi  │
│  └─ Pengaturan                      └─ Riwayat chat (v2: Room)      │
│       └─「Muat Daftar Model」──┐                                    │
│                                │                                    │
│  Voice Engine (layanan sistem — ringan, BUKAN LLM)                  │
│  ├─ STT: android.speech.SpeechRecognizer (id-ID)                    │
│  └─ TTS: android.speech.tts.TextToSpeech (id-ID)                    │
│                                                                     │
└──────────────┬──────────────────────────────────────────────────────┘
               │ HTTPS (OkHttp + kotlinx.serialization)
               ▼
   Google Generative Language API  ── SEMUA AI DI CLOUD ──
   ├─ GET  /v1beta/models                      → daftar model terbaru (pemilik pilih)
   ├─ POST /v1beta/models/{terpilih}:streamGenerateContent
   │      ├─ systemInstruction: kepribadian AVI
   │      └─ tools: google_search              → info terkini
   └─ model gambar (dari daftar, opsional)     → gambar AI
```

**Siklus suara:** tombol reaktor → STT dengar (id-ID) → prompt = kepribadian + riwayat →
`streamGenerateContent` (model terpilih) → TTS bacakan → kembali mendengar (mode beruntun).

---

## 6. Roadmap Fase

> Setiap fase = 1 milestone + 1 push. Fase dimulai **hanya setelah disetujui**.

| Fase | Isi | Hasil terukur |
|---|---|---|
| **F1** | **Fetch model dinamis dari Google** (bagian 4) + hapus hardcode | Pemilik bisa memilih model terbaru sendiri dari dalam aplikasi |
| F2 | Gradle Wrapper + simpan API key terenkripsi (M5, M6) | Build lokal jalan tanpa install Gradle; key lebih aman |
| F3 | Riwayat chat → Room + sesi (M4) | Riwayat stabil, bisa dihapus per sesi |
| F4 | Keputusan + implementasi gambar: model gambar Google (dari daftar) vs Pollinations (M3) | Fitur gambar konsisten dengan pilihan pemilik |
| F5 | Polish HUD (animasi, tema) + rilis **v1.1.0** | APK baru di Releases |

Catatan: penomoran bisa diubah pemilik. F1 adalah prioritas mutlak sesuai instruksi.

---

## 7. Stack Teknologi

| Lapisan | Teknologi |
|---|---|
| Bahasa / UI | Kotlin, Jetpack Compose + Material 3 |
| Jaringan | OkHttp + kotlinx.serialization (SSE streaming) |
| Penyimpanan | DataStore (pengaturan), file JSON → Room (v2), EncryptedSharedPreferences (key) |
| Asinkron | Coroutines + Flow |
| Gambar | Coil |
| CI/CD | GitHub Actions → artifact APK → Releases |

---

## 8. Build & Rilis

- **Otomatis**: push ke `main` (path proyek/workflow) → Actions build `assembleDebug` → artifact `AVI-debug-apk`; rilis bertag → asset APK di Releases.
- **Lokal (opsional)**: buka root repo di Android Studio → Run (setelah F2, cukup `./gradlew assembleDebug`).
- v1.0.0 tetap tersedia di Releases; riwayat lama hanya tersemat pada tag tersebut (branch `main` sudah bersih). Pemilik boleh minta tag lama dihapus agar benar-benar steril.

## 9. Keamanan

- `.env` lama sudah dikeluarkan dari repo & riwayat `main`.
- **PAT GitHub yang pernah dibagikan di chat sudah terekspos — sangat disarankan di-regenerate** setelah fase stabil, lalu update remote.
- API key Gemini milik pemilik, disimpan hanya di perangkat (ditingkatkan ke terenkripsi di F2); tidak pernah hardcode / masuk repo.
- Tidak ada kredensial apa pun di workflow CI.

## 10. Aturan Kerja (dikunci)

1. Tidak ada implementasi sebelum pemilik menyetujui rencana / memerintahkan fase.
2. Satu fase = satu push; push hanya saat milestone selesai.
3. Repo hanya berisi source AVI + docs — disiplin dijaga lewat `.gitignore` dan review sebelum push.
4. Semua perubahan besar dicatat di dokumen ini (bagian roadmap) dan `worklog` internal.
