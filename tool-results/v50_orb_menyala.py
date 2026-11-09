#!/usr/bin/env python3
# v50 — ORB MENYALA: badan orb SENDIRI berubah wujud saat diklik (bukan cuma animasi pinggiran)
# Umpan Pak: "yg jadi masalah orb-nya bukan animasi saat orb di klik"
# Prinsip: MK-XIX v41 bentuk TIDAK disentuh — hanya warna/terang/intensitas (diizinkan).
import re, sys

P = '/home/z/my-project/public/avi-agent.html'
s = open(P, encoding='utf-8').read()
ops = []
def sub(name, old, new, n=1):
    global s
    s, c = re.subn(re.escape(old), lambda m: new, s, count=n)
    ops.append((name, c, c == n))
    return c

# 1) judul & note versi
sub('title', '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v49</title>',
    '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v50</title>')
sub('note',
    'MK-XXI REGALIA v49 · DENGAR = DRAMATIS — ORB MEMBESAR + LEDAKAN CAHAYA + GELUMBANG SONAR TERANG · SUARA CUMA VIA ORB · MK-XIX v41 TETAP',
    'MK-XXI REGALIA v50 · ORB-nya SENDIRI MENYALA HIJAU SAAT MENDENGAR (bukan cuma animasi pinggiran) · SUARA CUMA VIA ORB · MK-XIX v41 TETAP')

# 2) cakram orb: gelap -> MENYALA HIJAU EMERALD saat hear (state persist, transisi .38s)
sub('ldisk-hear',
    '.orbBig.hear .ldisk{filter:brightness(1.32) saturate(1.22)}',
    '''/* (v50) AKAR KELUHAN PAK: "yg jadi masalah orb-nya bukan animasi saat orb di klik"
   -> BADAN ORB SENDIRI berubah wujud saat diklik: cakram gelap MENYALA HIJAU EMERALD
   (nyala terus selama mendengar — jelas beda, bukan gerak halus yang bisa terlewat) */
.orbBig .ldisk{transition:filter .38s ease,box-shadow .38s ease,border-color .38s ease}
.orbBig.hear .ldisk{filter:brightness(2.5) saturate(1.75) hue-rotate(-38deg);border-color:rgba(167,243,208,.78);box-shadow:0 0 30px 9px rgba(52,211,153,.5),0 0 76px 24px rgba(52,211,153,.22),inset 0 6px 12px rgba(205,255,236,.35),inset 0 -10px 16px rgba(0,0,0,.8),inset 0 0 28px rgba(16,185,129,.55)}''')

# 3) coreGlow: transisi filter + kolam plasma emerald saat hear
sub('coreGlow-tr', 'transition:width .5s,height .5s,margin .5s}',
    'transition:width .5s,height .5s,margin .5s,filter .38s ease}')

