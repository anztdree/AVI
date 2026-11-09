#!/usr/bin/env python3
# v47 — SUARA 100%: input teks dihapus, bar bawah = hasil suara yang didengar sistem;
# animasi dengar orb dibuat organik (anti kaku): sonar 3 cincin beruntun, inti menyimak,
# pindai cepat, ekualiser 7 batang, orb condong menyimak.
import re, sys

p = '/home/z/my-project/public/avi-agent.html'
s = open(p, encoding='utf-8').read()
n0 = len(s)

def sub1(pat, rep, label):
    global s
    ns, k = re.subn(pat, rep, s, count=1)
    if k != 1:
        print(f'FAIL: {label} (match={k})'); sys.exit(1)
    s = ns
    print(f'ok: {label}')

# ---------- 1) MARKUP komposer: input+send -> BAR HASIL SUARA ----------
sub1(r'  <!-- komposer: kapsul kaca — TANPA LAMPIRAN -->\n  <div class="composer">\n    <div class="cmd">\n      <input id="cin"[^>]*/>\n      <button class="cbtn send" id="btnSend"[\s\S]*?</button>\n    </div>',
"""  <!-- komposer (v47): BAR HASIL SUARA — input teks dihapus, input 100% lewat suara orb -->
  <div class="composer">
    <div class="cmd cmdVoice" id="voiceBar" aria-live="polite">
      <i class="vmic" aria-hidden="true"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="9" y="2.5" width="6" height="11.5" rx="3"/><path d="M5.5 11a6.5 6.5 0 0 0 13 0M12 17.5V21M9 21h6"/></svg></i>
      <div class="vtext"><span id="voiceText">Ketuk orb — AVI mendengar lewat suara, Pak</span><i class="vcar"></i></div>
    </div>""",
"markup bar hasil suara")

# ---------- 2) CSS komposer: buang input/cbtn/send, ganti gaya bar suara ----------
sub1(r"\.cmd:focus-within\{[^\n]*\}\n\.cmd:focus-within::before\{[^\n]*\}\n\.cmd input\{[^\n]*\}\n\.cmd input::placeholder\{[^\n]*\}\n\.cmd \.cbtn\{[\s\S]*?\.cmd \.send\.rdy:hover\{[^\n]*\}\n",
""".cmdVoice{display:flex;align-items:center;gap:11px;padding:7px 14px 7px 8px;cursor:pointer}
.vmic{width:36px;height:36px;border-radius:50%;flex:none;display:flex;align-items:center;justify-content:center;
  border:1px solid rgba(45,217,245,.32);color:var(--cyan);
  background:linear-gradient(160deg,rgba(45,217,245,.14),rgba(45,217,245,.03));transition:border-color .4s,box-shadow .4s}
.vtext{flex:1;min-width:0;display:flex;align-items:center;font:500 13.5px var(--ui);color:var(--txt);
  white-space:nowrap;overflow:hidden;gap:2px}
.vtext span{overflow:hidden;text-overflow:ellipsis}
.vcar{width:2px;height:15px;flex:none;border-radius:1px;background:var(--cyan);opacity:0;transition:opacity .3s}
.cmdVoice.listening{border-color:rgba(45,217,245,.55)}
.cmdVoice.listening .vmic{border-color:rgba(45,217,245,.8);box-shadow:0 0 18px -2px rgba(45,217,245,.55);animation:vmicD 1.4s ease-in-out infinite}
.cmdVoice.listening .vcar{opacity:1;animation:vcarD 1s steps(2,end) infinite}
.cmdVoice.listening .vtext{color:#D9F7FF}
@keyframes vmicD{0%,100%{transform:scale(1)}50%{transform:scale(1.09)}}
@keyframes vcarD{0%,100%{opacity:.9}50%{opacity:.12}}
""",
"css bar hasil suara")

