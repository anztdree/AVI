# AVI — Asisten Pribadi (MK1)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK1 v13 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v13 — ORB BESAR DI ATAS PAPAN KETIK

Prinsip: yang tampil hanya **orb AVI + obrolan saat ini** — riwayat chat
**tersembunyi di atas** (afordansi tipis di puncak → panel melorot saat
dipanggil). **Orb kehadiran AVI yang BESAR (112px) melekat permanen tepat
di atas papan ketik** — lengkap dengan sapaan, telemetri MK1, dan status
kerja di sisinya: saat tugas berjalan orb berdenyut dengan cincin progres
("SEDANG BEKERJA · 45%"), saat tuntas berubah emas dengan tanda ✓.
Kemampuan agent tidak pernah jadi mode terpisah: kartu kerja agent
mengalir **di dalam obrolan**, tepat di bawah pesan AVI
("AGENT AKTIF · DI DALAM AVI") — lengkap dengan langkah, log live, dan
pintu izin emas "AVI minta izin, Pak".

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: alur laporan pengeluaran (jeda/stop, izin di
60%, hasil PDF), panel riwayat tersembunyi, orb besar di atas papan ketik
(ketuk orb = mode suara), tema siang/malam. Papan ketik nyata di HP
ditangani rapi (`interactive-widget=resizes-content` + `visualViewport`)
sehingga orb dan komposer selalu berada di atas keyboard. Fitur bertanda
COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v13 (orb besar menemani di atas papan ketik; obrolan
saat ini; riwayat chat tersembunyi di atas; kartu kerja agent di dalam
obrolan). Implementasi desain terbaru ke aplikasi Android menyusul setelah
desain disetujui.
