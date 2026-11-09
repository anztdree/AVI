#!/usr/bin/env python3
# v51 — TENANG & SIMPLE (umpan Pak: "Gelombang sonar terlalu ganggu, bikin simple aja!!
#       trus hologram naik turun itu juga malah jadi jelek!!")
# (1) Sonar: 3 gelombang gradian besar -> CUMA SATU cincin tipis melebar pelan
# (2) Goyang/naik-turun badan orb (hearSway + hearBreath + terangkat -8px) DIHAPUS total
# MK-XIX v41 bentuk tetap tidak disentuh.
import re, sys

P = '/home/z/my-project/public/avi-agent.html'
s = open(P, encoding='utf-8').read()
ops = []
def sub(name, old, new, n=1):
    global s
    s, c = re.subn(re.escape(old), lambda m: new, s, count=n)
    ops.append((name, c, n))

# 1) versi
sub('title', '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v50</title>',
    '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v51</title>')
sub('note',
    'MK-XXI REGALIA v50 · ORB-nya SENDIRI MENYALA HIJAU SAAT MENDENGAR (bukan cuma animasi pinggiran) · SUARA CUMA VIA ORB · MK-XIX v41 TETAP',
    'MK-XXI REGALIA v51 · SONAR SIMPLE SATU CINCIN + HOLOGRAM TEGAK DIAM (goyang & naik-turun dihapus) · ORB MENYALA HIJAU SAAT MENDENGAR · MK-XIX v41 TETAP')

# 2) catatan blok v49 diperbarui
sub('komentar-v49',
    '''/* (v49) DENGAR = DRAMATIS — umpan Pak: "dari tadi komplain gak ada perubahan".
   Akar masalah: state idle sudah ramai animasi -> perubahan halus tak terlihat.
   Kini saat mendengarkan: orb MEMBESAR 16% + terangkat, LEDAKAN cahaya saat diklik,
   gelumbang sonar terang besar, glow kuat bernapas, inti berdebar, plasma melmebar,
   HUD power-up, EQ besar. Prinsip tetap: animation-duration animasi lama tak disentuh */''',
    '''/* (v49) DENGAR = DRAMATIS — umpan Pak: "dari tadi komplain gak ada perubahan".
   Akar masalah: state idle sudah ramai animasi -> perubahan halus tak terlihat.
   Kini saat mendengarkan: orb MEMBESAR 16%, LEDAKAN cahaya saat diklik,
   glow kuat bernapas, plasma melmebar, HUD power-up, EQ besar.
   (v51) sonar diganti SATU cincin tipis; goyang & naik-turun badan DIHAPUS — tenang.
   Prinsip tetap: animation-duration animasi lama tak disentuh */''')

# 3) terangkat -8px DIHAPUS — hologram tidak lagi naik-turun, cukup membesar
sub('komentar-hin', '/* (v49) mendengarkan = orb MEMBESAR 16% & terangkat — perubahan tak mungkin tak terlihat */',
    '/* (v49/v51) mendengarkan = orb MEMBESAR 16% — terangkat dihapus, hologram tegak diem */')
sub('hin', '.heroWrap.hIn{transform:scale(1.16) translateY(-8px)}',
    '.heroWrap.hIn{transform:scale(1.16)}')

# 4) GOYANG & NAPAS BADAN DIHAPUS TOTAL (hearSway + hearBreath)
sub('sway-breath',
    '''/* orb "menyimak" v48: BADAN condong bergoyang pelan (panggung) + inti bernapas miring halus —
   banyak lapis irama beda periode (5.6s/3.8s/3.4s/3.6s) = tidak pernah terasa metronom */
.orbBig.hear .panggung3d{animation:hearSway 5.6s ease-in-out infinite;transform-origin:50% 86%}
@keyframes hearSway{0%{transform:rotate(0) translateY(0)}18%{transform:rotate(-1.5deg) translateY(-1.5px)}38%{transform:rotate(.9deg) translateY(.4px)}58%{transform:rotate(-1.1deg) translateY(-.8px)}78%{transform:rotate(.65deg) translateY(.2px)}100%{transform:rotate(0) translateY(0)}}
.orbBig.hear .coreBox{animation:hearBreath 3.8s ease-in-out infinite}
@keyframes hearBreath{0%{transform:scale(1) rotate(0)}24%{transform:scale(1.075) rotate(1.1deg)}44%{transform:scale(1.02) rotate(-.9deg)}66%{transform:scale(1.09) rotate(.6deg)}100%{transform:scale(1) rotate(0)}}''',
    '''/* (v51) goyang badan (hearSway) & napas inti (hearBreath) DIHAPUS — umpan Pak:
   hologram naik-turun jadi jelek. Saat mendengar orb TEGAP DIAM;
   perubahan state cukup lewat warna menyala hijau + membesar 16% */''')