# ---------- 3) MARKUP orb: sonar 3 cincin + lscanH + ekualiser 7 batang ----------
sub1(r'<div class="ear"><i></i><i></i></div>', '<div class="ear"><i></i><i></i><i></i></div>', "sonar 3 cincin")
sub1(r'<i class="lscan"></i><i class="eq">', '<i class="lscan"></i><i class="lscanH"></i><i class="eq">', "lscanH markup")
sub1(r'<i class="eq"><u></u><u></u><u></u><u></u><u></u></i>',
     '<i class="eq"><u></u><u></u><u></u><u></u><u></u><u></u><u></u></i>', "eq 7 batang")

# ---------- 4) CSS v46 -> v47: blok dengar diganti versi organik ----------
sub1(r"/\* \(v46\) DENGAR DI TEMPAT: klik orb hanya hidup mendengar[\s\S]*?\.orbBig\.hear \.lring\{[^\n]*\}\n",
"""/* (v47) DENGAR LEBIH HIDUP + BAR HASIL SUARA (umpan Pak: masih kaku; input teks dihapus — 100% suara).
   Prinsip: TIDAK ada animation-duration diubah saat toggle (animasi tak pernah restart = tetap nyambung);
   kehidupan datang dari transisi filter/opacity/warna + elemen sonar & ekualiser yang selalu beranimasi */
#orbDock .heroWrap{transition:transform .6s cubic-bezier(.22,1,.36,1)}
.heroWrap.hIn{transform:scale(1.045) translateY(-2px)}
.orbBig .hearGlow{position:absolute;inset:-10px;border-radius:50%;opacity:0;transition:opacity .5s;pointer-events:none;
  background:radial-gradient(circle,rgba(34,211,238,.32) 0%,rgba(34,211,238,.12) 48%,transparent 72%)}
.orbBig.hear .hearGlow{opacity:1}
/* sonar hidup: 3 cincin mengembang beruntun tanpa henti, fase sian-emas berselang — bukan denyut kaku */
.orbBig .ear{position:absolute;inset:0;border-radius:50%;opacity:0;transition:opacity .5s;pointer-events:none;z-index:2}
.orbBig .ear i{position:absolute;inset:-2px;border-radius:50%;opacity:0;filter:blur(.4px);will-change:transform,opacity;
  border:1.5px solid rgba(140,238,255,.55);
  box-shadow:0 0 16px rgba(34,211,238,.35),inset 0 0 10px rgba(34,211,238,.15);
  animation:earD 2.9s cubic-bezier(.2,.55,.35,1) infinite}
.orbBig .ear i:nth-child(2){animation-delay:.97s;border-color:rgba(245,181,74,.42);box-shadow:0 0 14px rgba(245,181,74,.32)}
.orbBig .ear i:nth-child(3){animation-delay:1.94s;border-width:1px;border-color:rgba(190,247,255,.4)}
@keyframes earD{0%{transform:scale(.72);opacity:0}12%{opacity:.95}55%{opacity:.6}100%{transform:scale(1.8);opacity:0}}
.orbBig.hear .ear{opacity:1}
/* orb "menyimak": inti bernapas pelan + lensa & busur menguat lewat filter (tanpa restart animasi) */
.orbBig.hear .coreBox{animation:hearBreath 2.4s ease-in-out infinite}
@keyframes hearBreath{0%,100%{transform:scale(1)}50%{transform:scale(1.032)}}
.ldisk,.mhalo,.k1,.k2,.hdata{transition:filter .6s}
.orbBig.hear .ldisk{filter:brightness(1.18) saturate(1.12)}
.orbBig.hear .mhalo{filter:brightness(1.45)}
.orbBig.hear .k1{filter:drop-shadow(0 0 8px rgba(34,211,238,.85)) drop-shadow(0 0 20px rgba(34,211,238,.35))}
.orbBig.hear .k2{filter:drop-shadow(0 0 7px rgba(245,181,74,.6))}
.orbBig.hear .hdata{filter:brightness(1.5)}
.orbBig.hear .lring{border-color:rgba(190,247,255,.95);box-shadow:0 0 18px rgba(34,211,238,.8),inset 0 0 16px rgba(34,211,238,.45)}
/* inti kaya (anti-polos): plasma bernapas + heksagram D1 ganda berputar pelan di wajah lensa */
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
/* pindai cepat khas "sedang mendengar" — hanya tampil saat .hear */
.lface .lscanH{position:absolute;left:2px;right:2px;top:0;height:7px;margin-top:-3.5px;border-radius:3px;mix-blend-mode:screen;
  background:linear-gradient(180deg,transparent,rgba(160,244,255,.3) 40%,rgba(255,255,255,.55) 50%,rgba(160,244,255,.3) 60%,transparent);
  opacity:0;animation:lscanH 1.7s ease-in-out infinite;display:none}
.orbBig.hear .lscanH{display:block}
@keyframes lscanH{0%{opacity:0;transform:translateY(4px)}30%{opacity:.9}80%{opacity:.5}100%{opacity:0;transform:translateY(52px)}}
/* ekualiser dengar: 7 batang bergelombang berjalan sian-emas berselang */
.lface .eq{position:absolute;left:50%;bottom:8px;width:40px;height:13px;margin-left:-20px;z-index:2;
  display:flex;align-items:flex-end;justify-content:space-between;opacity:0;transform:translateY(4px);
  transition:opacity .45s,transform .45s}
.orbBig.hear .eq{opacity:1;transform:none}
.lface .eq u{width:3px;height:30%;border-radius:2px;
  background:linear-gradient(180deg,#EAFBFF,rgba(34,211,238,.8));
  box-shadow:0 0 5px rgba(34,211,238,.55);
  animation:eqD 1.1s ease-in-out infinite}
.lface .eq u:nth-child(2){width:2.6px;animation-duration:.85s;animation-delay:.12s}
.lface .eq u:nth-child(3){width:3.4px;animation-duration:1.35s;animation-delay:.24s}
.lface .eq u:nth-child(4){animation-duration:.95s;animation-delay:.36s;background:linear-gradient(180deg,#FFF4DA,rgba(245,181,74,.85));box-shadow:0 0 5px rgba(245,181,74,.5)}
.lface .eq u:nth-child(5){width:3.4px;animation-duration:1.5s;animation-delay:.48s}
.lface .eq u:nth-child(6){width:2.6px;animation-duration:.9s;animation-delay:.6s}
.lface .eq u:nth-child(7){animation-duration:1.2s;animation-delay:.72s}
@keyframes eqD{0%,100%{height:24%}50%{height:100%}}
""",
"css dengar organik v47")

