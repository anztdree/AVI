# AVI — Asisten Virtual Android

AVI adalah **asisten virtual berbasis AI** untuk Android: bicara bebas (STT & TTS
ramah bahasa Indonesia), otak agen cloud (**Google Gemini + NVIDIA NIM** — dua
tipe AI saja) dengan **perkakas perangkat nyata** dan Pusat Kendali panel samping.

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
- 🧠 **Dua tipe AI SAJA (P49)** — GEMINI (aistudio, gratis) & NVIDIA NIM
  (build.nvidia.com, kunci `nvapi-…`): keduanya punya MUAT DAFTAR MODEL sendiri,
  uji per model, dan pilihan model tersimpan per-tipe — ganti bolak-balik
  Gemini ↔ NIM atau antar model TIDAK PERNAH menghapus riwayat obrolan
- 🧠 **AI cloud Google Gemini** — auth `X-goog-api-key` + `?key=` (kebal VPN/proxy
  yang menghapus header), daftar model dinamis, fetch mencoba SEMUA kunci berurutan
- 🔑 **P48: CUKUP TEMPEL API KEY — OTAK TERPASANG OTOMATIS** — kunci ditempel,
  AVI sendiri memuat daftar model, menguji kandidat teratas, dan memilih yang
  benar-benar menjawab. Tidak ada lagi "model belum dipilih" yang membuat
  permintaan mati diam setelah input kunci. P49: jalur yang sama untuk NVIDIA NIM
    (daftar cadangan terkurasi siap dipakai bila NVIDIA tak terjangkau)
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
- 🎛️ **P49: PUSAT KENDALI = PANEL SAMPING SHOW/HIDE** — ketuk MENU di header,
  panel meluncur dari kiri berisi 5 menu TERPISAH (OTAK · SUARA · AGEN · GAYA ·
  MARKAS, tiap menu dengan statusnya sendiri); ketuk satu menu → isi HANYA menu
  itu — tidak ada lagi semua seksi bertumpuk dalam satu tempat
- 🌌 **Tampilan kelas Jarvis** — nebula animasi, bintang paralaks, piringan
  reaktor, orb MK-XIX + aura (P44: cincin teks telemetri di sekeliling orb
  dihapus — panggung murni bentuk), dock kaca beriak, tema Mark
- 🚀 **Ikon launcher reaktor mewah (P49)** — reaktor emas berukir halus +
  inti plasma sian bercahaya di angkasa navy, render 3D premium; adaptive icon
  (safe-zone aman dari masker bulat/squircle) + monochrome Android 13+ +
  PNG legacy semua densitas (48–192px)

## Riwayat singkat

K19 → K22: HUD Jarvis, Pusat Kendali dirambah, Ingatan AVI, dan AVI jadi
AI AGEN (function calling terbukti di 2 model free-tier). P48: cukup tempel
API key (otak terpasang otomatis), multi API key dengan failover kuota,
dan perkakas WhatsApp + simpan kontak. P49: dua tipe AI saja (GEMINI +
NVIDIA NIM) dengan fetch model anti-gagal, Pusat Kendali jadi panel samping
show/hide dengan menu terpisah, dan ikon launcher reaktor mewah.
Detail lengkap ada di pesan commit.
