# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `android-app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | **SATU-SATUNYA** mockup tampilan — self-contained, jadi contoh display sebelum dikemas jadi APK |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `android-app/**` berubah) |

## Preview v42 — "MK-XX ATELIER" : TEMA & TATA LETAK OBROLAN DIRANCANG ULANG

Masukan Pak: **"tema dan tata letak maupun UI dari tampilan chat ini
masih tidak sinkron tidak mewah"** — dan urusan orb dianggap selesai.
Maka v42:

1. **Orb MK-XIX v41 TETAP UTUH** — cangkang HUD SENTRI + inti plasma
   konsep 2 tidak disentuh sama sekali (CSS, HTML, state kerja/tuntas,
   tema siang — semua disalin apa adanya).
2. **Obrolan dibersihkan total** — scanline, bracket HUD, rel mesin,
   strip VOX, comms bar sesak DIHAPUS. Bubble kini kaca rapi dengan
   SATU aksen tepi tipis (sian untuk AVI, emas untuk Pak), meta
   ringkas (nama + jam), masuk pegas halus.
3. **Latar ATELIER** — grid blueprint samar bermask radial + dua
   pendaran tenang (sian atas, emas bawah) + vignette. Menggantikan
   3 blob aurora + starfield.
4. **Aksen disiplin & sinkron dengan orb** — sian = sistem/AVI,
   emas = Pak/aksi (kirim menyala emas saat ada tulisan), mint =
   tuntas.
5. **Sapaan cepat** — 4 chip elegan di bawah sapaan (rekap rapat,
   draf email, laporan pengeluaran, pengingat) langsung menjalankan
   alur agent. Riwayat obrolan diperpanjang (10) agar percakapan
   terasa hidup. Status sambungan jadi SATU baris tenang.
6. `prefers-reduced-motion` dihormati; tema siang "fajar atelier"
   & malam tetap ada.

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install
apa pun. Interaksi yang jalan di mockup: obrolan asisten, chip sapaan
cepat, pemicu tugas agent lewat pesan (mis. "buatkan laporan"), alur
laporan lengkap (JEDA/STOP, izin di 60%, hasil PDF), orb v41 dengan
koreografi keadaan (ketuk orb = mode suara), sheet TUGAS, KENDALI,
tema malam/siang. Papan ketik nyata di HP ditangani rapi
(`interactive-widget=resizes-content` + `visualViewport`).
Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v42 (MK-XX ATELIER — tema obrolan baru, orb v41
dikunci). Implementasi desain terbaru ke aplikasi Android menyusul
setelah desain disetujui.
