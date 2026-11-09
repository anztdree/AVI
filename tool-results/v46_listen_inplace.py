#!/usr/bin/env python3
# v46 — DENGAR DI TEMPAT: klik orb tidak pindah layer, orb hidup mendengar di panggungnya;
# inti orb diperkaya (anti-polos); tombol mic & overlay suara dibuang total.
import re, sys

p = '/home/z/my-project/public/avi-agent.html'
s = open(p, encoding='utf-8').read()
n0 = len(s)

def sub1(pat, rep, label, count=1):
    global s
    ns, k = re.subn(pat, rep, s, count=count)
    if k != count:
        print(f'FAIL: {label} (match={k})'); sys.exit(1)
    s = ns
    print(f'ok: {label}')

# ---------- 1) HAPUS CSS tombol mic ----------
sub1(r"#btnMic\{[\s\S]*?\}\n#btnMic:hover\{[\s\S]*?\}\n", "", "hapus css btnMic")

# ---------- 2) HAPUS CSS overlay suara (#liveov .. .soonTag) ----------
sub1(r"#liveov\{[\s\S]*?\.lvln small\{[^\n]*\}\n\.soonTag\{[^\n]*\}\n", "", "hapus css overlay suara")

# ---------- 3) HAPUS MARKUP tombol mic ----------
sub1(r'\n      <button class="cbtn" id="btnMic"[\s\S]*?</button>', "", "hapus markup btnMic")

# ---------- 4) HAPUS MARKUP overlay MODE SUARA ----------
sub1(r"\n  <!-- MODE SUARA -->\n  <div id=\"liveov\">[\s\S]*?\n  </div>\n", "\n", "hapus markup liveov")

# ---------- 5) JS: lvOpen/lvClose/lvBars -> DENGAR DI TEMPAT ----------
start = s.find("const lv=$('#liveov');let lvT=[];")
end_anchor = "$('#lvClose').addEventListener('click',lvClose);\n"
end = s.find(end_anchor)
if start < 0 or end < 0:
    print('FAIL: js voice block anchors'); sys.exit(1)
new_js = """/* (v46) DENGAR DI TEMPAT — klik orb TIDAK pindah layer: orb tetap di panggungnya,
   hanya "hidup" seperti mendengar (sonar + ekualiser + pindai inti). Ketuk lagi = tenang kembali.
   Tanpa mengubah animation-duration apa pun supaya animasi NYAMBUNG — hanya opacity/warna bertransisi */
const hearOrb=$('#orbBig');
function hearToggle(){hearOrb.classList.toggle('hear')}
hearOrb.addEventListener('click',hearToggle);
hearOrb.addEventListener('keydown',e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();hearToggle()}});
"""
s = s[:start] + new_js + s[end + len(end_anchor):]
print('ok: js dengar-di-tempat')

# ---------- 6) JS: kirim pesan menutup mode dengar ----------
sub1(r"i\.value='';\$\('#btnSend'\)\.classList\.remove\('rdy'\);",
     "i.value='';$('#btnSend').classList.remove('rdy');\n  {const ho=document.getElementById('orbBig');if(ho)ho.classList.remove('hear')}",
     "kirim menutup mode dengar")

# ---------- 7) MARKUP orbBig: role tombol + label baru ----------
sub1(r'<div class="orbBig" id="orbBig" title="Ketuk — bicara dengan AVI">',
     '<div class="orbBig" id="orbBig" role="button" tabindex="0" aria-label="Ketuk — AVI mendengarkan" title="Ketuk — AVI mendengarkan">',
     "orbBig role=button")

# ---------- 8) MARKUP: sonar + glow dengar (sebelum .halo) ----------
sub1(r'      </svg>\n      <div class="halo"></div>',
     '      </svg>\n      <div class="hearGlow"></div>\n      <div class="ear"><i></i><i></i></div>\n      <div class="halo"></div>',
     "markup sonar+glow")

# ---------- 9) MARKUP lface: coreGlow + heksagram + ekualiser ----------
sub1(r'<i class="lface"><i class="lscan"></i></i>',
     '<i class="lface"><i class="coreGlow"></i><i class="hexp"><i></i><i class="r"></i></i><i class="lscan"></i><i class="eq"><u></u><u></u><u></u><u></u><u></u></i></i>',
     "markup inti kaya")

