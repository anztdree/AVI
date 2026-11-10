# AVI — Asisten Virtual Android

AVI adalah **asisten virtual berbasis AI** untuk Android: bicara bebas (STT & TTS
ramah bahasa Indonesia), otak agen cloud (Google Gemini + kompatibel OpenAI)
dengan **perkakas perangkat nyata**, Pusat Kendali 5 tab, dan telemetri orbit.

## Isi repo — KHUSUS PROYEK APK

| Jalur | Isi |
|---|---|
| `android-app/` | Source aplikasi Android AVI (Kotlin + Jetpack Compose, paket `com.anztdree.avi`) |
| `build.gradle.kts` · `settings.gradle.kts` · `gradle.properties` | Gradle root proyek |
| `.github/workflows/build-android.yml` | CI: build Release APK + perbarui Release `v1.0.0` |

> **Catatan tegas:** repo ini murni proyek APK. Plumbing sandbox pengembangan
> lokal (Next.js/bun, mockup tampilan, alat bantu, log) sengaja **tidak
> di-commit** — `.gitignore` memakai whitelist, jadi file di luar daftar di
> atas tidak akan pernah masuk GitHub walau di-`git add -A`.

## Unduh APK

Setiap perubahan `android-app/**` memicu build otomatis. APK terbaru selalu ada
di **GitHub Release `v1.0.0`** dengan nama file unik per build:
`AVI-v{versi}-{kode build}-{hash commit}.apk`

🔗 https://github.com/anztdree/AVI/releases/tag/v1.0.0

## Build lokal

```bash
gradle assembleRelease
# hasil: android-app/build/outputs/apk/release/app-release.apk
```

## Kemampuan utama

- 🔊 **Bicara bebas** "Hai, Pak…" — STT & TTS selalu aktif, ramah bahasa Indonesia
- 🧠 **AI cloud Google Gemini** — auth `X-goog-api-key`, daftar model dinamis
  (keluarga 1.5 / 2.0 / 2.5 / alias `-latest`), plus klien kompatibel OpenAI
- 🤖 **Mesin agen** pikir–aksi–hasil (maks 6 langkah) dengan 16 perkakas nyata:
  diagnostik, buka aplikasi, volume, senter, URL, pencarian web, share,
  pengaturan, telepon/SMS draf, kalkulator aman, ingatan, dan lainnya
- 🎛️ **Pusat Kendali 5 tab** — OTAK / SUARA / GAYA / SISTEM / AGENT
  (otoritas 3 tingkat AMAN/BAKU/PENUH, mode AUTO/SELALU/MATI, log aksi)
- 🛰️ **Orbit telemetri** di sekeliling orb — baterai, memori, penyimpanan,
  jaringan, jam
- 🌌 **Tampilan kelas Jarvis** — nebula animasi, bintang paralaks, piringan
  reaktor, dock kaca beriak, tema Mark

## Riwayat singkat

K19 → K22: HUD Jarvis, Pusat Kendali dirambah, Ingatan AVI, dan AVI jadi
AI AGEN (function calling terbukti di 2 model free-tier). Detail lengkap ada
di pesan commit.
