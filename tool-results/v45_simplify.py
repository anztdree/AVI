#!/usr/bin/env python3
# v45 — LATAR BERSIH-HIDUP: buang semua corak statis, sisakan animasi yang jelas terlihat
import re, sys

p = '/home/z/my-project/public/avi-agent.html'
s = open(p, encoding='utf-8').read()
orig_len = len(s)

def sub1(pat, rep, label):
    global s
    ns, n = re.subn(pat, rep, s, count=1)
    if n != 1:
        print(f'FAIL: {label} (match={n})'); sys.exit(1)
    s = ns
    print(f'ok: {label}')

# 1) SPECKS (bintang statis) + keyframes twk — HAPUS
sub1(r"/\* \(v44\.0\) latar diperkaya[\s\S]*?@keyframes twk\{[^\n]*\}\n", "", "hapus specks+twk")

# 2) gC (pendaran ke-4) — HAPUS
sub1(r"\.bgFx \.gC\{[^\n]*\n", "", "hapus gC")

# 3) gD sedikit lebih hidup (lantai bawah, menopang orb)
sub1(r"\.bgFx \.gD\{width:72%;height:36%;left:14%;bottom:-15%;background:rgba\(45,217,245,\.10\);animation:driftB 23s",
     ".bgFx .gD{width:72%;height:36%;left:14%;bottom:-15%;background:rgba(45,217,245,.14);animation:driftB 19s", "gD diperkuat")

# 4) aurora sedikit lebih terlihat (tetap lembut)
sub1(r"\.bgFx \.aur\{left:6%;right:6%;top:9%;height:26%;filter:blur\(30px\);opacity:\.48;",
     ".bgFx .aur{left:6%;right:6%;top:9%;height:26%;filter:blur(30px);opacity:.55;", "aurora .55")

# 5) SWEEP (radar, tak terlihat) — HAPUS
sub1(r"\.bgFx \.sweep\{[\s\S]*?@keyframes sweepD\{[^\n]*\}\n", "", "hapus sweep+sweepD")

# 6) METEOR: satu saja (m2 dihapus dari markup), lebih terlihat & elegan
sub1(r"\.bgFx \.meteor\{[\s\S]*?@keyframes meteorD\{[^\n]*\}\n",
""".bgFx .meteor{left:13%;top:8%;width:150px;height:2px;border-radius:2px;filter:none;opacity:0;
  background:linear-gradient(90deg,transparent 0,rgba(45,217,245,.55) 44%,rgba(240,253,255,.95) 96%);
  box-shadow:0 0 9px rgba(45,217,245,.4);
  animation:meteorD 10.5s ease-in infinite 2.2s;will-change:transform,opacity}
@keyframes meteorD{0%{opacity:0;transform:rotate(24deg) translateX(-80px)}4%{opacity:.9}16%{opacity:0;transform:rotate(24deg) translateX(280px)}100%{opacity:0;transform:rotate(24deg) translateX(280px)}}
""", "meteor tunggal baru")

# 7) tema siang + reduced-motion: bersihkan referensi elemen yang dihapus
sub1(r"/\* tema siang[\s\S]*?@media\(prefers-reduced-motion:reduce\)\{[^\n]*\}\n",
"""/* tema siang: animasi latar diredam agar tetap elegan */
html[data-theme="siang"] .motes{opacity:.5}
html[data-theme="siang"] .bgFx .meteor{display:none}
@media(prefers-reduced-motion:reduce){.bgFx .gA,.bgFx .gB,.bgFx .gD,.bgFx .aur,.bgFx .motes b,.bgFx .meteor,#orbDock::before{animation:none}}
""", "siang+reduced-motion dirapikan")