# ---------- 10) CSS baru: dengar di tempat + inti kaya (sisip sebelum state KERJA) ----------
sub1(r"/\* — state KERJA: HUD ngebut, sapuan gelisah, inti lensa bekerja — \*/",
"""/* (v46) DENGAR DI TEMPAT: klik orb hanya hidup mendengar — tanpa pindah layer.
   Semua lewat transisi opacity/warna (BUKAN restart durasi) supaya animasi nyambung */
.orbBig .hearGlow{position:absolute;inset:-10px;border-radius:50%;opacity:0;transition:opacity .5s;pointer-events:none;
  background:radial-gradient(circle,rgba(34,211,238,.32) 0%,rgba(34,211,238,.12) 48%,transparent 72%)}
.orbBig .ear{position:absolute;inset:0;border-radius:50%;opacity:0;transition:opacity .5s;pointer-events:none;z-index:2}
.orbBig .ear i{position:absolute;inset:-2px;border-radius:50%;border:1.6px solid rgba(140,238,255,.6);
  box-shadow:0 0 14px rgba(34,211,238,.4);opacity:0;animation:earD 2.3s ease-out infinite}
.orbBig .ear i:nth-child(2){animation-delay:1.15s}
@keyframes earD{0%{transform:scale(.8);opacity:0}20%{opacity:.85}100%{transform:scale(1.66);opacity:0}}
.orbBig.hear .hearGlow,.orbBig.hear .ear{opacity:1}
/* inti lebih kaya (anti-polos): plasma bernapas + heksagram D1 ganda berputar pelan di wajah lensa */
.lface .coreGlow{position:absolute;left:50%;top:50%;width:36px;height:36px;margin:-18px 0 0 -18px;border-radius:50%;
  background:radial-gradient(circle,rgba(130,238,255,.5) 0%,rgba(34,211,238,.24) 45%,transparent 70%);
  animation:breath 3.6s ease-in-out infinite}
.lface .hexp{position:absolute;left:50%;top:50%;width:42px;height:42px;margin:-21px 0 0 -21px;opacity:.55;animation:hexpD 16s linear infinite}
.lface .hexp i{position:absolute;inset:0;
  background:linear-gradient(180deg,rgba(150,240,255,.7),rgba(34,211,238,.15));
  clip-path:polygon(50% 0,86% 100%,14% 100%)}
.lface .hexp i.r{transform:rotate(180deg);
  background:linear-gradient(180deg,rgba(245,181,74,.6),rgba(245,181,74,.1))}
@keyframes hexpD{from{transform:rotate(0)}to{transform:rotate(360deg)}}
/* ekualiser dengar: 5 batang mini di bawah reticle — hanya muncul saat .hear */
.lface .eq{position:absolute;left:50%;bottom:8px;width:32px;height:13px;margin-left:-16px;z-index:2;
  display:flex;align-items:flex-end;justify-content:space-between;opacity:0;transform:translateY(4px);
  transition:opacity .45s,transform .45s}
.lface .eq u{width:3.2px;height:30%;border-radius:2px;background:linear-gradient(180deg,#EAFBFF,rgba(34,211,238,.8));
  animation:eqD .9s ease-in-out infinite}
.lface .eq u:nth-child(2){animation-duration:.72s;animation-delay:.1s}
.lface .eq u:nth-child(3){animation-duration:1.05s;animation-delay:.2s}
.lface .eq u:nth-child(4){animation-duration:.8s;animation-delay:.32s}
.lface .eq u:nth-child(5){animation-duration:.95s;animation-delay:.42s}
@keyframes eqD{0%,100%{height:26%}50%{height:100%}}
.orbBig.hear .eq{opacity:1;transform:none}
.orbBig.hear .lring{border-color:rgba(190,247,255,.95);box-shadow:0 0 18px rgba(34,211,238,.8),inset 0 0 16px rgba(34,211,238,.45)}

/* — state KERJA: HUD ngebut, sapuan gelisah, inti lensa bekerja — */""",
"css dengar+inti kaya")

# ---------- 11) .lring base dapat transisi halus ----------
sub1(r"box-shadow:0 0 9px rgba\(34,211,238,\.4\),inset 0 0 9px rgba\(34,211,238,\.22\);\n  animation:breath 4\.4s ease-in-out infinite\}",
     "box-shadow:0 0 9px rgba(34,211,238,.4),inset 0 0 9px rgba(34,211,238,.22);\n  animation:breath 4.4s ease-in-out infinite;transition:border-color .5s,box-shadow .5s}",
     "lring transisi")

# ---------- 12) versi ----------
sub1(r"<title>AVI — Asisten Pribadi · MK-XXI REGALIA v45\.1</title>",
     "<title>AVI — Asisten Pribadi · MK-XXI REGALIA v46</title>", "title v46")
sub1(r"MK-XXI REGALIA v45\.1 · LATAR BERSIH-HIDUP \+ PANGGUNG ORB TANPA TEKS · MK-XIX v41 TETAP",
     "MK-XXI REGALIA v46 · DENGAR DI TEMPAT — ORB HIDUP MENDENGAR TANPA PINDAH LAYER · INTI DIPERKAYA · MIC DIHAPUS · MK-XIX v41 TETAP",
     "note v46")

open(p, 'w', encoding='utf-8').write(s)
print(f'size: {n0} -> {len(s)} ({n0-len(s)} dibuang)')
left = [w for w in ['liveov','lvOpen','lvClose','lvBars','btnMic','lvLog','lvState','lvorbig','soonTag','#wave'] if w in s]
print('sisa overlay suara:', left if left else 'BERSIH ✓')
