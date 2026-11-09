# -*- coding: utf-8 -*-
# v48: SUARA HANYA VIA ORB + ANIMASI DENGAR ORGANIK MULTI-LAPIS
# Umpan Pak: (1) papan ketik/bar bawah TIDAK BOLEH bisa diklik utk aktifkan suara — cuma orb;
#            (2) animasi orb mode mendengarkan masih kaku -> dibuat hidup: goyangan badan,
#                inti bernapas miring, gelombang sonar lembut buram, glow bernapas, EQ multi-bump,
#                + denyut reaktif SETIAP KATA yang terdengar (wpulse).
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

# ---------- 1. versi ----------
sub('title', r'<title>AVI — Asisten Pribadi · MK-XXI REGALIA v47</title>',
    '<title>AVI — Asisten Pribadi · MK-XXI REGALIA v48</title>')

sub('note', r'<div class="note">MK-XXI REGALIA v47 · SUARA 100% — BAR BAWAH = HASIL SUARA SISTEM · DENGAR LEBIH HIDUP · MK-XIX v41 TETAP</div>',
    '<div class="note">MK-XXI REGALIA v48 · SUARA HANYA DARI ORB — BAR BAWAH CUMA CERMIN HASIL SUARA · ORB GOYANG ORGANIK SAAT MENDENGAR · MK-XIX v41 TETAP</div>')

# ---------- 2. bar bawah: DISPLAY SAJA (tidak bisa mengaktifkan suara) ----------
sub('cmdVoice cursor', r'\.cmdVoice\{display:flex;align-items:center;gap:11px;padding:7px 14px 7px 8px;cursor:pointer\}',
    '/* (v48) BAR BUKAN TOMBOL — umpan Pak: papan ketik gak boleh disentuh utk suara; saklar suara CUMA orb */\n'
    '.cmdVoice{display:flex;align-items:center;gap:11px;padding:7px 14px 7px 8px;cursor:default}')

sub('hint markup', r'<div class="vtext"><span id="voiceText">Ketuk orb — AVI mendengar lewat suara, Pak</span><i class="vcar"></i></div>',
    '<div class="vtext"><span id="voiceText">Hasil suara tampil di sini — ketuk ORB untuk bicara, Pak</span><i class="vcar"></i></div>')

sub('hint js', r"const VOICE_HINT='Ketuk orb — AVI mendengar lewat suara, Pak';",
    "const VOICE_HINT='Hasil suara tampil di sini — ketuk ORB untuk bicara, Pak';")

sub('hapus klik bar', r"\nvoiceBar\.addEventListener\('click',hearToggle\);",
    '\n/* (v48) klik bar DIHAPUS — bar cuma cermin hasil suara; satu-satunya saklar suara = ORB */')

# ---------- 3. animasi dengar: organik multi-lapis ----------
# 3a. header komentar blok
sub('komentar blok v47', r'/\* \(v47\) DENGAR LEBIH HIDUP \+ BAR HASIL SUARA \(umpan Pak: masih kaku; input teks dihapus — 100% suara\)\.\n   Prinsip: TIDAK ada animation-duration diubah saat toggle \(animasi tak pernah restart = tetap nyambung\);\n   kehidupan datang dari transisi filter/opacity/warna \+ elemen sonar &amp; ekualiser yang selalu beranimasi \*/'
    .replace('&amp;','&'),
    '/* (v48) DENGAR ORGANIK — umpan Pak: animasi dengar masih kaku.\n'
    '   Prinsip tetap: TIDAK ada animation-duration animasi lama diubah (tak pernah restart = nyambung);\n'
    '   kehidupan kini BERLAPIS: goyangan badan (panggung) + inti bernapas miring + gelombang sonar\n'
    '   lembut buram + glow bernapas + EQ multi-bump + denyut reaktif tiap kata terdengar (wpulse) */')

# 3b. hearGlow: parent tetap fade-in, ::after bernapas (scale+brightness) — glow hidup, bukan statis
sub('hearGlow napas', r'\.orbBig \.hearGlow\{position:absolute;inset:-10px;border-radius:50%;opacity:0;transition:opacity \.5s;pointer-events:none;\n  background:radial-gradient\(circle,rgba\(34,211,238,\.32\) 0%,rgba\(34,211,238,\.12\) 48%,transparent 72%\)\}\n\.orbBig\.hear \.hearGlow\{opacity:1\}',
    '.orbBig .hearGlow{position:absolute;inset:-10px;border-radius:50%;opacity:0;transition:opacity .5s;pointer-events:none}\n'
    '.orbBig .hearGlow::after{content:\'\';position:absolute;inset:0;border-radius:50%;\n'
    '  background:radial-gradient(circle,rgba(34,211,238,.32) 0%,rgba(34,211,238,.12) 48%,transparent 72%)}\n'
    '.orbBig.hear .hearGlow{opacity:1}\n'
    '/* glow dengar BERNAPAS — bukan nyala statis kaku */\n'
    '.orbBig.hear .hearGlow::after{animation:hearGlowD 3.4s ease-in-out infinite}\n'
    '@keyframes hearGlowD{0%,100%{transform:scale(1);filter:brightness(1)}50%{transform:scale(1.09);filter:brightness(1.28)}}')

