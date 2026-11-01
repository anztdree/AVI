# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK-II v19 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v19 — REACTOR REBUILD (Orb Teknologi + Logika Animasi)

Masukan Pak: **"tema jarvis teknologi orb dan beberapa logika animasi yg
indah"**. Maka v19 dibangun ulang dari nol:

1. **Orb teknologi (reaktor)** — inti plasma dengan monogram AVI, tick-ring,
   arc derajat emas, dua arc sian berlawanan arah, sapuan radar, dan **dua
   cincin orbit 3D miring** dengan titik emas & sian yang mengelilingi orb.
   Orb menyala dengan urutan **ignition** saat dibuka, dan koreografinya
   berubah per keadaan: siaga (tenang), mendengar (riak), kerja (cepat +
   ring persen), tuntas (ledakan emas).
2. **Logika animasi yang hidup** — latar **plexus** (partikel terhubung
   garis, canvas, bukan gradient polos), lantai grid hologram mengalir,
   sapuan pindai, telemetri sudut vertikal, dan **telemetri KENDALI yang
   bernafas** (reaktor/termal/mem/latensi berubah pelan sendiri).
3. **Jawaban AVI muncul dengan efek mengetik** (caret berkedip) di obrolan —
   di atas orb; perintah Pak tampil sebagai **satu slot** "TERKIRIM ✓" di
   samping orb, lalu kembali ke telemetri siaga.

Arahan lama tetap: **asisten dulu, agent sekadar cadangan**; tema AUTO
ikut waktu dengan crossfade halus (siang = HUD terang, malam = HUD gelap —
dua-duanya Jarvis); riwayat chat tersembunyi di atas (v10); tanpa jam,
tanpa chip saran, tanpa lampiran; tanpa audit billing.

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: obrolan asisten (mengetik → balasan dengan
efek mengetik), pemicu tugas agent lewat pesan (mis. "buatkan laporan"),
alur laporan lengkap (jeda/stop, izin di 60%, hasil PDF), panel riwayat
tersembunyi, orb reaktor di atas papan ketik (ketuk orb = mode suara;
perintah voice tampil di samping orb), tema AUTO/malam/siang dengan
crossfade. Papan ketik nyata di HP ditangani rapi
(`interactive-widget=resizes-content` + `visualViewport`).
Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v19 (reaktor MK-II: orb teknologi + plexus hidup +
logika animasi per keadaan). Implementasi desain terbaru ke aplikasi
Android menyusul setelah desain disetujui.
