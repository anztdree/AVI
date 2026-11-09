#!/usr/bin/env python3
# APK v1.0.0 — REBRAND MULAI DARI AWAL (umpan Pak: "implementasi jadi APK mulai dari
# versi 1.0.0. hapus semua versi sebelumnya.. mulai dari awal")
# - Identitas terlihat: judul, meta, footer, header, pengaturan, IDLE text -> APK v1.0.0
# - Semua penanda versi lama (v40-v52, MK-XVIII/XIX/XXI, REGALIA) dibersihkan dari komentar
# - Tampilan & perilaku tidak diubah sama sekali
import re, sys

P = '/home/z/my-project/public/avi-agent.html'
s = open(P, encoding='utf-8').read()
ops = []
def sub(name, old, new, n=1):
    global s
    s, c = re.subn(re.escape(old), lambda m: new, s, count=n)
    ops.append((name, c, n))
def subre(name, pat, rep, n=None):
    global s
    s, c = re.subn(pat, rep, s)
    ops.append((name, c, n if n is not None else c))

# ============ IDENTITAS TERLIHAT ============
sub('title', '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v52</title>',
    '<title>AVI — Asisten Pribadi · APK v1.0.0</title>')
sub('meta-versi',
    '<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover, interactive-widget=resizes-content"/>',
    '<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover, interactive-widget=resizes-content"/>\n<meta name="version" content="1.0.0"/>')
sub('hdr-1', '   AVI v43 — "MK-XXI REGALIA" : TEMA OBROLAN DIRANCANG ULANG',
    '   AVI APK v1.0.0 — TEMA OBROLAN DIRANCANG ULANG')
sub('hdr-2', '   terlihat sampah. Jawaban REGALIA — kemewahan lewat LAPISAN:',
    '   terlihat sampah. Jawabannya — kemewahan lewat LAPISAN:')
sub('hdr-3', '   (5) ORB MK-XIX v41 TETAP UTUH — tidak disentuh sama sekali.',
    '   (5) BENTUK ORB TETAP UTUH — tidak disentuh sama sekali.')
sub('note',
    '<div class="note">MK-XXI REGALIA v52 · BAR EQ & PINDAI DENGAR DIHAPUS — WAJAH ORB BERSIH & TENANG · ORB MENYALA HIJAU SAAT MENDENGAR · MK-XIX v41 TETAP</div>',
    '<div class="note">AVI APK v1.0.0 · SUARA CUMA VIA ORB — ORB MENYALA HIJAU SAAT MENDENGAR · WAJAH ORB BERSIH & TENANG</div>')
sub('mark', 'title="AVI MK-XIX"', 'title="AVI v1.0.0"')
sub('toast', 'data-toast="AVI MK-XIX · HUD SENTRI — kerangka pondasi REGALIA"',
    'data-toast="AVI v1.0.0 · HUD SENTRI — kerangka pondasi APK"')
sub('tentang', '<small>MK-XIX · HUD SENTRI</small>', '<small>APK v1.0.0 · HUD SENTRI</small>')
sub('idle-tx', "const IDLE_TX='MK-XIX · HUD SENTRI · NET AKTIF · MEMORI 62%';",
    "const IDLE_TX='AVI v1.0.0 · HUD SENTRI · NET AKTIF · MEMORI 62%';")

# ============ KOMENTAR: FRASA EKSPLISIT ============
sub('plasma-morf', 'bola plasma morf v40 DIANGKAT total', 'bola plasma morf lama DIANGKAT total')
sub('janin', '(v43.2: terbaca seperti janin bergerak, ditolak Pak).', '(terbaca seperti janin bergerak, ditolak Pak).')
sub('mkxviii', '/* — MK-XVIII PLASMA HIDUP (v43.2): lensa reaktor konsep 2 yang diberi nyawa.',
    '/* — PLASMA HIDUP: lensa reaktor konsep 2 yang diberi nyawa.')
sub('alas', 'dekor latar, orb v41 TIDAK disentuh', 'dekor latar, bentuk orb TIDAK disentuh')
sub('debu', 'dekor murni, orb MK-XIX v41 tidak disentuh', 'dekor murni, bentuk orb tidak disentuh')
sub('bersih-hidup', 'bentuk orb MK-XIX v41 tetap tidak disentuh', 'bentuk orb tetap tidak disentuh')
sub('orb-hero', 'ORB MK-XIX HUD SENTRI — HERO di panggung bawah: di atas komposer, diangkat SEDIKIT dari posisi lama (v41 · TIDAK DISENTUH)',
    'ORB HUD SENTRI — HERO di panggung bawah (bentuk TIDAK DISENTUH)')
sub('kartu', 'kartu REGALIA: kaca pekat', 'kartu utama: kaca pekat')

# ============ KOMENTAR: TOKEN VERSI GENERIK (hanya v40-v59, v1.0.0 aman) ============
subre('token-gabungan', r'\(v[45]\d(?:\.\d+)?/v[45]\d(?:\.\d+)?\)', '')
subre('token-bare', r'\(v[45]\d(?:\.\d+)?\)', '')
subre('token-colon', r'\(v[45]\d(?:\.\d+)?:\s*', '(')
subre('token-teks', r'\s+v[45]\d(?:\.\d+)?', '')
subre('spasi-komentar', r'/\*  ', '/* ')

open(P, 'w', encoding='utf-8').write(s)

bad = 0
print('== HASIL ==')
for name, c, n in ops:
    ok = (c == n)
    print(f'{name:16s} match={c} {"OK" if ok else "GAGAL!!"}')
    if not ok: bad += 1

left = {
  'sisa v40-v59': re.findall(r'v[45]\d(?:\.\d+)?', s),
  'sisa MK-XVIII': re.findall(r'MK-XVIII', s),
  'sisa MK-XIX': re.findall(r'MK-XIX', s),
  'sisa MK-XXI': re.findall(r'MK-XXI', s),
  'sisa REGALIA': re.findall(r'REGALIA', s),
}
tot = 0
for k, v in left.items():
    print(f'{k:16s}: {len(v)} {v[:4]}')
    tot += len(v)
ok_v = re.findall(r'v1\.0\.0', s)
print(f'APK v1.0.0 ada    : {len(ok_v)}')
print(f'hexagram utuh     : {len(re.findall(chr(99)+"lip-path:polygon(50% 0,86% 100%,14% 100%)", s))}')
print(f'menyala hijau     : {len(re.findall(r"brightness.2\.5. saturate.1\.75.", s))}')
print(f'v52 marker sisa   : {len(re.findall(r"v52", s))}')
print('BERSIH' if (bad == 0 and tot == 0 and len(ok_v) >= 4) else 'ADA SISA/GAGAL')
sys.exit(0 if (bad == 0 and tot == 0 and len(ok_v) >= 4) else 1)