# 3c. sonar: cincin garis keras -> GELOMBANG gradian buram yang mengembang lembut
sub('sonar lembut', r'/\* sonar hidup: 3 cincin mengembang beruntun tanpa henti, fase sian-emas berselang — bukan denyut kaku \*/\n\.orbBig \.ear\{position:absolute;inset:0;border-radius:50%;opacity:0;transition:opacity \.5s;pointer-events:none;z-index:2\}\n\.orbBig \.ear i\{position:absolute;inset:-2px;border-radius:50%;opacity:0;filter:blur\(\.4px\);will-change:transform,opacity;\n  border:1\.5px solid rgba\(140,238,255,\.55\);\n  box-shadow:0 0 16px rgba\(34,211,238,\.35\),inset 0 0 10px rgba\(34,211,238,\.15\);\n  animation:earD 2\.9s cubic-bezier\(\.2,\.55,\.35,1\) infinite\}\n\.orbBig \.ear i:nth-child\(2\)\{animation-delay:\.97s;border-color:rgba\(245,181,74,\.42\);box-shadow:0 0 14px rgba\(245,181,74,\.32\)\}\n\.orbBig \.ear i:nth-child\(3\)\{animation-delay:1\.94s;border-width:1px;border-color:rgba\(190,247,255,\.4\)\}\n@keyframes earD\{0%\{transform:scale\(\.72\);opacity:0\}12%\{opacity:\.95\}55%\{opacity:\.6\}100%\{transform:scale\(1\.8\);opacity:0\}\}',
    '/* sonar hidup v48: 3 GELOMBANG gradian buram (tanpa garis keras) mengembang terus — organik */\n'
    '.orbBig .ear{position:absolute;inset:0;border-radius:50%;opacity:0;transition:opacity .5s;pointer-events:none;z-index:2}\n'
    '.orbBig .ear i{position:absolute;inset:-2px;border-radius:50%;opacity:0;will-change:transform,opacity;\n'
    '  background:radial-gradient(circle,transparent 50%,rgba(140,238,255,.2) 63%,rgba(190,247,255,.46) 72%,transparent 83%);\n'
    '  filter:blur(1.7px);\n'
    '  animation:earD 3.6s cubic-bezier(.18,.6,.32,1) infinite}\n'
    '.orbBig .ear i:nth-child(2){animation-delay:1.2s;\n'
    '  background:radial-gradient(circle,transparent 50%,rgba(245,181,74,.16) 63%,rgba(255,214,140,.4) 72%,transparent 83%)}\n'
    '.orbBig .ear i:nth-child(3){animation-delay:2.4s;filter:blur(2.4px)}\n'
    '@keyframes earD{0%{transform:scale(.8);opacity:0}14%{opacity:.85}52%{opacity:.5}78%{opacity:.2}100%{transform:scale(2.12);opacity:0}}\n'
    '/* denyut reaktif: tiap KATA yang terdengar menyentak satu gelombang halus dari orb (JS) */\n'
    '.orbBig .wpulse{position:absolute;inset:-2px;border-radius:50%;opacity:0;pointer-events:none;z-index:2;\n'
    '  background:radial-gradient(circle,transparent 52%,rgba(190,247,255,.42) 68%,rgba(255,255,255,.18) 74%,transparent 82%);\n'
    '  filter:blur(1px);transform:scale(.88)}\n'
    '.orbBig .wpulse.go{animation:wpD .72s cubic-bezier(.2,.7,.3,1) 1}\n'
    '@keyframes wpD{0%{opacity:.9;transform:scale(.88)}100%{opacity:0;transform:scale(1.6)}}')

# 3d. inti: napas kaku 2-fase -> napas organik miring + BADAN ORB bergoyang menyimak (panggung3d)
sub('inti organik + goyang badan', r'/\* orb "menyimak": inti bernapas pelan \+ lensa &amp; busur menguat lewat filter \(tanpa restart animasi\) \*/\n\.orbBig\.hear \.coreBox\{animation:hearBreath 2\.4s ease-in-out infinite\}\n@keyframes hearBreath\{0%,100%\{transform:scale\(1\)\}50%\{transform:scale\(1\.032\)\}\}'
    .replace('&amp;','&'),
    '/* orb "menyimak" v48: BADAN condong bergoyang pelan (panggung) + inti bernapas miring halus —\n'
    '   banyak lapis irama beda periode (5.6s/3.8s/3.4s/3.6s) = tidak pernah terasa metronom */\n'
    '.orbBig.hear .panggung3d{animation:hearSway 5.6s ease-in-out infinite;transform-origin:50% 86%}\n'
    '@keyframes hearSway{0%{transform:rotate(0) translateY(0)}18%{transform:rotate(-1.5deg) translateY(-1.5px)}38%{transform:rotate(.9deg) translateY(.4px)}58%{transform:rotate(-1.1deg) translateY(-.8px)}78%{transform:rotate(.65deg) translateY(.2px)}100%{transform:rotate(0) translateY(0)}}\n'
    '.orbBig.hear .coreBox{animation:hearBreath 3.8s ease-in-out infinite}\n'
    '@keyframes hearBreath{0%{transform:scale(1) rotate(0)}26%{transform:scale(1.04) rotate(.9deg)}47%{transform:scale(1.01) rotate(-.7deg)}68%{transform:scale(1.048) rotate(.4deg)}100%{transform:scale(1) rotate(0)}}')