# ---------- 5) JS: blok v46 -> sistem suara penuh ----------
sub1(r"/\* \(v46\) DENGAR DI TEMPAT — klik orb TIDAK pindah layer[\s\S]*?hearOrb\.addEventListener\('keydown'[^\n]*\n",
"""/* (v47) SUARA 100% — ketuk orb → AVI mendengar → kalimat muncul KATA DEMI KATA di bar bawah
   → selesai otomatis masuk obrolan sebagai perintah (berat = membangunkan agent). Tanpa pindah layer */
const VOICE_HINT='Ketuk orb — AVI mendengar lewat suara, Pak';
const VOICE_DEMOS=[
  'AVI, ingatkan saya bayar listrik Jumat pagi.',
  'Buatkan laporan penjualan minggu ini, dirangkas.',
  'AVI, kabari saya kalau paket dari luar sudah sampai.',
  'Riset harga tiket ke Bali, carikan yang termurah.'
];
let vdIdx=0,vdTimers=[];
const voiceBar=$('#voiceBar'),voiceText=$('#voiceText');
function voiceReset(){vdTimers.forEach(clearTimeout);vdTimers=[];voiceBar.classList.remove('listening');voiceText.textContent=VOICE_HINT}
function hearToggle(){
  const ob=$('#orbBig'),hw=document.querySelector('.heroWrap');
  if(ob.classList.toggle('hear')){
    hw&&hw.classList.add('hIn');
    voiceBar.classList.add('listening');
    voiceText.textContent='';
    const words=VOICE_DEMOS[vdIdx++%VOICE_DEMOS.length].split(' ');
    let t=380;
    words.forEach((w,i)=>{vdTimers.push(setTimeout(()=>{voiceText.textContent+=(i?' ':'')+w},t));t+=360+Math.min(w.length*26,430)});
    vdTimers.push(setTimeout(()=>finishVoice(false),t+950));
  }else{
    finishVoice(true);
  }
}
function finishVoice(aborted){
  vdTimers.forEach(clearTimeout);vdTimers=[];
  const said=voiceText.textContent.trim();
  const ob=$('#orbBig');ob.classList.remove('hear');
  const hw=document.querySelector('.heroWrap');hw&&hw.classList.remove('hIn');
  voiceBar.classList.remove('listening');
  if(!said||said===VOICE_HINT||(aborted&&said.split(' ').length<3)){voiceText.textContent=VOICE_HINT;return}
  voiceText.textContent='Saya urus, Pak — hasilnya menyusul di obrolan.';
  vdTimers.push(setTimeout(voiceReset,2600));
  meBubble(said);
  const heavy=/(laporan|rekap|buatkan|bikin|ringkas|riset|analisis|pdf|draf|email)/i.test(said);
  if(heavy&&!taskActive){startTask(said)}
  else if(taskActive){aviReply('Masuk antre setelah tugas yang jalan, Pak — biar rapi.')}
  else{
    aviReply(REPLIES[repIdx++%REPLIES.length]);
    setTimeout(()=>{if(!taskActive)slotIdle()},6800);
  }
}
$('#orbBig').addEventListener('click',hearToggle);
$('#orbBig').addEventListener('keydown',e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();hearToggle()}});
voiceBar.addEventListener('click',hearToggle);
""",
"js sistem suara")

