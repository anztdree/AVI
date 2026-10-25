# AVI — Asisten Virtual Indonesia

> Asisten suara pribadi ala Jarvis, jiwa Indonesia: **"Hay pak… Siap pak!"** 🦾

**100% Native Android** (Kotlin + Jetpack Compose) — bukan wrapper, bukan WebView.
**AI 100% cloud** (Google Gemini API) — HP spek rendah pun kuat, karena tidak ada model yang berjalan di perangkat.

## Status Proyek

| Tahap | Status |
|---|---|
| Pembersihan & restrukturisasi repo | ✅ Selesai |
| Rencana master + spesifikasi | ✅ `docs/RENCANA.md`, `docs/SPESIFIKASI.md` |
| Implementasi v1 (chat suara dasar) | ✅ Terlanjur ada — lihat `docs/RIWAYAT-V1.md` |
| **Sinkronisasi kode dengan rencana** (prioritas: fetch model dinamis dari Google) | ⏸️ **MENUNGGU PERSETUJUAN PEMILIK** |

> ⚠️ Aturan kerja: **tidak ada implementasi baru sebelum pemilik menyetujui rencana.**

## Dokumentasi

| Dokumen | Isi |
|---|---|
| [`docs/RENCANA.md`](docs/RENCANA.md) | Rencana master: prinsip, audit kondisi, arsitektur, spesifikasi fetch model Google, roadmap, build |
| [`docs/SPESIFIKASI.md`](docs/SPESIFIKASI.md) | Kepribadian AVI, siklus suara, spesifikasi HUD |
| [`docs/RIWAYAT-V1.md`](docs/RIWAYAT-V1.md) | Catatan implementasi v1 (sebelum restrukturisasi) |

## Prinsip Wajib

1. **Native Android** — Kotlin + Jetpack Compose. Bukan PWA, bukan Capacitor/WebView.
2. **AI 100% cloud** — nol inferensi on-device. STT/TTS memakai layanan sistem Android (ringan, bukan LLM).
3. **Model dinamis dari Google** — aplikasi men-*fetch* daftar model terbaru dari endpoint `ListModels`; pemilik memilih model aktif sendiri, tanpa perlu update aplikasi.
4. **Repo higienis** — hanya berisi source code AVI. Tanpa scaffold web/infra yang tidak terkait.

## Cara Dapat APK (tanpa Android Studio)

1. Buka tab **Actions** di repo ini → workflow **Build AVI APK** → tunggu run hijau ✅
2. Klik run → **Artifacts** → unduh `AVI-debug-apk`
3. Ekstrak → pindahkan `app-debug.apk` ke HP → install (izinkan sumber tidak dikenal)

Rilis stabil: [Releases](https://github.com/anztdree/AVI/releases) (v1.0.0 tersedia).

## Setup Otak AVI (sekali saja)

1. Buka https://aistudio.google.com/apikey → **Create API key**
2. Aplikasi AVI → ⚙ Pengaturan → tempel API key
3. Selesai — AVI siap diajak ngobrol suara.

## Struktur Repo

```
AVI/
├── app/src/main/java/com/anztdree/avi/
│   ├── MainActivity.kt
│   ├── AviViewModel.kt      # orkestrasi state: chat + suara + HUD
│   ├── ai/                  # klien Gemini (streaming SSE) + OpenAI-compat + prompt
│   ├── data/                # DataStore settings + riwayat chat
│   ├── speech/              # AviTts (suara) + AviStt (pendengar)
│   ├── image/               # ImageGen
│   └── ui/                  # Compose: HUD orb reaktor, chat, pengaturan
├── app/src/main/res/        # tema gelap, ikon adaptive
├── .github/workflows/       # CI build APK otomatis
├── docs/                    # rencana & spesifikasi
├── build.gradle.kts · settings.gradle.kts · gradle.properties
└── README.md
```
