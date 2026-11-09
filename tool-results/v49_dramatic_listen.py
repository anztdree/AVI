# -*- coding: utf-8 -*-
# v49: DENGAR = DRAMATIS (umpan Pak: "dari tadi saya komplain gak ada perubahan")
# Akar masalah: idle orb memang sudah ramai animasi -> perubahan halus v47/v48 tak terlihat.
# Solusi: saat mendengarkan orb harus BERUBAH BESAR-besaran dalam <1 detik:
#   1. orb MEMBESAR 16% + terangkat (bukan 4.5%)
#   2. LEDAKAN cahaya saat diklik (wake flash: kilat inti + cincin besar)
#   3. gelumbang sonar TERANG & BESAR (band .85, sampai 2.3x, tempo 2.6s)
#   4. glow sekitar jauh lebih kuat & bernapas lebar
#   5. inti BERDEBAR (thump scale 1.09) + kolam plasma MEMBESAR 36->52px
#   6. EQ lebih besar terang; busur HUD "power up" (brightness 1.5-1.9, bayangan lebar)
#   7. per-kata tetap berdenyut (ring), wake flash versi besar (fill + ring 1.95x)
import re, io

P = '/home/z/my-project/public/avi-agent.html'
s = io.open(P, encoding='utf-8').read()
ops = []

def sub(name, pat, rep, n=1):
    global s
    s2, k = re.subn(pat, rep, s, count=n, flags=re.S)
    assert k == n, f'GAGAL {name}: match={k} (harus {n})'
    s = s2
    ops.append((name, k))

# ---------- versi ----------
sub('title', r'<title>AVI — Asisten Pribadi · MK-XXI REGALIA v48</title>',
    '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v49</title>')
sub('note', r'<div class="note">MK-XXI REGALIA v48 · SUARA HANYA DARI ORB — BAR BAWAH CUMA CERMIN HASIL SUARA · ORB GOYANG ORGANIK SAAT MENDENGAR · MK-XIX v41 TETAP</div>',
    '<div class="note">MK-XXI REGALIA v49 · DENGAR = DRAMATIS — ORB MEMBESAR + LEDAKAN CAHAYA + GELUMBANG SONAR TERANG · SUARA CUMA VIA ORB · MK-XIX v41 TETAP</div>')

# ---------- 1. ORB MEMBESAR nyata saat mendengarkan ----------
sub('hIn dramatis', r'#orbDock \.heroWrap\{transition:transform \.6s cubic-bezier\(\.22,1,\.36,1\)\}\n\.heroWrap\.hIn\{transform:scale\(1\.045\) translateY\(-2px\)\}',
    '#orbDock .heroWrap{transition:transform .6s cubic-bezier(.22,1,.36,1)}\n'
    '/* (v49) mendengarkan = orb MEMBESAR 16% & terangkat — perubahan tak mungkin tak terlihat */\n'
    '.heroWrap.hIn{transform:scale(1.16) translateY(-8px)}')

# ---------- 2. LEDAKAN CAHAYA (wake flash) + denyut kata dua rasa ----------
sub('wpulse flash besar', r'\.orbBig \.wpulse\{position:absolute;inset:-2px;border-radius:50%;opacity:0;pointer-events:none;z-index:2;\n  background:radial-gradient\(circle,transparent 52%,rgba\(190,247,255,\.42\) 68%,rgba\(255,255,255,\.18\) 74%,transparent 82%\);\n  filter:blur\(1px\);transform:scale\(\.88\)\}\n\.orbBig \.wpulse\.go\{animation:wpD \.72s cubic-bezier\(\.2,\.7,\.3,1\) 1\}\n@keyframes wpD\{0%\{opacity:\.9;transform:scale\(\.88\)\}100%\{opacity:0;transform:scale\(1\.6\)\}\}',
    '/* denyut per KATA (ring) + LEDAKAN saat mulai mendengarkan (go.big = kilat inti + cincin raksasa) */\n'
    '.orbBig .wpulse{position:absolute;inset:-2px;border-radius:50%;opacity:0;pointer-events:none;z-index:2;\n'
    '  background:radial-gradient(circle,transparent 52%,rgba(190,247,255,.5) 66%,rgba(255,255,255,.22) 74%,transparent 82%);\n'
    '  filter:blur(1px);transform:scale(.88)}\n'
    '.orbBig .wpulse::before{content:\'\';position:absolute;inset:30%;border-radius:50%;opacity:0;\n'
    '  background:radial-gradient(circle,rgba(230,252,255,.8) 0%,rgba(34,211,238,.35) 55%,transparent 75%)}\n'
    '.orbBig .wpulse.go{animation:wpD .72s cubic-bezier(.2,.7,.3,1) 1}\n'
    '.orbBig .wpulse.go.big{animation:wpD2 .85s ease-out 1}\n'
    '.orbBig .wpulse.go.big::before{animation:wpC .85s ease-out 1}\n'
    '@keyframes wpD{0%{opacity:.9;transform:scale(.88)}100%{opacity:0;transform:scale(1.6)}}\n'
    '@keyframes wpD2{0%{opacity:1;transform:scale(.82)}55%{opacity:.5}100%{opacity:0;transform:scale(1.95)}}\n'
    '@keyframes wpC{0%{opacity:1}100%{opacity:0}}')

