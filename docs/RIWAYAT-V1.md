# AVI — Asisten Virtual Indonesia (Native Android)

Asisten suara pribadi ala Jarvis, gaya Indonesia: **"Hay pak… Siap pak!"** 🦾

**100% Native Android** (Kotlin + Jetpack Compose) — bukan wrapper/webview.
**LLM via cloud API** — HP spek rendah pun kuat, karena AI diproses di server.

## Fitur

| Fitur | Keterangan |
|---|---|
| 💬 Obrolan pintar | Streaming jawaban real-time |
| 🎤 Perintah suara | SpeechRecognizer Android (id-ID), HUD kustom |
| 🔊 Jawaban bersuara | TextToSpeech Android (bahasa Indonesia) |
| 🔄 Mode percakapan | Tanya-jawab suara beruntun otomatis |
| 🔍 Cari info terkini | Gemini + Google Search grounding |
| 🎨 Buat gambar | "AVI, buatkan gambar…" (Pollinations, gratis) |
| 🖥️ HUD Iron Man | Orb arc reactor animasi: siaga/mendengar/berpikir/bicara |
| 🗄️ Riwayat chat | Tersimpan lokal di HP |

## Cara dapat APK (paling mudah, tanpa Android Studio)

1. Buka tab **Actions** di repo GitHub ini
2. Pilih workflow **Build AVI APK** → tunggu run selesai (hijau ✅)
3. Klik run → bagian **Artifacts** → unduh **AVI-debug-apk**
4. Ekstrak zip → pindahkan `app-debug.apk` ke HP → install
   (izinkan "Install dari sumber tidak dikenal" di pengaturan)

Build berjalan otomatis setiap kali folder `android/` ada perubahan.

## Setup otak AVI (sekali saja, gratis)

1. Buka https://aistudio.google.com/apikey → login Google → **Create API key**
2. Buka aplikasi AVI → ikon ⚙ Pengaturan → tempel API key
3. Selesai! AVI langsung bisa diajak ngobrol suara.

Alternatif: pakai API kompatibel OpenAI (OpenRouter gratis, Groq, dll) di pengaturan.

## Struktur

```
android/
├── app/src/main/java/com/anztdree/avi/
│   ├── MainActivity.kt
│   ├── AviViewModel.kt          # orkestrasi state: chat + suara + HUD
│   ├── ai/                       # Gemini streaming SSE + OpenAI-compat + prompt AVI
│   ├── data/                     # DataStore settings + riwayat file JSON
│   ├── speech/                   # AviTts (suara) + AviStt (pendengar)
│   ├── image/ImageGen.kt         # Pollinations image URL + parser tag [GAMBAR:]
│   └── ui/                       # Compose: HUD, orb reaktor, chat, pengaturan
└── app/src/main/res/             # tema gelap, ikon arc reactor adaptive
```

## Build lokal (opsional)

Buka folder `android/` di Android Studio → Run. Min SDK 26 (Android 8.0).
