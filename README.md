# AVI — Asisten Pribadi (MK1)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK1 v17 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v17 — LEMBUT, HIDUP, SATU SUARA

Koreksi atas masukan Pak (logika tema kaku, orb jelek, dua teks output tidak
estetik):

1. **Tema tidak kaku** — ada mode **AUTO** yang mengikuti waktu
   (18.00–06.00 malam, sisanya siang); tombol menyeberang
   AUTO→MALAM→SIANG, dan setiap perpindahan warna dianimasikan
   crossfade halus — tidak ada tema yang "menukap" mendadak.
2. **Orb diulang total** — wajah AVI berlapis: inti bercahaya dengan
   plasma yang berputar & kilau kaca, dua cincin halus berlawanan arah,
   satu titik emas yang mengorbit, cahaya yang menempel erat ke orb
   (bukan banner), dan badan yang melayang lembut. Saat kerja makin
   cepat & terang; saat tuntas memerah emas lalu kembali tenang.
3. **Satu slot teks di samping orb** — telemetri MK1, TERKIRIM ✓, dan
   status kerja bergiliran di SLOT YANG SAMA; tidak pernah ada dua teks
   bersamaan. Jawaban AVI tetap muncul DI ATAS orb — di dalam obrolan,
   kini didahului indikator mengetik tiga titik.

Arahan lama dipertahankan: **asisten dulu, agent sekadar cadangan** —
mockup terbuka dalam wujud asisten tenang; agent baru bangun saat Pak
memberi tugas berat (coba ketik "buatkan laporan pengeluaran") dan
kembali jadi asisten setelah tuntas. Riwayat chat tetap tersembunyi di
atas (v10); tanpa lampiran; tanpa audit billing.

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: obrolan asisten (mengetik → balasan),
pemicu tugas agent lewat pesan (mis. "buatkan laporan"), alur laporan
lengkap (jeda/stop, izin di 60%, hasil PDF), panel riwayat tersembunyi,
orb melayang di atas papan ketik (ketuk orb = mode suara; perintah voice
tampil di samping orb), tema AUTO/malam/siang dengan crossfade. Papan
ketik nyata di HP ditangani rapi (`interactive-widget=resizes-content` +
`visualViewport`). Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v17 (tema auto ikut waktu + crossfade; orb hidup
bertingkat; satu slot teks di samping orb; jawaban AVI di obrolan).
Implementasi desain terbaru ke aplikasi Android menyusul setelah desain
disetujui.