# ---------- 3. glow sekitar jauh lebih kuat ----------
sub('hearGlow kuat', r'\.orbBig \.hearGlow\{position:absolute;inset:-10px;border-radius:50%;opacity:0;transition:opacity \.5s;pointer-events:none\}',
    '.orbBig .hearGlow{position:absolute;inset:-20px;border-radius:50%;opacity:0;transition:opacity .45s;pointer-events:none}')
sub('hearGlow grad', r"\.orbBig \.hearGlow::after\{content:'';position:absolute;inset:0;border-radius:50%;\n  background:radial-gradient\(circle,rgba\(34,211,238,\.32\) 0%,rgba\(34,211,238,\.12\) 48%,transparent 72%\)\}",
    ".orbBig .hearGlow::after{content:'';position:absolute;inset:0;border-radius:50%;\n"
    "  background:radial-gradient(circle,rgba(34,211,238,.5) 0%,rgba(34,211,238,.2) 48%,transparent 72%)}")
sub('hearGlow napas lebar', r'@keyframes hearGlowD\{0%,100%\{transform:scale\(1\);filter:brightness\(1\)\}50%\{transform:scale\(1\.09\);filter:brightness\(1\.28\)\}\}',
    '@keyframes hearGlowD{0%,100%{transform:scale(1);filter:brightness(1)}50%{transform:scale(1.15);filter:brightness(1.5)}}')

# ---------- 4. gelumbang sonar TERANG & BESAR ----------
sub('sonar terang besar', r'\.orbBig \.ear i\{position:absolute;inset:-2px;border-radius:50%;opacity:0;will-change:transform,opacity;\n  background:radial-gradient\(circle,transparent 50%,rgba\(140,238,255,\.2\) 63%,rgba\(190,247,255,\.46\) 72%,transparent 83%\);\n  filter:blur\(1\.7px\);\n  animation:earD 3\.6s cubic-bezier\(\.18,\.6,\.32,1\) infinite\}\n\.orbBig \.ear i:nth-child\(2\)\{animation-delay:1\.2s;\n  background:radial-gradient\(circle,transparent 50%,rgba\(245,181,74,\.16\) 63%,rgba\(255,214,140,\.4\) 72%,transparent 83%\)\}\n\.orbBig \.ear i:nth-child\(3\)\{animation-delay:2\.4s;filter:blur\(2\.4px\)\}\n@keyframes earD\{0%\{transform:scale\(\.8\);opacity:0\}14%\{opacity:\.85\}52%\{opacity:\.5\}78%\{opacity:\.2\}100%\{transform:scale\(2\.12\);opacity:0\}\}',
    '.orbBig .ear i{position:absolute;inset:-6px;border-radius:50%;opacity:0;will-change:transform,opacity;\n'
    '  background:radial-gradient(circle,transparent 46%,rgba(140,238,255,.42) 60%,rgba(190,247,255,.85) 70%,transparent 80%);\n'
    '  filter:blur(1.2px);\n'
    '  animation:earD 2.6s cubic-bezier(.16,.6,.3,1) infinite}\n'
    '.orbBig .ear i:nth-child(2){animation-delay:.87s;\n'
    '  background:radial-gradient(circle,transparent 46%,rgba(245,181,74,.34) 60%,rgba(255,214,140,.78) 70%,transparent 80%)}\n'
    '.orbBig .ear i:nth-child(3){animation-delay:1.74s;filter:blur(2px)}\n'
    '@keyframes earD{0%{transform:scale(.82);opacity:0}10%{opacity:1}50%{opacity:.72}100%{transform:scale(2.3);opacity:0}}')

