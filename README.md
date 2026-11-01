# AVI — Asisten Pribadi (MK-II)

AVI adalah **ASISTEN PRIBADI** yang punya kemampuan agent — bukan perkakas HP.
Dirancang untuk tugas berat: laporan PDF, coding, analisa kode, analisa data.

## Isi repo

| Jalur | Isi |
|---|---|
| `app/` | Source code aplikasi Android AVI (Kotlin + Jetpack Compose) |
| `public/avi-agent.html` | Mockup/preview tampilan MK-II v26 — satu file, self-contained |
| `.github/workflows/build-android.yml` | CI build APK (jalan saat `app/**` berubah) |

## Preview v26 — "PRISM" : TEMA BARU + IKON BARU + TATA LETAK BARU

Masukan Pak: **"dah lah,, suruh tema UI icon tata letak baru,.
tampilan gtu gitu aja muluuu"**. Maka v26 bukan polesan dan bukan
restore — **identitas visual diganti total** dengan rujukan nyata
(Gemini Live: orb aurora + jawaban sebagai teks; iOS Liquid Glass:
dock & kapsul kaca melayang; arsip riset repo: Snapshot, Siri glow,
Pi, Sesame):

1. **Tema aurora prism** — graphite dalam + 3 blob aurora hidup
   (sian / violet / emas) yang bernapas + starfield berkelip;
   grid kotak & kartu kurung sudut v25 DIHAPUS. Kartu kini **kaca
   prisma** dengan border gradien sian→emas (tanpa blur tebal).
2. **Ikon SVG duotone kustom** (sian + titik aksen emas) di squircle
   gradien — karakter unicode ▤◔☀ DIHAPUS; ikon baru untuk brief,
   dock, KENDALI, sheet, dan tema.
3. **Tata letak baru** — nav jadi **dock kapsul melayang** dengan
   indikator aktif yang bergeser halus; komposer jadi **kapsul
   komando tunggal** (input + mikrofon + kirim dalam SATU kapsul);
   tipografi baru: **Unbounded** (display) + **Space Grotesk** (UI).
4. **Orb "PRISM CORE"** — inti pusaran aurora konik berputar +
   highlight kaca + orbit elips miring dengan satelit emas & sian;
   bukan lagi reaktor salinan. Tetap 112px, bersih tanpa banner di
   atas papan ketik, ring progres saat kerja, emas saat tuntas
   (ledakan halo), mengecil saat papan ketik terbuka.
5. **Yang tetap** (arahan Pak yang sudah disetujui): nuansa Jarvis
   sian #22D3EE + emas #F5B54A & telemetri MK1; TANPA riwayat total
   (v23); SATU slot teks di samping orb (v17); perintah DI SAMPING
   orb, jawaban AVI DI ATAS orb (v16/v17); ASISTEN DULU — agent di
   dalam AVI bangun saat tugas berat, gate izin 60%, JEDA/STOP
   (v16); tanpa lampiran/billing/jam; COMING SOON untuk yang belum
   ada; tema siang "fajar prism" / malam "aurora".

Teruji agent-browser desktop 1440×900 & mobile 390×844: boot bersih,
kirim santai → TERKIRIM samping orb → balasan di atas orb (satu slot),
tugas berat → agent penuh (langkah → gate 60% → izinkan → tuntas emas →
kartu PDF → settle), STOP, sheet TUGAS, KENDALI (indikator dock
bergeser), tema siang/malam, mode suara buka-tutup → slot; nol error
konsol.

## Melihat preview tampilan

Buka `public/avi-agent.html` langsung di browser — tidak perlu install
apa pun. Interaksi yang jalan di mockup: obrolan asisten (gelembung
kaca prisma + indikator mengetik), pemicu tugas agent lewat pesan
(mis. "buatkan laporan"), alur laporan lengkap (JEDA/STOP, izin di 60%,
hasil PDF), orb prism dengan koreografi keadaan (ketuk orb = mode
suara), dock kapsul dengan indikator geser, tema malam/siang. Papan
ketik nyata di HP ditangani rapi (`interactive-widget=resizes-content`
+ `visualViewport`). Fitur bertanda COMING SOON belum diimplementasi.

## Status

Tahap desain: mockup v26 (PRISM — tema aurora, ikon SVG duotone, dock
kapsul, orb prism core). Implementasi desain terbaru ke aplikasi
Android menyusul setelah desain disetujui.