# ---------- 6) JS: buang handleSend + binding lama, sisakan REPLIES ----------
sub1(r"function handleSend\(\)\{[\s\S]*?\n\}\n", "", "hapus handleSend")
sub1(r"\$\('#btnSend'\)\.addEventListener\('click',handleSend\);\n\$\('#cin'\)\.addEventListener\('keydown'[^\n]*\n\$\('#cin'\)\.addEventListener\('input'[^\n]*\n", "", "hapus binding kirim lama")
sub1(r"/\* ---- kirim: perintah[^\n]*\n",
     "/* ---- jawaban AVI ringan (dipakai alur suara v47) ---- */\n", "label komentar REPLIES")
sub1(r"/\* ---- papan ketik: --vvh \+ fokus = obrolan terkini ---- \*/\nconst cin=\$\('#cin'\);\ncin\.addEventListener\('focus'[^\n]*\n",
     "/* ---- papan ketik: --vvh tetap dijaga (tanpa input teks, tidak ada fokus keyboard) ---- */\n",
     "hapus cin focus")

# ---------- 7) versi ----------
sub1(r"<title>AVI — Asisten Pribadi · MK-XXI REGALIA v46</title>",
     "<title>AVI — Asisten Pribadi · MK-XXI REGALIA v47</title>", "title v47")
sub1(r"MK-XXI REGALIA v46 · DENGAR DI TEMPAT — ORB HIDUP MENDENGAR TANPA PINDAH LAYER · INTI DIPERKAYA · MIC DIHAPUS · MK-XIX v41 TETAP",
     "MK-XXI REGALIA v47 · SUARA 100% — BAR BAWAH = HASIL SUARA SISTEM · DENGAR LEBIH HIDUP · MK-XIX v41 TETAP",
     "note v47")

open(p, 'w', encoding='utf-8').write(s)
print(f'size: {n0} -> {len(s)} ({n0-len(s)} dibuang)')
left = [w for w in ['id="cin"','btnSend','handleSend','.cmd input','cbtn'] if w in s]
print('sisa input teks:', left if left else 'BERSIH ✓')
