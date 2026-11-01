# AVI — Asisten Pribadi (MK1)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK1 v15 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v15 — TEKS TERKIRIM DI SEBELAH ORB

Prinsip: yang tampil hanya **orb AVI + obrolan saat ini** — riwayat chat
**tersembunyi di atas** (afordansi tipis di puncak → panel melorot saat
dipanggil). **Orb kehadiran AVI yang BESAR (112px) melayang tanpa banner
tepat di atas papan ketik**, dan **teks yang dikirim / hasil voice tampil
DI SEBELAH orb** — lengkap dengan tanda TERKIRIM ✓ dan balasan singkat
AVI — bukan di atas orb. Sapaan berada di dalam obrolan; status kerja
hidup di dalam orb sendiri (cincin progres + persentase; emas + ✓ saat
tuntas).
Kemampuan agent tidak pernah jadi mode terpisah: kartu kerja agent
mengalir **di dalam obrolan**, tepat di bawah pesan AVI
("AGENT AKTIF · DI DALAM AVI") — lengkap dengan langkah, log live, dan
pintu izin emas "AVI minta izin, Pak".

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: alur laporan pengeluaran (jeda/stop, izin di
60%, hasil PDF), panel riwayat tersembunyi, orb melayang di atas papan ketik
(ketuk orb = mode suara), kirim teks / hasil voice → tampil **di sebelah orb**
dengan tanda TERKIRIM ✓, tema siang/malam. Papan ketik nyata di HP ditangani
rapi (`interactive-widget=resizes-content` + `visualViewport`) sehingga orb
dan komposer selalu berada di atas keyboard — orb mengecil halus saat
keyboard terbuka. Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v15 (teks terkirim & hasil voice di sebelah orb; orb
melayang tanpa banner di atas papan ketik; riwayat chat tersembunyi di atas;
kartu kerja agent di dalam obrolan). Implementasi desain terbaru ke
aplikasi Android menyusul setelah desain disetujui.
