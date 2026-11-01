# AVI — Asisten Pribadi (MK1)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK1 v11 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v11 — orb + obrolan saat ini + orb di atas papan ketik

Prinsip: yang tampil hanya **orb AVI + obrolan saat ini** — riwayat chat
**tersembunyi di atas** (afordansi tipis di puncak → panel melorot saat
dipanggil). Saat Pak mengetik, **orb menemani di atas papan ketik** —
kehadiran AVI tidak pernah hilang, labelnya ikut hidup
("KERJA · 37% · TETAP MENEMANI"). Kemampuan agent tidak pernah jadi mode
terpisah: aliran kerja agent mengalir **dari dalam AVI**
("AGENT AKTIF · DI DALAM AVI") — lengkap dengan langkah, log live, dan
pintu izin emas "AVI minta izin, Pak".

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: alur laporan pengeluaran (jeda/stop, izin di
60%, hasil PDF), panel riwayat tersembunyi, orb menemani saat mengetik
(ketuk kolom tulis), mode suara (ketuk orb), tema siang/malam. Papan ketik
nyata di HP ditangani rapi (`interactive-widget=resizes-content` +
`visualViewport`) sehingga komposer dan orb selalu berada di atas keyboard.
Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v11 (orb + obrolan saat ini; riwayat chat tersembunyi
di atas; orb menemani di atas papan ketik). Implementasi desain terbaru ke
aplikasi Android menyusul setelah desain disetujui.
