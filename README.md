# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK-II v23 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v23 — NADI (denyut ruangan)

Masukan Pak: **"masih monoton.... masih ada riyawat"**. Diagnosis v22:
(1) sisa riwayat masih terlihat — ikon ⧗, seksi RIWAYAT di TUGAS,
"Simpan riwayat", bahkan obrolan contoh yang sudah terisi saat dibuka;
(2) yang hidup cuma orb — panel, obrolan, komposer mati semua.

1. **Tanpa riwayat TOTAL** — semua jejak riwayat dihapus: ikon ⧗ di
   header, panel melorot, seksi RIWAYAT di sheet TUGAS, toggle
   "Simpan riwayat", "Percakapan Tersimpan", baris log, dan obrolan
   contoh. Aplikasi dibuka BERSIH: hanya brief pagi + sapaan hidup.
   Hasil kerja hidup di obrolan, titik.
2. **Ruangan bernadi** — seluruh ruang merespons keadaan AVI sebagai
   satu nadi: MIKIR (sapuan melambat, plexus cepat), BICARA (aurora
   lebih jenuh), KERJA (sapuan 2x cepat, lantai grid mengalir deras,
   plexus menyala), TUNTAS (siram emas sekali lalu tenang).
3. **Denyut di semua lapis** — scanline HUD di seluruh kaca, sapuan
   cahaya saat pindah tab, kilat node di rel saat pesan tiba,
   gelembung menyala saat AVI mengetik, tombol kirim menyala emas saat
   Pak menulis, nav aktif berdenyut, sempalan HELIX ganda (emas+sian)
   berputar berlawanan di dalam inti.
4. **Identitas v22 dipertahankan** — inti cair energi ganda (sian =
   pikiran, emas = tindakan) + giroskop dua orbit bersatelit.

Arahan lama tetap: **asisten dulu, agent sekadar cadangan**; inti AVI di
atas papan ketik; perintah Pak satu slot "TERKIRIM ✓" di samping orb;
jawaban AVI di obrolan (di atas orb) dengan efek mengetik; pesan sebagai
log transmisi + panel sudut potong (v20); SYSRAIL telemetri hidup;
tema AUTO ikut waktu (malam = HUD gelap, siang = HUD terang — dua-duanya
Jarvis); tanpa jam, tanpa chip saran, tanpa lampiran; tanpa audit billing.

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
