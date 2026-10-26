
---
Task ID: R4-akar-masalah-429
Agent: Z.ai Code (main)
Task: "gemini model status siap .. waktu di chat error 429. pake nanobot bisa" — cari akar masalah sampai ketemu, TANPA revisi buta

Work Log:
- Bedah kode: chat tangga-1 kirim tools google_search (default webSearch=true), sedangkan testModel ping TANPA tools; 429 tidak masuk daftar retriable {400,404,500,503} → chat berhenti total saat 429
- Buat workflow diag-chat-429.yml: matriks diferensial S0(uji)/S1(ber-tools)/S2(tanpa-tools)/S3(curl) di runner AS
- Debug 1: 404 kosong instan utk semua model → URL kurang segmen models/ → diperbaiki + guard ListModels
- Hasil matriks (run 40339627196): 4/4 model → S0=200, S1=429 instan (0,2 dtk, RESOURCE_EXHAUSTED), S2=200 dgn jawaban utuh → VONIS: TOOLS PELAKU (kuota grounding google_search free tier = 0)
- Perbaikan minimal K16 di GeminiClient.kt: retriable += (429 && useTools && webSearch) → 429 ber-tools turun tangga ke bentuk tanpa-tools yang terbukti 200; header KDoc diperbarui
- Bump versionCode 7 / versionName 1.0.2; README K16; commit + push; pantau CI & release

Stage Summary:
- Akar masalah TERBUKTI dgn bukti server: tools google_search → 429 instan; tanpa tools → 200
- Fix satu baris terarah; perilaku chat baru = tangga-1 (429 instan) → tangga-2 (200) → jawaban utuh

---
Task ID: R5-hapus-riset-web
Agent: Z.ai Code (main)
Task: "hapus riset web total. lebih baik nanti pake provider lain seperti DDG. wajib jalan fitur utama: obrolan berjalan + buka aplikasi"

Work Log:
- Petakan semua jejak: webSearch (SettingsRepository 4 titik), researchMode (field runtime + isResearchRequest), GeminiTool/tools (Dtos), tools di buildBody+tangga (GeminiClient), MODE RISET + "gunakan pencarian web" (SystemPrompts), saran riset di pesan 429 (AiClient)
- Hapus SEMUA: field+key DataStore webSearch, researchMode, isResearchRequest, GeminiTool+field tools, tangga 3→2 bentuk (lengkap tanpa-tools → curl bukti), prompt riset diganti jawaban jujur soal data terkini, pesan 429 jujur soal jatah per menit/hari
- Verifikasi manifest: <queries> LAUNCHER sudah ada (wajib Android 11+ utk daftar app)
- Perkuat buka-app: kata kerja jalankan/bukakan + word-boundary di pembersih kata (nama app "buku" tidak rusak oleh "bu")
- rg final: hanya komentar KDoc GeminiClient yg menyebut google_search (dokumentasi alasan penghapusan)
- Bump v1.0.3(8), README K17, push, pantau CI & release

Stage Summary:
- Seluruh request chat kini murni jalur tanpa-tools yang terbukti 200 (matriks AS 4/4)
- Buka aplikasi: jalur lokal langsung + jalur [BUKA:] model, manifest aman, pencocokan diperkuat

---
Task ID: R6-panggilan-custom-ui-orb
Agent: Z.ai Code (main)
Task: "panggilan custom harus ada dong, jangan cuma pak doang; bagian status sistem di pengaturan hapus aja; teks muter di orb bagus di versi awal muter nya gak beraturan"

Work Log:
- Verifikasi warisan R5: commit K17 (hapus riset web) sudah push & CI sukses; release berisi AVI-v1.0.3-8-a784af6.apk (21,9 MB)
- PANGGILAN CUSTOM: field userName sudah ada di lapisan data tapi TIDAK punya kolom UI → panel baru "PEMILIK → PANGGILAN SAYA" di SettingsScreen (DataStore, bawaan "Pak", kosong→"Pak")
- Seluruh lapisan bicara ikut panggilan: SystemPrompts (contoh "Siap <u>!" dinamis + larangan panggil "Pak" bila bukan panggilannya), sapaan boot (tunggu settings.first() dulu), greetingText, balasan buka-app, fallback "Siap <u>.", clearHistory, DeviceActions.summary/localAnswer (param panggilan default "Pak")
- Pembersih kata buka-app: panggilan custom dibuang HANYA di posisi awal/akhir kalimat (Regex edge) supaya nama aplikasi yang kebetulan sama tidak rusak
- STATUS SISTEM DIHAPUS: gauge BATERAI/RAM/PENYIMPANAN/JARINGAN di panel E SISTEM dihapus + state diagnostics & LaunchedEffect refresh di SettingsScreen dibersihkan (telemetri tetap hidup di orbit orb; refreshDiagnostics tetap dipanggil init ViewModel); panel SISTEM kini: versi APK + info perangkat + pemakaian API + hapus riwayat
- TEKS MUTER ORB: ReactorHalo PITA 2 diberi wobble kecepatan (16·sin(2πt)+10·sin(2π·3t+1,7)) — periodik thd siklus animasi supaya mulus tanpa lompatan, tanpa alokasi per-frame; titik pemisah ikut sudut wobble
- Header Pusat Kendali "SISTEM MARKAS — v1.0.0" tercetak manual → versi dinamis dari PackageManager
- Import orphans dibersihkan (LaunchedEffect, DeviceDiagnostics); kurung diverifikasi seimbang di luar string
- Bump v1.0.4(9), README K18; push + pantau CI + verifikasi APK di release

Stage Summary:
- Panggilan custom kini nyata di semua titik bicara AVI (UI + prompt + balasan lokal)
- Panel SISTEM bersih dari gauge status; telemetri tetap di orb
- Teks orbit kembali gak beraturan seperti versi awal (kecepatan berombak, mulus)
- v1.0.4 (build 9) — CI/Release mengikuti alur K15 (nama APK unik per build)
