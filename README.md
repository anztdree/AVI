# AVI — Asisten Pribadi (MK1)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK1 v12 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v12 — orb + obrolan saat ini + orb SELALU di atas papan ketik

Prinsip: yang tampil hanya **orb AVI + obrolan saat ini** — riwayat chat
**tersembunyi di atas** (afordansi tipis di puncak → panel melorot saat
dipanggil). **Orb AVI SELALU tampil di atas papan ketik** — bar orb kecil
bernyawa menempel permanen di atas komposer, tidak pernah tersembunyi,
labelnya ikut kondisi ("KERJA · 37% · TETAP MENEMANI" / "AVI SIAGA" /
"TUNTAS · SAYA MENEMANI, PAK"). Kemampuan agent tidak pernah jadi mode
terpisah: aliran kerja agent mengalir **dari dalam AVI**
("AGENT AKTIF · DI DALAM AVI") — lengkap dengan langkah, log live, dan
pintu izin emas "AVI minta izin, Pak".

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install apa pun.
Interaksi yang jalan di mockup: alur laporan pengeluaran (jeda/stop, izin di
60%, hasil PDF), panel riwayat tersembunyi, orb permanen di atas papan
ketik, mode suara (ketuk orb), tema siang/malam. Papan ketik nyata di HP
ditangani rapi (`interactive-widget=resizes-content` + `visualViewport`)
sehingga komposer dan orb selalu berada di atas keyboard. Fitur bertanda
COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v12 (orb + obrolan saat ini; riwayat chat tersembunyi
di atas; orb SELALU tampil di atas papan ketik). Implementasi desain
terbaru ke aplikasi Android menyusul setelah desain disetujui.