# ---------- 5. HUD "power up" + lring menyala besar ----------
sub('hear filters kuat', r'\.orbBig\.hear \.ldisk\{filter:brightness\(1\.18\) saturate\(1\.12\)\}\n\.orbBig\.hear \.mhalo\{filter:brightness\(1\.45\)\}\n\.orbBig\.hear \.k1\{filter:drop-shadow\(0 0 8px rgba\(34,211,238,\.85\)\) drop-shadow\(0 0 20px rgba\(34,211,238,\.35\)\)\}\n\.orbBig\.hear \.k2\{filter:drop-shadow\(0 0 7px rgba\(245,181,74,\.6\)\)\}\n\.orbBig\.hear \.hdata\{filter:brightness\(1\.5\)\}',
    '.orbBig.hear .ldisk{filter:brightness(1.32) saturate(1.22)}\n'
    '.orbBig.hear .mhalo{filter:brightness(1.9)}\n'
    '.orbBig.hear .k1{filter:brightness(1.5) drop-shadow(0 0 9px rgba(34,211,238,.95)) drop-shadow(0 0 28px rgba(34,211,238,.5))}\n'
    '.orbBig.hear .k2{filter:brightness(1.55) drop-shadow(0 0 8px rgba(245,181,74,.75))}\n'
    '.orbBig.hear .hdata{filter:brightness(1.8)}')
sub('lring menyala', r'\.orbBig\.hear \.lring\{border-color:rgba\(190,247,255,\.95\);box-shadow:0 0 18px rgba\(34,211,238,\.8\),inset 0 0 16px rgba\(34,211,238,\.45\)\}',
    '.orbBig.hear .lring{border-color:#DFF7FF;box-shadow:0 0 26px rgba(34,211,238,.95),0 0 62px rgba(34,211,238,.4),inset 0 0 20px rgba(34,211,238,.55)}')

# ---------- 6. inti BERDEBAR (thump) + plasma membesar ----------
sub('hearBreath thump', r'\.orbBig\.hear \.coreBox\{animation:hearBreath 3\.8s ease-in-out infinite\}\n@keyframes hearBreath\{0%\{transform:scale\(1\) rotate\(0\)\}26%\{transform:scale\(1\.04\) rotate\(\.9deg\)\}47%\{transform:scale\(1\.01\) rotate\(-\.7deg\)\}68%\{transform:scale\(1\.048\) rotate\(\.4deg\)\}100%\{transform:scale\(1\) rotate\(0\)\}\}',
    '.orbBig.hear .coreBox{animation:hearBreath 3.8s ease-in-out infinite}\n'
    '@keyframes hearBreath{0%{transform:scale(1) rotate(0)}24%{transform:scale(1.075) rotate(1.1deg)}44%{transform:scale(1.02) rotate(-.9deg)}66%{transform:scale(1.09) rotate(.6deg)}100%{transform:scale(1) rotate(0)}}')
sub('coreGlow membesar', r'\.lface \.coreGlow\{position:absolute;left:50%;top:50%;width:36px;height:36px;margin:-18px 0 0 -18px;border-radius:50%;\n  background:radial-gradient\(circle,rgba\(130,238,255,\.5\) 0%,rgba\(34,211,238,\.24\) 45%,transparent 70%\);\n  animation:breath 3\.6s ease-in-out infinite\}',
    '.lface .coreGlow{position:absolute;left:50%;top:50%;width:36px;height:36px;margin:-18px 0 0 -18px;border-radius:50%;\n'
    '  background:radial-gradient(circle,rgba(130,238,255,.5) 0%,rgba(34,211,238,.24) 45%,transparent 70%);\n'
    '  animation:breath 3.6s ease-in-out infinite;\n'
    '  transition:width .5s,height .5s,margin .5s}\n'
    '/* (v49) saat mendengarkan kolam plasma MELMEBAR 36->52px — jelas mata orb membuka */\n'
    '.orbBig.hear .lface .coreGlow{width:52px;height:52px;margin:-26px 0 0 -26px}')

