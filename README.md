# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK-II v24 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v24 — PRECISION HUD (rombak total, referensi film asli)

Masukan Pak: **"anda gak ada referensi tema UI lain ?? saya dari minta
rombak tapi perubahan anda cuma kecil kecil"**. Maka v24 TIDAK menambal
v23 — bahasa visualnya diganti total, dan kali ini dibangun di atas
**referensi eksternal asli**: arsip [HUDS+GUIS](https://www.hudsandguis.com)
— layar HUD Territory Studio untuk Avengers / Age of Ultron + HUD helm
Iron Man 3 — diunduh dan dianalisis sebelum satu baris pun ditulis.

Yang mati dari v23: kartu kaca membulat + blur, inti cair heliks,
plexus, lantai Tron, aurora blob, sudut potong. Yang hidup (bahasa film):

1. **Panel presisi flat** — garis 1px, nol blur, nol shadow; tick sudut
   TL/BR sebagai tanda tangan instrumen.
2. **Orb = reticle derajat** (HUD helm MK-III) — cincin tick 72 titik,
   label derajat 000/090/180/270, busur sian+emas berlawanan arah, tick
   silang, inti putih panas; waveform radial 28 batang saat AVI bicara;
   ring kemajuan segmen saat kerja; emas mengambil alih saat tuntas.
3. **Obrolan = log transmisi** — rel node + header mono
   "AVI · MK-II // ASISTEN"; pesan MATERIALIZING (sapuan clip-path).
4. **Latar = ruang ukur** — kanvas titik-matriks berdenyut, dua garis
   silang horizontal, satu sapuan pindai, RAIL PENGGARIS skala vertikal
   bernomor + kursor berlian kedua sisi, gugus data sudut hidup
   (ID/GRID/jam/MEM/NET/PUSTAKA + bar EQ).
5. **Tipografi Michroma** (Eurostile — huruf khas UI film) untuk judul.
6. **Komposer presisi** — prompt ">" berkedip; tombol kirim kotak rata
   yang menyala emas saat Pak menulis.
7. Strip kendali & semua bar kemajuan jadi **segmen tick**.

Arahan lama tetap: TANPA riwayat total; inti di atas papan ketik; satu
slot perintah di samping orb; jawaban AVI di obrolan (di atas orb);
**asisten dulu, agent cadangan** — agent hanya bangun untuk tugas berat,
minta izin di 60%, tuntas = kartu PDF di obrolan.

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
