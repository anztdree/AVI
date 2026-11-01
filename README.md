# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK-II v25 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v25 — RESTORE PROTOTIPE AWAL

Masukan Pak: **"masih lebih bagus prototipe awal !! tema gelap UI icon
menarik.. makin kesini garapan anda makin ngacoo"**. Maka v25 BUKAN
eksperimen baru — seluruh bahasa visual **dikembalikan ke prototipe
awal** (v9→v15) yang disukai Pak, dan semua eksperimen v18–v24
(panel flat presisi, reticle, helix, plexus) dibuang:

1. **Tema gelap prototipe awal** — latar `#070B12`, ruangan 4 lapis
   (aurora + grid + partikel + vignette), kartu kaca + sudut
   holografik sian/emas, ikon menarik (▤ ◔ ☀ ✉ ✦), tanda vital
   **JANTUNG MK1** di KENDALI.
2. **Orb reaktor 112px** melayang bersih di atas papan ketik — TANPA
   banner/kabut apa pun di belakangnya (arahan v16), cincin ganda
   sian/emas berputar, napas inti, ring progres saat kerja, emas saat
   tuntas, mengecil halus saat papan ketik nyata terbuka.
3. **Satu slot teks** di samping orb (arahan v17) — telemetri MK1 /
   TERKIRIM ✓ / status kerja bergiliran, tak pernah dua teks bersamaan.
4. **Jawaban AVI di atas orb** (arahan v16/v17) — gelembung beravatar
   di dalam obrolan, didahului indikator mengetik tiga titik; perintah
   Pak tetap DI SAMPING orb.
5. **Tanpa riwayat total** (arahan v23) — nol kata riwayat di UI;
   terbuka bersih: brief HARI INI + sapaan hidup saja.
6. **ASISTEN DULU** (arahan v16) — terbuka tenang; agent di dalam AVI
   baru bangun saat tugas berat (laporan/rekap/draf email/dll atau MULAI
   CEPAT), strip JEDA/STOP, gate izin 60%, kartu hasil PDF, lalu kembali
   jadi asisten.
7. Tetap: tanpa lampiran, tanpa billing audit, tanpa jam, COMING SOON
   untuk yang belum ada, tema siang/malam, mode suara COMING SOON.

Teruji agent-browser desktop 1440×900 & mobile 390×844: boot bersih,
kirim santai → TERKIRIM samping orb → balasan di atas orb (satu slot),
tugas berat → agent penuh (langkah → gate 60% → izinkan → tuntas emas →
kartu PDF → settle), MULAI CEPAT, JEDA/STOP, sheet TUGAS, KENDALI,
tema siang/malam, mode suara buka-tutup → slot; nol error konsol.

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: obrolan asisten bersih (log transmisi +
efek mengetik), pemicu tugas agent lewat pesan (mis. "buatkan laporan"),
alur laporan lengkap (jeda/stop, izin di 60%, hasil PDF), orb inti cair
dengan koreografi 6 keadaan (ketuk orb = mode suara), denyut ACK +
parallax kursor, tema AUTO/malam/siang dengan crossfade. Papan ketik
nyata di HP ditangani rapi (`interactive-widget=resizes-content` +
`visualViewport`). Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v23 (NADI — tanpa riwayat total, ruangan bernadi;
inti cair + giroskop v22 dan layout kokpit HUD v20 dipertahankan).
Implementasi desain terbaru ke aplikasi Android menyusul setelah desain
disetujui.
