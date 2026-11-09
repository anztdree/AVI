#!/usr/bin/env python3
# v52 — HAPUS BAR NAIK-TURUN (umpan Pak: "itu seperti bar naik turunnya masih ada
#       dan itu justru yg bikin orb jelek")
# (1) Ekualiser 7 batang (eq) di wajah orb: DIHAPUS TOTAL (CSS + markup + keyframes)
# (2) Pindai cepat dengar (lscanH — bar meluncur di wajah orb): DIHAPUS TOTAL juga
# MK-XIX v41 bentuk tetap tidak disentuh; identitas v50 (orb menyala hijau) tetap.
import re, sys

P = '/home/z/my-project/public/avi-agent.html'
s = open(P, encoding='utf-8').read()
ops = []
def sub(name, old, new, n=1):
    global s
    s, c = re.subn(re.escape(old), lambda m: new, s, count=n)
    ops.append((name, c, n))

# 1) versi
sub('title', '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v51</title>',
    '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v52</title>')
sub('note',
    'MK-XXI REGALIA v51 · SONAR SIMPLE SATU CINCIN + HOLOGRAM TEGAK DIAM (goyang & naik-turun dihapus) · ORB MENYALA HIJAU SAAT MENDENGAR · MK-XIX v41 TETAP',
    'MK-XXI REGALIA v52 · BAR EQ & PINDAI DENGAR DIHAPUS — WAJAH ORB BERSIH & TENANG · ORB MENYALA HIJAU SAAT MENDENGAR · MK-XIX v41 TETAP')

# 2) blok CSS lscanH + eq: buang semua, ganti komentar penanda
sub('css-lscanh-eq',
    '''/* pindai cepat khas "sedang mendengar" — hanya tampil saat .hear */
.lface .lscanH{position:absolute;left:2px;right:2px;top:0;height:7px;margin-top:-3.5px;border-radius:3px;mix-blend-mode:screen;
  background:linear-gradient(180deg,transparent,rgba(160,244,255,.3) 40%,rgba(255,255,255,.55) 50%,rgba(160,244,255,.3) 60%,transparent);
  opacity:0;animation:lscanH 1.7s ease-in-out infinite;display:none}
.orbBig.hear .lscanH{display:block}
@keyframes lscanH{0%{opacity:0;transform:translateY(4px)}30%{opacity:.9}80%{opacity:.5}100%{opacity:0;transform:translateY(52px)}}
/* ekualiser dengar: 7 batang bergelombang berjalan sian-emas berselang */
.lface .eq{position:absolute;left:50%;bottom:5px;width:52px;height:20px;margin-left:-26px;z-index:2;
  display:flex;align-items:flex-end;justify-content:space-between;opacity:0;transform:translateY(4px);
  transition:opacity .45s,transform .45s}
.orbBig.hear .eq{opacity:1;transform:none}
.lface .eq u{width:4px;height:30%;border-radius:2px;
  background:linear-gradient(180deg,#EAFBFF,rgba(34,211,238,.85));
  box-shadow:0 0 7px rgba(34,211,238,.7);
  animation:eqD 1.1s ease-in-out infinite}
.lface .eq u:nth-child(2){width:3.4px;animation-duration:.85s;animation-delay:.12s}
.lface .eq u:nth-child(3){width:4.4px;animation-duration:1.35s;animation-delay:.24s}
.lface .eq u:nth-child(4){animation-duration:.95s;animation-delay:.36s;background:linear-gradient(180deg,#FFF4DA,rgba(245,181,74,.9));box-shadow:0 0 7px rgba(245,181,74,.65)}
.lface .eq u:nth-child(5){width:4.4px;animation-duration:1.5s;animation-delay:.48s}
.lface .eq u:nth-child(6){width:3.4px;animation-duration:.9s;animation-delay:.6s}
.lface .eq u:nth-child(7){animation-duration:1.2s;animation-delay:.72s}
@keyframes eqD{0%{height:26%}30%{height:94%}55%{height:36%}78%{height:100%}100%{height:26%}}''',
    '''/* (v52) BAR NAIK-TURUN DIHAPUS TOTAL — umpan Pak: "bar naik turunnya masih ada
   dan itu justru yg bikin orb jelek". Ekualiser 7 batang (eq) & pindai cepat
   dengar (lscanH) dibuang semua — wajah orb bersih & tenang saat mendengar */''')

# 3) rule hijau v50 utk eq & lscanH: buang
sub('v50-lscanh-eq',
    '''.orbBig.hear .lscanH{background:linear-gradient(180deg,transparent,rgba(167,243,208,.35) 40%,rgba(240,255,248,.6) 50%,rgba(167,243,208,.35) 60%,transparent)}
.orbBig.hear .eq u{background:linear-gradient(180deg,#EAFFF6,rgba(52,211,153,.9));box-shadow:0 0 8px rgba(52,211,153,.75)}
.orbBig.hear .eq u:nth-child(4){background:linear-gradient(180deg,#FFF4DA,rgba(245,181,74,.9));box-shadow:0 0 7px rgba(245,181,74,.65)}
''',
    '')

# 4) rule siang utk eq: buang
sub('siang-eq',
    '''html[data-theme="siang"] .orbBig.hear .eq u{background:linear-gradient(180deg,#EAFBF4,rgba(5,150,105,.85));box-shadow:0 0 7px rgba(5,150,105,.6)}
html[data-theme="siang"] .orbBig.hear .eq u:nth-child(4){background:linear-gradient(180deg,#FFF4DA,rgba(176,110,10,.85));box-shadow:0 0 7px rgba(176,110,10,.55)}
''',
    '')

# 5) markup: buang elemen lscanH + eq dari wajah orb
sub('markup',
    '<i class="lscanH"></i><i class="eq"><u></u><u></u><u></u><u></u><u></u><u></u><u></u></i>',
    '')

# 6) komentar dokumentasi ikut jujur
sub('komentar-v50', '   heksagram D1 menyala penuh, plasma kolam emerald, EQ hijau, segitiga kaca hijau,',
    '   heksagram D1 menyala penuh, plasma kolam emerald, segitiga kaca hijau,')
sub('komentar-v49', '   glow kuat bernapas, plasma melmebar, HUD power-up, EQ besar.',
    '   glow kuat bernapas, plasma melmebar & HUD power-up.')

open(P, 'w', encoding='utf-8').write(s)

bad = 0
print('== HASIL ==')
for name, c, n in ops:
    ok = (c == n)
    print(f'{name:16s} match={c} {"OK" if ok else "GAGAL!!"}')
    if not ok: bad += 1

checks = {
  'sisa eqD': len(re.findall(r'eqD', s)),
  'sisa lscanH': len(re.findall(r'lscanH', s)),
  'sisa .eq css': len(re.findall(r'\.eq\b', s)),
  'sisa markup eq': len(re.findall(r'class="eq"', s)),
  'v52 markers': len(re.findall(r'v52', s)),
  'MK-XIX v41 tetap': len(re.findall(r'MK-XIX v41', s)),
  'hexagram clip utuh': len(re.findall(r'clip-path:polygon\(50% 0,86% 100%,14% 100%\)', s)),
  'menyala hijau tetap': len(re.findall(r'brightness\(2\.5\) saturate\(1\.75\)', s)),
  'heksagram menyala tetap': len(re.findall(r'\.orbBig\.hear \.lface \.hexp\{opacity:1\}', s)),
  'cincin sonar tetap': len(re.findall(r'border:1\.5px solid rgba\(110,231,183,\.5\)', s)),
}
for k, v in checks.items(): print(f'{k:26s}: {v}')
sys.exit(0 if bad == 0 else 1)
