# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK-II v20 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v20 — COCKPIT HUD (layout dirombak)

Masukan Pak: **"ini bukan tema jarvis dan teknologi — layout juga gak berubah
signifikan"**. Maka v20 mengubah LAYOUT, bukan sekadar warna:

1. **Pesan = log transmisi** — tiap pesan punya rel node (belah ketupat +
   garis penghubung) di sisi kiri, tag `AVI · ASISTEN` / `PAK` huruf mono,
   dan panel **sudut potong** (clip-path) — bukan gelembung chat bulat.
   Pesan PAK rata kanan beraksen emas.
2. **SYSRAIL** — strip telemetri di bawah header yang selalu hidup:
   REAKTOR · TERMAL · MEM · NET, angkanya berubah pelan sendiri.
3. **Rail telemetri di kedua sisi layar** (garis ukur bertitik) + kurung
   sudut di empat pojok bingkai — layar terasa kokpit, bukan aplikasi.
4. **Semua panel sudut potong** — kartu, tombol, komposer (input cut-corner,
   tombol kirim heksagonal, mic oktagonal), toast, stat TUGAS. Kurung sudut
   cyan/emas pindah ke pojok sisi panel.
5. **Konsol energi mini di kiri orb** — batang sian/emas bernafas,
   melengkapi slot teks di kanan orb.

Arahan lama tetap: **asisten dulu, agent sekadar cadangan**; orb reaktor
(v19: cincin orbit 3D, radar sweep, ignition, koreografi per keadaan) di
atas papan ketik; perintah Pak satu slot "TERKIRIM ✓" di samping orb;
jawaban AVI di obrolan (di atas orb) dengan efek mengetik; tema AUTO
ikut waktu (malam = HUD gelap, siang = HUD terang — dua-duanya Jarvis);
riwayat chat tersembunyi di atas (v10); tanpa jam, tanpa chip saran,
tanpa lampiran; tanpa audit billing.

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: obrolan asisten (log transmisi + efek
mengetik), pemicu tugas agent lewat pesan (mis. "buatkan laporan"), alur
laporan lengkap (jeda/stop, izin di 60%, hasil PDF), panel riwayat
tersembunyi, orb reaktor di atas papan ketik (ketuk orb = mode suara),
tema AUTO/malam/siang dengan crossfade. Papan ketik nyata di HP ditangani
rapi (`interactive-widget=resizes-content` + `visualViewport`).
Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v20 (kokpit HUD: log transmisi, sysrail hidup, panel
sudut potong, rail telemetri, orb reaktor MK-II). Implementasi desain
terbaru ke aplikasi Android menyusul setelah desain disetujui.

