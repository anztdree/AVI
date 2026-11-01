# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK-II v22 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v22 — HELIX CORE (identitas sendiri, bukan salinan Jarvis)

Masukan Pak: **"orb emang mirip jarvis!! tapi kita jadi gak punya identitas
sendiri dan keliatan banget orb jadul!!!! tanpa riwayat!! bikin tema yg
bagus, bukan monoton dari tadi kek gini"**. Diagnosis v21: meniru reaktor
busur Stark terlalu harfiah — koil 12 segmen, tick radar, inti putih — itu
bahasa 2008. v22 membangun **IDENTITAS SENDIRI: ENERGI GANDA AVI**.

1. **Inti cair** — bola energi dengan pusaran conic dua warna yang berputar
   pelan: SIAN = pikiran, EMAS = tindakan. Bahasa modern (energi cair yang
   mengalir), bukan koil logam 2008.
2. **Giroskop AVI** — dua cincin orbit miring + satelit energi (satu emas,
   satu sian) mengelilingi inti — tanda tangan visual AVI sendiri, tidak
   dimiliki reaktor Stark maupun asisten lain.
3. **Busur presisi dipangkas** — satu busur sian + satu emas + ring
   kemajuan. Koil, tick radar, sapuan radar: dihapus.
4. **Koreografi 6 keadaan tetap** — SIAGA (napas) → MIKIR (pusaran & orbit
   mendesak) → BICARA (inti berkedip seperti suara) → DENGAR (riak keluar)
   → KERJA (inti mendorong + ring kemajuan + %) → TUNTAS (emas mengambil
   alih + ledakan sekali) — plus denyut ACK & parallax menoleh ke kursor.
5. **Tema makin hidup (anti monoton)** — glob aurora melayang di latar,
   transisi antar tab bernyawa, mini-orb header & mode suara ikut bahasa
   inti cair.
6. **Obrolan bersih tanpa pita riwayat** — riwayat pindah ke ikon ⧗ di
   header; layar utama fokus ke inti + obrolan berjalan.

Arahan lama tetap: **asisten dulu, agent sekadar cadangan**; inti AVI di
atas papan ketik; perintah Pak satu slot "TERKIRIM ✓" di samping orb;
jawaban AVI di obrolan (di atas orb) dengan efek mengetik; pesan sebagai
log transmisi + panel sudut potong (v20); SYSRAIL telemetri hidup;
tema AUTO ikut waktu (malam = HUD gelap, siang = HUD terang — dua-duanya
Jarvis); riwayat chat tersembunyi di atas (v10); tanpa jam, tanpa chip
saran, tanpa lampiran; tanpa audit billing.

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: obrolan asisten (log transmisi + efek
mengetik), pemicu tugas agent lewat pesan (mis. "buatkan laporan"), alur
laporan lengkap (jeda/stop, izin di 60%, hasil PDF), panel riwayat
tersembunyi (ikon ⧗ di header), orb inti cair dengan koreografi 6 keadaan
(ketuk orb = mode suara), denyut ACK + parallax kursor, tema AUTO/malam/
siang dengan crossfade. Papan ketik nyata di HP ditangani rapi
(`interactive-widget=resizes-content` + `visualViewport`).
Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v22 (inti cair energi ganda + giroskop; layout
kokpit HUD v20 dipertahankan). Implementasi desain terbaru ke aplikasi
Android menyusul setelah desain disetujui.
