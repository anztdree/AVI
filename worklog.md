
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