# 8) BLOK BESAR: v44.4 override + ruler/hatch + FOTO AI (base64 177KB) + gridL + noise + orn — HAPUS SEMUA
sub1(r"/\* \(v44\.4\) LATAR TENANG[\s\S]*?\.orn\.br\{[^\n]*\}\n",
"""/* (v45) LATAR BERSIH-HIDUP (umpan Pak: corak kebanyakan & jelek, animasi tak terlihat) —
   foto AI, grid blueprint, specks, grain, ornamen sudut, penggaris, arsir, sapuan radar: SEMUA dipensiunkan.
   Sisa latar: 3 pendaran lembut + aurora + 9 debu energi + 1 bintang jatuh — sedikit elemen, semua bergerak nyata.
   Panggung orb jadi area paling bersih & kontras; bentuk orb MK-XIX v41 tetap tidak disentuh */
@keyframes driftA{to{transform:translate(66px,52px) scale(1.18)}}
@keyframes driftB{to{transform:translate(-60px,-46px) scale(1.14)}}
""", "blok corak statis dihapus + drift diperbesar")

# 9) MARKUP bgFx: foto/scrim/gC/specks/gridL/noise/orn/ruler/hatch/sweep/m2 DIBUANG, motes 22→9 (lebih besar & terang)
motes = "".join(
    f'<b style="--x:{x};--s:{s_};--c:{c};--d:{d};--dl:{dl};--o:{o};--dx:{dx}"></b>'
    for x, s_, c, d, dl, o, dx in [
        ('4%',  '3px',   '#2DD9F5', '14s',   '-2s',   '.6',  '18px'),
        ('14%', '2.2px', '#F6C15B', '18s',   '-7s',   '.52', '-14px'),
        ('25%', '3.6px', '#5EEAD4', '15s',   '-4s',   '.58', '24px'),
        ('37%', '2.4px', '#BFE9FF', '19s',   '-11s',  '.55', '-18px'),
        ('49%', '3.2px', '#F6C15B', '16s',   '-6s',   '.6',  '16px'),
        ('61%', '2.2px', '#2DD9F5', '21s',   '-13s',  '.5',  '-12px'),
        ('72%', '3.8px', '#5EEAD4', '14.5s', '-8s',   '.6',  '26px'),
        ('84%', '2.6px', '#F6C15B', '17.5s', '-3.5s', '.58', '-20px'),
        ('93%', '2.4px', '#2DD9F5', '20s',   '-9.5s', '.52', '14px'),
    ]
)
sub1(r'  <div class="bgFx">[\s\S]*?</div>\n  <div class="vig"></div>',
     f'  <div class="bgFx"><i class="gA"></i><i class="gB"></i><i class="gD"></i><i class="aur"></i>'
     f'<div class="motes">{motes}</div><i class="meteor m1"></i></div>\n  <div class="vig"></div>',
     "markup bgFx baru (5 lapis animasi)")

# 10) vignette sedikit digelapkan agar orb makin kontras
sub1(r"\.vig\{position:absolute;inset:0;z-index:0;pointer-events:none;box-shadow:inset 0 0 120px rgba\(0,0,0,\.55\)\}",
     ".vig{position:absolute;inset:0;z-index:0;pointer-events:none;box-shadow:inset 0 0 120px rgba(0,0,0,.62)}",
     "vignette .62")

# 11) versi
sub1(r"<title>AVI — Asisten Pribadi · MK-XXI REGALIA v44\.4</title>",
     "<title>AVI — Asisten Pribadi · MK-XXI REGALIA v45</title>", "title v45")
sub1(r"MK-XXI REGALIA v44\.4 · LATAR TENANG — ORB DISOROT PANGGUNG · ANIMASI TETAP · MK-XIX v41 TETAP",
     "MK-XXI REGALIA v45 · LATAR BERSIH-HIDUP — SEDIKIT ELEMEN, SEMUA BERGERAK · MK-XIX v41 TETAP", "note v45")

open(p, 'w', encoding='utf-8').write(s)
print(f'size: {orig_len} -> {len(s)} bytes ({orig_len-len(s)} dibuang)')

# sanity: tidak boleh ada sisa referensi corak lama
left = [w for w in ['class="shot"','class="specks"','class="sweep"','class="gridL','class="noise"',
                    'class="orn ','class="ruler','class="hatch','meteor m2','gC','base64'] if w in s]
print('sisa corak:', left if left else 'BERSIH ✓')
