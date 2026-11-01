# AVI — Asisten Pribadi (MK1)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK1 v18 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v18 — JARVIS SIANG & MALAM: DUA-DUANYA HUD

Masukan Pak: **ini asisten tema JARVIS — bukan tema polos "kek kebun" tanpa
animasi**. Betul, maka v18:

1. **Tema siang diulang** — bukan putih polos, melainkan HUD holografik
   terang: latar biru-langit teknologi, grid hologram yang terlihat,
   aurora bercahaya, partikel, dan berkas pindai yang menyapu ruangan.
   Siang tetap JARVIS.
2. **Animasi di semua lapis** — berkas pindai menyapu tiap ±8 detik,
   garis energi di bawah header bernafas, nama AVI berkilau, nav aktif
   berdenyut, komposer menyala saat fokus, dan orb kini punya cincin
   derajat + sapuan radar (makin cepat saat kerja, emas saat tuntas).
3. Slot teks **satu** di samping orb, jawaban AVI **di atas orb**
   (obrolan, dengan indikator mengetik), asisten dulu — semua arahan
   sebelumnya dipertahankan.

Arahan lama tetap: **asisten dulu, agent sekadar cadangan**; tema AUTO
ikut waktu dengan crossfade halus; riwayat chat tersembunyi di atas (v10);
tanpa lampiran; tanpa audit billing.

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

Tahap desain: mockup v18 (siang & malam dua-duanya HUD Jarvis; animasi
di semua lapis; satu slot teks di samping orb; jawaban AVI di obrolan).
Implementasi desain terbaru ke aplikasi Android menyusul setelah desain
disetujui.