# 3e. EQ: gelombang multi-bump (bukan naik-turun metronom)
sub('eq multi-bump', r'@keyframes eqD\{0%,100%\{height:24%\}50%\{height:100%\}\}',
    '@keyframes eqD{0%{height:26%}30%{height:94%}55%{height:36%}78%{height:100%}100%{height:26%}}')

# ---------- 4. markup: elemen wpulse ----------
sub('markup wpulse', r'<div class="ear"><i></i><i></i><i></i></div>',
    '<div class="ear"><i></i><i></i><i></i></div>\n      <div class="wpulse"></div>')

# ---------- 5. JS: denyut per kata + wake pulse ----------
sub('js wordPulse', r"const voiceBar=\$\('#voiceBar'\),voiceText=\$\('#voiceText'\);",
    "const voiceBar=$('#voiceBar'),voiceText=$('#voiceText');\n"
    "const wpEl=document.querySelector('.wpulse');\n"
    "/* satu gelombang halus menyentak dari orb tiap kata terdengar — orb terlihat BENAR-BENAR mendengar */\n"
    "function wordPulse(){if(!wpEl)return;wpEl.classList.remove('go');void wpEl.offsetWidth;wpEl.classList.add('go')}")

sub('js hear start pulse', r"  if\(ob\.classList\.toggle\('hear'\)\)\{\n    hw&&hw\.classList\.add\('hIn'\);\n    voiceBar\.classList\.add\('listening'\);",
    "  if(ob.classList.toggle('hear')){\n    hw&&hw.classList.add('hIn');\n    wordPulse();\n    voiceBar.classList.add('listening');")

sub('js kata->pulse', r"words\.forEach\(\(w,i\)=>\{vdTimers\.push\(setTimeout\(\(\)=>\{voiceText\.textContent\+=\(i\?' ':''\)\+w\},t\)\);t\+=360\+Math\.min\(w\.length\*26,430\)\}\);",
    "words.forEach((w,i)=>{vdTimers.push(setTimeout(()=>{voiceText.textContent+=(i?' ':'')+w;wordPulse()},t));t+=360+Math.min(w.length*26,430)});")

# ---------- 6. komentar alur ----------
sub('komentar mode suara', r'/\* \(v47\) SUARA 100% — ketuk orb → AVI mendengar → kalimat muncul KATA DEMI KATA di bar bawah\n   → selesai otomatis masuk obrolan sebagai perintah \(berat = membangunkan agent\)\. Tanpa pindah layer \*/',
    '/* (v48) SUARA 100% VIA ORB SAJA — bar bawah cuma cermin hasil suara (tak bisa diklik).\n'
    '   ketuk orb → AVI mendengar → kalimat muncul KATA DEMI KATA + orb berdenyut tiap kata\n'
    '   → selesai otomatis masuk obrolan sebagai perintah (berat = membangunkan agent). Tanpa pindah layer */')

io.open(P, 'w', encoding='utf-8').write(s)

print('=== SEMUA OPERASI OK ===')
for n, k in ops:
    print(f'  ok  {n}')

# sanity: tidak boleh ada sisa pola lama
leftovers = {
    'klik bar masih ada': "voiceBar.addEventListener('click'",
    'cursor pointer bar': '.cmdVoice{display:flex;align-items:center;gap:11px;padding:7px 14px 7px 8px;cursor:pointer}',
    'hint lama': 'Ketuk orb — AVI mendengar lewat suara',
    'earD lama 2.9s': 'earD 2.9s',
    'hearBreath lama': 'hearBreath{0%,100%{transform:scale(1)}50%{transform:scale(1.032)}}',
    'eqD lama': 'eqD{0%,100%{height:24%}50%{height:100%}}',
    'v47 title': 'REGALIA v47',
}
bersih = True
for label, pat in leftovers.items():
    if pat in s:
        print(f'  SISA!! {label}: {pat}')
        bersih = False
print('BERSIH ✓' if bersih else 'MASIH ADA SISA ✗')