# ---------- 7. EQ lebih besar & terang ----------
sub('eq besar', r'\.lface \.eq\{position:absolute;left:50%;bottom:8px;width:40px;height:13px;margin-left:-20px;z-index:2;',
    '.lface .eq{position:absolute;left:50%;bottom:5px;width:52px;height:20px;margin-left:-26px;z-index:2;')
sub('eq bar tebal', r'\.lface \.eq u\{width:3px;height:30%;border-radius:2px;\n  background:linear-gradient\(180deg,#EAFBFF,rgba\(34,211,238,\.8\)\);\n  box-shadow:0 0 5px rgba\(34,211,238,\.55\);',
    '.lface .eq u{width:4px;height:30%;border-radius:2px;\n'
    '  background:linear-gradient(180deg,#EAFBFF,rgba(34,211,238,.85));\n'
    '  box-shadow:0 0 7px rgba(34,211,238,.7);')
sub('eq nth tebal', r'\.lface \.eq u:nth-child\(2\)\{width:2\.6px;animation-duration:\.85s;animation-delay:\.12s\}\n\.lface \.eq u:nth-child\(3\)\{width:3\.4px;animation-duration:1\.35s;animation-delay:\.24s\}\n\.lface \.eq u:nth-child\(4\)\{animation-duration:\.95s;animation-delay:\.36s;background:linear-gradient\(180deg,#FFF4DA,rgba\(245,181,74,\.85\)\);box-shadow:0 0 5px rgba\(245,181,74,\.5\)\}\n\.lface \.eq u:nth-child\(5\)\{width:3\.4px;animation-duration:1\.5s;animation-delay:\.48s\}\n\.lface \.eq u:nth-child\(6\)\{width:2\.6px;animation-duration:\.9s;animation-delay:\.6s\}',
    '.lface .eq u:nth-child(2){width:3.4px;animation-duration:.85s;animation-delay:.12s}\n'
    '.lface .eq u:nth-child(3){width:4.4px;animation-duration:1.35s;animation-delay:.24s}\n'
    '.lface .eq u:nth-child(4){animation-duration:.95s;animation-delay:.36s;background:linear-gradient(180deg,#FFF4DA,rgba(245,181,74,.9));box-shadow:0 0 7px rgba(245,181,74,.65)}\n'
    '.lface .eq u:nth-child(5){width:4.4px;animation-duration:1.5s;animation-delay:.48s}\n'
    '.lface .eq u:nth-child(6){width:3.4px;animation-duration:.9s;animation-delay:.6s}')

# ---------- 8. JS: wake flash besar saat mulai ----------
sub('js wordPulse big', r"function wordPulse\(\)\{if\(!wpEl\)return;wpEl\.classList\.remove\('go'\);void wpEl\.offsetWidth;wpEl\.classList\.add\('go'\)\}",
    "function wordPulse(big){if(!wpEl)return;wpEl.classList.remove('go','big');void wpEl.offsetWidth;if(big)wpEl.classList.add('big');wpEl.classList.add('go')}")
sub('js start big', r"    hw&&hw\.classList\.add\('hIn'\);\n    wordPulse\(\);",
    "    hw&&hw.classList.add('hIn');\n    wordPulse(true);")

# ---------- 9. komentar blok ----------
sub('komentar v48', r'/\* \(v48\) DENGAR ORGANIK — umpan Pak: animasi dengar masih kaku\.\n   Prinsip tetap: TIDAK ada animation-duration animasi lama diubah \(tak pernah restart = nyambung\);\n   kehidupan kini BERLAPIS: goyangan badan \(panggung\) \+ inti bernapas miring \+ gelombang sonar\n   lembut buram \+ glow bernapas \+ EQ multi-bump \+ denyut reaktif tiap kata terdengar \(wpulse\) \*/',
    '/* (v49) DENGAR = DRAMATIS — umpan Pak: "dari tadi komplain gak ada perubahan".\n'
    '   Akar masalah: state idle sudah ramai animasi -> perubahan halus tak terlihat.\n'
    '   Kini saat mendengarkan: orb MEMBESAR 16% + terangkat, LEDAKAN cahaya saat diklik,\n'
    '   gelumbang sonar terang besar, glow kuat bernapas, inti berdebar, plasma melmebar,\n'
    '   HUD power-up, EQ besar. Prinsip tetap: animation-duration animasi lama tak disentuh */')

