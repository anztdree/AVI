# AVI — Asisten Pribadi (MK1)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK1 v14 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v14 — ORB MELAYANG DI ATAS PAPAN KETIK, TANPA BANNER

Prinsip: yang tampil hanya **orb AVI + obrolan saat ini** — riwayat chat
**tersembunyi di atas** (afordansi tipis di puncak → panel melorot saat
dipanggil). **Orb kehadiran AVI yang BESAR (112px) melayang sendiri tepat
di atas papan ketik** — tanpa banner, tanpa garis, tanpa kotak; hanya
diiringi satu bisikan telemetri MK1 kecil di bawahnya. Sapaan berada di
dalam obrolan; status kerja hidup di dalam orb sendiri (cincin progres
+ persentase; emas + ✓ saat tuntas). Teks yang dikirim muncul sebagai
glembung **di dalam obrolan** — bukan di sebelah orb.
Kemampuan agent tidak pernah jadi mode terpisah: kartu kerja agent
mengalir **di dalam obrolan**, tepat di bawah pesan AVI
("AGENT AKTIF · DI DALAM AVI") — lengkap dengan langkah, log live, dan
pintu izin emas "AVI minta izin, Pak".

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: alur laporan pengeluaran (jeda/stop, izin di
60%, hasil PDF), panel riwayat tersembunyi, orb melayang di atas papan ketik
(ketuk orb = mode suara), kirim teks → gelembung masuk obrolan, tema
siang/malam. Papan ketik nyata di HP ditangani rapi
(`interactive-widget=resizes-content` + `visualViewport`) sehingga orb dan
komposer selalu berada di atas keyboard — orb mengecil halus saat keyboard
terbuka. Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v14 (orb melayang tanpa banner di atas papan ketik;
sapaan & teks terkirim di dalam obrolan; riwayat chat tersembunyi di atas;
kartu kerja agent di dalam obrolan). Implementasi desain terbaru ke
aplikasi Android menyusul setelah desain disetujui.
