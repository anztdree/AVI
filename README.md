# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK-II v21 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v21 — ARC REACTOR (orb dirombak total)

Masukan Pak: **"masih terlalu jelek — orb, animasi, cara kerja orb dan
lainnya masih gak cocok"**. Diagnosis v19/v20: terlalu ramai — cincin elips
miring, banyak putaran dengan kecepatan acak, bola gradient glossy, batang
energi mini. Itu bukan bahasa Jarvis. v21 memakai satu prinsip:
**PRESISI & TENANG**.

1. **Orb = reaktor busur murni SVG** — geometri presisi satu bahasa:
   inti putih menyala (white-hot) dengan monogram AVI, cincin koil 12
   segmen, dua arc sian berlawanan arah, arc derajat emas, tick ring
   halus, sapuan radar tipis di sangkar luar. Semua rotasi LAMBAT &
   SELARAS — hipnotis, bukan chaos. Elips miring, konsol batang, dan
   halo blob dihapus total.
2. **Cara kerja orb — koreografi per keadaan**: SIAGA (napas lambat) →
   MIKIR (arc mendesak saat perintah dikirim) → BICARA (inti berkedip
   seperti suara selama jawaban diketik) → DENGAR (riak keluar saat mode
   suara) → KERJA (reaktor push + ring kemajuan + % di inti) → TUNTAS
   (emas mengambil alih + ledakan cahaya sekali).
3. **Orb merespons Pak** — denyut ACK emas saat perintah dikirim, dan
   reaktor MENOLEH halus ke arah kursor (parallax 3D, desktop).
4. **Warna gradasi mengikuti tema** — stop SVG memakai variabel tema,
   transisi mulus saat ganti AUTO/malam/siang; inti siang dibuat lebih
   terang agar monogram tetap kontras.
5. **Perbaikan bug** — tag slot orb tidak lagi mewarisi gaya sakelar
   KENDALI (bentuk pil yang menimpa teks).

Arahan lama tetap: **asisten dulu, agent sekadar cadangan**; reaktor di
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
tersembunyi, orb reaktor dengan koreografi 6 keadaan (ketuk orb = mode
suara), denyut ACK + parallax kursor, tema AUTO/malam/siang dengan
crossfade. Papan ketik nyata di HP ditangani rapi
(`interactive-widget=resizes-content` + `visualViewport`).
Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v21 (arc reactor SVG + koreografi keadaan; layout
kokpit HUD v20 dipertahankan). Implementasi desain terbaru ke aplikasi
Android menyusul setelah desain disetujui.