# 5) SONAR SIMPLE: satu cincin tipis; dua gelombang gradian lain dihapus
sub('sonar',
    '''/* sonar hidup v48: 3 GELOMBANG gradian buram (tanpa garis keras) mengembang terus — organik */
.orbBig .ear{position:absolute;inset:0;border-radius:50%;opacity:0;transition:opacity .5s;pointer-events:none;z-index:2}
.orbBig .ear i{position:absolute;inset:-6px;border-radius:50%;opacity:0;will-change:transform,opacity;
  background:radial-gradient(circle,transparent 46%,rgba(110,231,183,.45) 60%,rgba(167,243,208,.88) 70%,transparent 80%);
  filter:blur(1.2px);
  animation:earD 2.6s cubic-bezier(.16,.6,.3,1) infinite}
.orbBig .ear i:nth-child(2){animation-delay:.87s;
  background:radial-gradient(circle,transparent 46%,rgba(245,181,74,.34) 60%,rgba(255,214,140,.78) 70%,transparent 80%)}
.orbBig .ear i:nth-child(3){animation-delay:1.74s;filter:blur(2px)}
@keyframes earD{0%{transform:scale(.82);opacity:0}10%{opacity:1}50%{opacity:.72}100%{transform:scale(2.3);opacity:0}}''',
    '''/* (v51) sonar DISEDERHANAKAN — umpan Pak: gelombang terlalu ganggu, bikin simple aja.
   Kini CUMA SATU cincin tipis melebar pelan lalu pudar; dua gelombang gradian lain DIHAPUS total.
   Jangkauan dipendekkan (1.62x vs 2.3x) & redup (puncak .55) = tidak menyilaukan */
.orbBig .ear{position:absolute;inset:0;border-radius:50%;opacity:0;transition:opacity .5s;pointer-events:none;z-index:2}
.orbBig .ear i{position:absolute;inset:-6px;border-radius:50%;opacity:0;will-change:transform,opacity;
  background:none;border:1.5px solid rgba(110,231,183,.5);box-shadow:0 0 7px rgba(52,211,153,.28);
  animation:earD 3.4s cubic-bezier(.25,.6,.35,1) infinite}
.orbBig .ear i:nth-child(2),.orbBig .ear i:nth-child(3){display:none}
@keyframes earD{0%{transform:scale(.86);opacity:.55}100%{transform:scale(1.62);opacity:0}}''')

# 6) ledakan awal diringkas (cincin lebih dekat & tenang)
sub('komentar-wp', '/* denyut per KATA (ring) + LEDAKAN saat mulai mendengarkan (go.big = kilat inti + cincin raksasa) */',
    '/* denyut per KATA (ring) + LEDAKAN pelan saat mulai mendengarkan (go.big — v51 diringkas) */')
sub('wpD2', '@keyframes wpD2{0%{opacity:1;transform:scale(.82)}55%{opacity:.5}100%{opacity:0;transform:scale(1.95)}}',
    '@keyframes wpD2{0%{opacity:.85;transform:scale(.82)}100%{opacity:0;transform:scale(1.5)}}')

# 7) tema siang: ikut gaya cincin tipis; rule gelombang emas dihapus
sub('s-ear',
    'html[data-theme="siang"] .orbBig .ear i{background:radial-gradient(circle,transparent 46%,rgba(5,150,105,.34) 60%,rgba(6,125,95,.66) 70%,transparent 80%)}',
    'html[data-theme="siang"] .orbBig .ear i{border-color:rgba(5,150,105,.5);box-shadow:0 0 6px rgba(5,150,105,.3)}')
sub('s-ear-gold',
    'html[data-theme="siang"] .orbBig .ear i:nth-child(2){background:radial-gradient(circle,transparent 46%,rgba(176,110,10,.26) 60%,rgba(176,110,10,.55) 70%,transparent 80%)}\n',
    '')

open(P, 'w', encoding='utf-8').write(s)

bad = 0
print('== HASIL ==')
for name, c, n in ops:
    ok = (c == n)
    print(f'{name:14s} match={c} {"OK" if ok else "GAGAL!!"}')
    if not ok: bad += 1

checks = {
  'sisa hearSway': len(re.findall(r'hearSway', s)),
  'sisa hearBreath': len(re.findall(r'hearBreath', s)),
  'sisa translateY(-8px)': len(re.findall(r'translateY\(-8px\)', s)),
  'sisa gelombang emas sonar': len(re.findall(r'rgba\(245,181,74,\.34\)', s)),
  'cincin tipis baru': len(re.findall(r'border:1\.5px solid rgba\(110,231,183,\.5\)', s)),
  'earD baru 1.62x': len(re.findall(r'scale\(1\.62\)', s)),
  'ear 2&3 display none': len(re.findall(r'\.ear i:nth-child\(2\),\.orbBig \.ear i:nth-child\(3\)\{display:none\}', s)),
  'hIn scale polos': len(re.findall(r'\.heroWrap\.hIn\{transform:scale\(1\.16\)\}', s)),
  'v51 markers': len(re.findall(r'v51', s)),
  'MK-XIX v41 tetap': len(re.findall(r'MK-XIX v41', s)),
  'hexagram clip utuh': len(re.findall(r'clip-path:polygon\(50% 0,86% 100%,14% 100%\)', s)),
  'menyala hijau tetap': len(re.findall(r'brightness\(2\.5\) saturate\(1\.75\)', s)),
}
for k, v in checks.items(): print(f'{k:26s}: {v}')
sys.exit(0 if bad == 0 else 1)
