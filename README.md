# AVI — Asisten Virtual Android

AVI adalah **asisten virtual berbasis AI** untuk Android: bicara bebas (STT & TTS
ramah bahasa Indonesia), otak agen cloud (Google Gemini + kompatibel OpenAI)
dengan **perkakas perangkat nyata** dan Pusat Kendali 5 tab.

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

- 🔊 **Bicara bebas** "Hai, Pak…" — STT & TTS ramah bahasa Indonesia;
  VOKAL JAWABAN bisa disaklarkan (jawaban tetap tampil di chat), SUARA LANGSUNG
  = sesi LIVE dengar-tanpa-henti yang benar-benar MULAI/BERHENTI
- 🔊 **Input SUARA SAJA (P47 — keputusan pemilik)** — papan ketik DIMATIKAN
  total: tidak ada lapangan tulis, tidak bisa diklik, tanpa tulis manual.
  Bar bawah murni CERMIN SUARA: teks parsial/hasil pengenalan suara tampil
  di situ. Saklar suara = ORB atau tombol MIC; perintah preset tetap hidup
  di Deck → PINTASAN MARKAS
- 🧠 **AI cloud Google Gemini** — auth `X-goog-api-key`, daftar model dinamis
  (keluarga 1.5 / 2.0 / 2.5 / alias `-latest`), plus klien kompatibel OpenAI
- 🔑 **P48: CUKUP TEMPEL API KEY — OTAK TERPASANG OTOMATIS** — kunci ditempel,
  AVI sendiri memuat daftar model, menguji kandidat teratas, dan memilih yang
  benar-benar menjawab. Tidak ada lagi "model belum dipilih" yang membuat
  permintaan mati diam setelah input kunci
- 🔑 **P48: MULTI API KEY GEMINI + FAILOVER OTOMATIS** — kunci utama + kunci
  cadangan (2, 3, hingga 8 buah) di Pusat Kendali → OTAK. Kuota satu kunci
  habis (429), kunci ditolak (403/invalid) → AVI lanjut otomatis ke kunci
  berikutnya, tanpa berhenti, tanpa disuruh; tombol UJI SEMUA KUNCI menunjukkan
  status tiap kunci. Semua kunci tersimpan terenkripsi (AndroidKeyStore)
- 🤖 **Mesin agen** pikir–aksi–hasil (maks 6 langkah) dengan 18 perkakas nyata:
  diagnostik, buka aplikasi, volume, senter, URL/browser & alamat web, pencarian
  web, share, pengaturan, telepon/SMS draf, **P48: kirim WhatsApp (nomor + isi
  pesan terisi, 0→62 otomatis)**, **P48: simpan kontak (nama+nomor terisi)**,
  kalkulator aman, ingatan, dan lainnya
- 🎛️ **Pusat Kendali 1 halaman jelas (P45)** — 5 seksi berbahasa manusia:
  OTAK (kunci API + model ketuk-pilih) · SUARA · AGEN (izin 3 tingkat,
  perkakas, log) · GAYA (panggilan, ingatan, protokol) · MARKAS
  (BATERAI · SIMPAN · NET nyata) — setiap baris ada keterangan fungsinya;
  tanpa tab samar, tanpa stub "segera"
- 🌌 **Tampilan kelas Jarvis** — nebula animasi, bintang paralaks, piringan
  reaktor, orb MK-XIX + aura (P44: cincin teks telemetri di sekeliling orb
  dihapus — panggung murni bentuk), dock kaca beriak, tema Mark
- 🚀 **Ikon launcher monogram A (P47)** — huruf A emas berlapis + gelombang
  suara sian di angkasa navy (bukan reaktor lagi), lengkap adaptive icon +
  monochrome Android 13+ + PNG legacy semua densitas (48–192px)

## Riwayat singkat

K19 → K22: HUD Jarvis, Pusat Kendali dirambah, Ingatan AVI, dan AVI jadi
AI AGEN (function calling terbukti di 2 model free-tier). P48: cukup tempel
API key (otak terpasang otomatis), multi API key dengan failover kuota,
dan perkakas WhatsApp + simpan kontak. Detail lengkap ada di pesan commit.