# 4) blok v50 utama: wajah & energi orb jadi HIJAU saat mendengar
sub('blok-v50',
    '@keyframes eqD{0%{height:26%}30%{height:94%}55%{height:36%}78%{height:100%}100%{height:26%}}',
    '''@keyframes eqD{0%{height:26%}30%{height:94%}55%{height:36%}78%{height:100%}100%{height:26%}}

/* — (v50) DENGAR = ORB MENYALA HIJAU: seluruh wajah & energi orb berganti warna state —
   heksagram D1 menyala penuh, plasma kolam emerald, EQ hijau, segitiga kaca hijau,
   halo/pulsa/energi orbit ikut emerald — satu identitas "MIC HIDUP" — */
.lface .hexp{transition:opacity .4s}
.orbBig.hear .lface .hexp{opacity:1}
.orbBig.hear .lface .hexp i{background:linear-gradient(180deg,rgba(167,243,208,.95),rgba(16,185,129,.35))}
.orbBig.hear .lface .hexp i.r{background:linear-gradient(180deg,rgba(253,230,138,.92),rgba(245,181,74,.3))}
.orbBig.hear .lface .coreGlow{background:radial-gradient(circle,rgba(209,250,229,.92) 0%,rgba(52,211,153,.48) 45%,transparent 72%);filter:brightness(1.5)}
.orbBig.hear .ltri{opacity:1}
.orbBig.hear .ltri b{background:linear-gradient(180deg,rgba(236,253,245,.98),rgba(110,231,183,.8) 55%,rgba(16,185,129,.92))}
.orbBig.hear .ltri b::after{background:rgba(3,26,18,.55)}
.orbBig.hear .lscanH{background:linear-gradient(180deg,transparent,rgba(167,243,208,.35) 40%,rgba(240,255,248,.6) 50%,rgba(167,243,208,.35) 60%,transparent)}
.orbBig.hear .eq u{background:linear-gradient(180deg,#EAFFF6,rgba(52,211,153,.9));box-shadow:0 0 8px rgba(52,211,153,.75)}
.orbBig.hear .eq u:nth-child(4){background:linear-gradient(180deg,#FFF4DA,rgba(245,181,74,.9));box-shadow:0 0 7px rgba(245,181,74,.65)}
.orbBig.hear .lseg{filter:brightness(1.35) drop-shadow(0 0 8px rgba(52,211,153,.75))}
.orbBig.hear .ltick{filter:hue-rotate(-38deg) brightness(1.4)}
.orbBig.hear .lbkt{border-color:rgba(167,243,208,.72);opacity:1}
.orbBig.hear .pulse{border-color:rgba(167,243,208,.6)}
.orbBig.hear .carc.a i{border-top-color:rgba(167,243,208,.95);border-right-color:rgba(52,211,153,.55);filter:drop-shadow(0 0 5px rgba(52,211,153,.6))}
.orbBig.hear .spark s{background:#EFFFF9;box-shadow:0 0 8px 2px rgba(52,211,153,.85),0 0 18px 6px rgba(52,211,153,.35)}
.orbBig.hear .aura{filter:hue-rotate(-38deg) brightness(1.35) saturate(1.3)}''')

# 5) energi di belakang lensa ikut hijau
sub('mhalo-hear', '.orbBig.hear .mhalo{filter:brightness(1.9)}',
    '.orbBig.hear .mhalo{filter:brightness(1.9) hue-rotate(-38deg)}')
sub('k1-hear',
    '.orbBig.hear .k1{filter:brightness(1.5) drop-shadow(0 0 9px rgba(34,211,238,.95)) drop-shadow(0 0 28px rgba(34,211,238,.5))}',
    '.orbBig.hear .k1{filter:brightness(1.5) hue-rotate(-38deg) drop-shadow(0 0 9px rgba(52,211,153,.95)) drop-shadow(0 0 28px rgba(52,211,153,.5))}')
sub('hdata-hear', '.orbBig.hear .hdata{filter:brightness(1.8)}',
    '.orbBig.hear .hdata{filter:brightness(1.8) hue-rotate(-38deg)}')
sub('lring-hear',
    '.orbBig.hear .lring{border-color:#DFF7FF;box-shadow:0 0 26px rgba(34,211,238,.95),0 0 62px rgba(34,211,238,.4),inset 0 0 20px rgba(34,211,238,.55)}',
    '.orbBig.hear .lring{border-color:#DFFFEE;box-shadow:0 0 26px rgba(52,211,153,.95),0 0 62px rgba(52,211,153,.42),inset 0 0 20px rgba(52,211,153,.55)}')

# 6) sonar / glow / denyut kata -> emerald (hanya tampil saat hear)
sub('glow-after',
    "background:radial-gradient(circle,rgba(34,211,238,.5) 0%,rgba(34,211,238,.2) 48%,transparent 72%)}",
    "background:radial-gradient(circle,rgba(52,211,153,.55) 0%,rgba(52,211,153,.22) 48%,transparent 72%)}")
sub('ear1',
    "background:radial-gradient(circle,transparent 46%,rgba(140,238,255,.42) 60%,rgba(190,247,255,.85) 70%,transparent 80%);",
    "background:radial-gradient(circle,transparent 46%,rgba(110,231,183,.45) 60%,rgba(167,243,208,.88) 70%,transparent 80%);")
