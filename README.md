# AVI — Asisten Pribadi (MK1)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK1 v16 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v16 — ASISTEN DULU, AGENT SEKADAR CADANGAN

Arahan Pak: **fokus utama adalah ASISTEN** — agent hanya terkadang, cuma
cadangan. Maka mockup **terbuka dalam wujud asisten yang tenang**: sapaan,
obrolan sehari-hari, dan orb yang bernafas — tanpa tugas berjalan, tanpa
persen, tanpa strip. Agent baru bangun saat Pak memberi tugas berat
(coba ketik "buatkan laporan pengeluaran") — kartu kerjanya mengalir di
dalam obrolan ("AGENT AKTIF · DI DALAM AVI"), dan setelah tuntas AVI
kembali penuh sebagai asisten.

Detail tata: **orb melayang BERSIH di atas papan ketik — tanpa banner,
tanpa apa pun di belakangnya**. Perintah Pak (teks / hasil voice) tampil
**DI SAMPING orb** dengan tanda TERKIRIM ✓; **jawaban AVI muncul DI ATAS
orb** — di dalam obrolan, sebagai gelembung beravatar. Riwayat chat
tetap tersembunyi di atas (v10).

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: obrolan asisten (balasan AVI muncul di
obrolan), pemicu tugas agent lewat pesan (mis. "buatkan laporan"), alur
laporan lengkap (jeda/stop, izin di 60%, hasil PDF), panel riwayat
tersembunyi, orb melayang di atas papan ketik (ketuk orb = mode suara;
perintah voice tampil di samping orb), tema siang/malam. Papan ketik
nyata di HP ditangani rapi (`interactive-widget=resizes-content` +
`visualViewport`). Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v16 (asisten dulu — agent sekadar cadangan; perintah
di samping orb, jawaban AVI di obrolan; orb bersih tanpa banner; riwayat
chat tersembunyi di atas). Implementasi desain terbaru ke aplikasi Android
menyusul setelah desain disetujui.