# ---------- 10. tema siang: ikut dramatis ----------
sub('siang sonar kuat', r'html\[data-theme="siang"\] \.orbBig \.ear i\{background:radial-gradient\(circle,transparent 50%,rgba\(9,125,150,\.16\) 63%,rgba\(12,110,136,\.38\) 72%,transparent 83%\)\}\nhtml\[data-theme="siang"\] \.orbBig \.ear i:nth-child\(2\)\{background:radial-gradient\(circle,transparent 50%,rgba\(176,110,10,\.12\) 63%,rgba\(176,110,10,\.3\) 72%,transparent 83%\)\}\nhtml\[data-theme="siang"\] \.orbBig \.hearGlow::after\{background:radial-gradient\(circle,rgba\(9,125,150,\.15\) 0%,rgba\(9,125,150,\.06\) 48%,transparent 72%\)\}\nhtml\[data-theme="siang"\] \.orbBig \.wpulse\{background:radial-gradient\(circle,transparent 52%,rgba\(12,110,136,\.32\) 68%,rgba\(9,125,150,\.14\) 74%,transparent 82%\)\}',
    'html[data-theme="siang"] .orbBig .ear i{background:radial-gradient(circle,transparent 46%,rgba(9,125,150,.32) 60%,rgba(12,110,136,.62) 70%,transparent 80%)}\n'
    'html[data-theme="siang"] .orbBig .ear i:nth-child(2){background:radial-gradient(circle,transparent 46%,rgba(176,110,10,.26) 60%,rgba(176,110,10,.55) 70%,transparent 80%)}\n'
    'html[data-theme="siang"] .orbBig .hearGlow::after{background:radial-gradient(circle,rgba(9,125,150,.24) 0%,rgba(9,125,150,.1) 48%,transparent 72%)}\n'
    'html[data-theme="siang"] .orbBig .wpulse{background:radial-gradient(circle,transparent 52%,rgba(12,110,136,.42) 66%,rgba(9,125,150,.2) 74%,transparent 82%)}\n'
    'html[data-theme="siang"] .orbBig .wpulse::before{background:radial-gradient(circle,rgba(210,245,252,.9) 0%,rgba(12,110,136,.3) 55%,transparent 75%)}')
sub('siang lring kuat', r'html\[data-theme="siang"\] \.orbBig\.hear \.lring\{border-color:rgba\(9,125,150,\.85\);box-shadow:0 0 18px rgba\(9,125,150,\.5\),inset 0 0 16px rgba\(9,125,150,\.3\)\}',
    'html[data-theme="siang"] .orbBig.hear .lring{border-color:#0A7A96;box-shadow:0 0 24px rgba(9,125,150,.7),0 0 52px rgba(9,125,150,.3),inset 0 0 18px rgba(9,125,150,.4)}')

io.open(P, 'w', encoding='utf-8').write(s)

print('=== SEMUA OPERASI OK ===')
for n, k in ops:
    print(f'  ok  {n}')

leftovers = {
    'hIn lama': 'scale(1.045)',
    'earD lama': 'earD 3.6s',
    'glowD lama': 'brightness(1.28)',
    'hearBreath lama': 'scale(1.048)',
    'coreGlow statis': 'margin:-18px 0 0 -18px;border-radius:50%;\n  background',
    'v48 note': 'REGALIA v48',
    'wordPulse lama': 'function wordPulse(){',
}
bersih = True
for label, pat in leftovers.items():
    if pat in s:
        print(f'  SISA!! {label}: {pat}')
        bersih = False
print('BERSIH ✓' if bersih else 'MASIH ADA SISA ✗')