sub('wpulse',
    "background:radial-gradient(circle,transparent 52%,rgba(190,247,255,.5) 66%,rgba(255,255,255,.22) 74%,transparent 82%);",
    "background:radial-gradient(circle,transparent 52%,rgba(167,243,208,.55) 66%,rgba(255,255,255,.22) 74%,transparent 82%);")
sub('wpulse-c',
    "background:radial-gradient(circle,rgba(230,252,255,.8) 0%,rgba(34,211,238,.35) 55%,transparent 75%)}",
    "background:radial-gradient(circle,rgba(209,250,229,.85) 0%,rgba(52,211,153,.4) 55%,transparent 75%)}")

# 7) bar hasil suara: aksen hijau saat listening (malam)
sub('bar-b', '.cmdVoice.listening{border-color:rgba(45,217,245,.55)}',
    '.cmdVoice.listening{border-color:rgba(52,211,153,.6)}')
sub('bar-m', '.cmdVoice.listening .vmic{border-color:rgba(45,217,245,.8);box-shadow:0 0 18px -2px rgba(45,217,245,.55);animation:vmicD 1.4s ease-in-out infinite}',
    '.cmdVoice.listening .vmic{border-color:rgba(52,211,153,.85);box-shadow:0 0 18px -2px rgba(52,211,153,.6);animation:vmicD 1.4s ease-in-out infinite}')
sub('bar-t', '.cmdVoice.listening .vtext{color:#D9F7FF}',
    '.cmdVoice.listening .vtext{color:#DAFFEF}')

# 8) tema siang: tinta sian v48 -> tinta EMERALD agar satu identitas
sub('s-ear', 'html[data-theme="siang"] .orbBig .ear i{background:radial-gradient(circle,transparent 46%,rgba(9,125,150,.32) 60%,rgba(12,110,136,.62) 70%,transparent 80%)}',
    'html[data-theme="siang"] .orbBig .ear i{background:radial-gradient(circle,transparent 46%,rgba(5,150,105,.34) 60%,rgba(6,125,95,.66) 70%,transparent 80%)}')
sub('s-glow', 'html[data-theme="siang"] .orbBig .hearGlow::after{background:radial-gradient(circle,rgba(9,125,150,.24) 0%,rgba(9,125,150,.1) 48%,transparent 72%)}',
    'html[data-theme="siang"] .orbBig .hearGlow::after{background:radial-gradient(circle,rgba(5,150,105,.28) 0%,rgba(5,150,105,.12) 48%,transparent 72%)}')
sub('s-wp', 'html[data-theme="siang"] .orbBig .wpulse{background:radial-gradient(circle,transparent 52%,rgba(12,110,136,.42) 66%,rgba(9,125,150,.2) 74%,transparent 82%)}',
    'html[data-theme="siang"] .orbBig .wpulse{background:radial-gradient(circle,transparent 52%,rgba(6,125,95,.46) 66%,rgba(5,150,105,.22) 74%,transparent 82%)}')
sub('s-wpc', 'html[data-theme="siang"] .orbBig .wpulse::before{background:radial-gradient(circle,rgba(210,245,252,.9) 0%,rgba(12,110,136,.3) 55%,transparent 75%)}',
    'html[data-theme="siang"] .orbBig .wpulse::before{background:radial-gradient(circle,rgba(190,247,225,.92) 0%,rgba(5,150,105,.32) 55%,transparent 75%)}')
sub('s-lring', 'html[data-theme="siang"] .orbBig.hear .lring{border-color:#0A7A96;box-shadow:0 0 24px rgba(9,125,150,.7),0 0 52px rgba(9,125,150,.3),inset 0 0 18px rgba(9,125,150,.4)}',
    'html[data-theme="siang"] .orbBig.hear .lring{border-color:#0B7A5A;box-shadow:0 0 24px rgba(5,150,105,.75),0 0 52px rgba(5,150,105,.32),inset 0 0 18px rgba(5,150,105,.42)}')
sub('s-vtext', 'html[data-theme="siang"] .cmdVoice.listening .vtext{color:#075E75}',
    'html[data-theme="siang"] .cmdVoice.listening .vtext{color:#067A5B}')
sub('s-vmic', 'html[data-theme="siang"] .cmdVoice.listening .vmic{border-color:rgba(9,125,150,.65);color:#0A7A96}',
    'html[data-theme="siang"] .cmdVoice.listening .vmic{border-color:rgba(5,150,105,.7);color:#0B7A5A}')

# 9) siang: badan lensa PUTIH — state hijau via cincin & isi wajah tinta emerald
sub('s-blok', 'html[data-theme="siang"] .cmdVoice.listening .vmic{border-color:rgba(5,150,105,.7);color:#0B7A5A}',
    '''html[data-theme="siang"] .cmdVoice.listening .vmic{border-color:rgba(5,150,105,.7);color:#0B7A5A}
/* (v50) siang: badan lensa PUTIH — state hijau via cincin & isi wajah tinta emerald */
html[data-theme="siang"] .orbBig.hear .ldisk{filter:none;border-color:rgba(5,150,105,.72);box-shadow:0 0 24px 7px rgba(5,150,105,.42),0 0 56px 18px rgba(5,150,105,.2),inset 0 0 26px rgba(5,150,105,.3)}
html[data-theme="siang"] .orbBig.hear .lface .coreGlow{background:radial-gradient(circle,rgba(209,250,229,.95) 0%,rgba(5,150,105,.42) 45%,transparent 72%);filter:none}
html[data-theme="siang"] .orbBig.hear .lface .hexp i{background:linear-gradient(180deg,rgba(6,125,95,.85),rgba(5,150,105,.35))}
html[data-theme="siang"] .orbBig.hear .lface .hexp i.r{background:linear-gradient(180deg,rgba(176,110,10,.85),rgba(176,110,10,.35))}
html[data-theme="siang"] .orbBig.hear .ltri b{background:linear-gradient(180deg,rgba(236,253,245,.95),rgba(110,231,183,.55) 55%,rgba(6,125,95,.8))}
html[data-theme="siang"] .orbBig.hear .eq u{background:linear-gradient(180deg,#EAFBF4,rgba(5,150,105,.85));box-shadow:0 0 7px rgba(5,150,105,.6)}
html[data-theme="siang"] .orbBig.hear .eq u:nth-child(4){background:linear-gradient(180deg,#FFF4DA,rgba(176,110,10,.85));box-shadow:0 0 7px rgba(176,110,10,.55)}
html[data-theme="siang"] .orbBig.hear .lseg{filter:drop-shadow(0 0 5px rgba(5,150,105,.6))}
html[data-theme="siang"] .orbBig.hear .mhalo{background:radial-gradient(circle,rgba(5,150,105,.2) 0%,rgba(5,150,105,.08) 52%,transparent 74%);filter:none}''')

open(P, 'w', encoding='utf-8').write(s)

ok = all(c == n for _, c, n in [(a, b, d) for a, b, d in ops if isinstance(d, bool)] )
print('== HASIL ==')
bad = 0
for name, c, target in [(n, c, c == 1) for n, c in ops]:
    pass
for (name, c, passed) in ops:
    print(f'{name:14s} match={c} {"OK" if passed else "GAGAL!!"}')
    if not passed: bad += 1
print('SEMUA OK' if bad == 0 else f'{bad} OPERASI GAGAL')

# sanity
import io
checks = {
  'emerald rgba(52,211,153': len(re.findall(r'rgba\(52,211,153', s)),
  'hue-rotate(-38deg)': len(re.findall(r'hue-rotate\(-38deg\)', s)),
  'sisa brightness(1.32)': len(re.findall(r'brightness\(1\.32\)', s)),
  'v50 markers': len(re.findall(r'v50', s)),
  'gold wave sonar utuh': len(re.findall(r'rgba\(245,181,74,\.34\)', s)),
  'MK-XIX v41 tetap': len(re.findall(r'MK-XIX v41', s)),
  'heksagram clip-path utuh': len(re.findall(r'clip-path:polygon\(50% 0,86% 100%,14% 100%\)', s)),
}
for k, v in checks.items():
    print(f'{k:28s}: {v}')
sys.exit(0 if bad == 0 else 1)
