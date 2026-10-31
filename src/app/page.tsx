import {
  BarChart3,
  BatteryFull,
  Brain,
  Check,
  ChevronRight,
  Code2,
  Download,
  ExternalLink,
  FileDown,
  FileText,
  FolderOpen,
  Globe,
  History,
  Loader2,
  Mic,
  Paperclip,
  Pause,
  Pencil,
  Plus,
  ScanSearch,
  Send,
  Settings,
  Share2,
  ShieldCheck,
  Signal,
  Square,
  Wifi,
  X,
  Bell,
  CalendarClock,
  FastForward,
  Gauge,
  GitBranch,
  MessageSquare,
  Play,
  Plug,
  Sparkles,
  Terminal,
  Users,
  Video,
} from "lucide-react";

/* ============================================================
   PAPAN KONSULTASI DESAIN — AVI MODE AGENT v1.4.0
   Bukan implementasi. Semua layar = mockup visual saja.
   Pola diambil dari produk agen nyata (lihat bagian Referensi).
   ============================================================ */

/* ---------- primitif kecil ---------- */

function HudLabel({ children }: { children: React.ReactNode }) {
  return (
    <div className="font-hud text-[8px] tracking-[0.3em] text-[#67E8F9]/90">
      {children}
    </div>
  );
}

function SourceTag({ children }: { children: React.ReactNode }) {
  return (
    <span className="font-hud text-[9px] tracking-wider text-[#67E8F9] border border-[#22D3EE]/25 bg-[#22D3EE]/5 rounded-full px-2.5 py-1">
      POLA · {children}
    </span>
  );
}

function StatusBadge({
  tone,
  pulse,
  children,
}: {
  tone: "gold" | "green" | "red" | "cyan";
  pulse?: boolean;
  children: React.ReactNode;
}) {
  const map = {
    gold: "text-[#F5B54A] border-[#F5B54A]/40 bg-[#F5B54A]/10",
    green: "text-[#4ADE80] border-[#4ADE80]/40 bg-[#4ADE80]/10",
    red: "text-[#F87171] border-[#F87171]/40 bg-[#F87171]/10",
    cyan: "text-[#22D3EE] border-[#22D3EE]/40 bg-[#22D3EE]/10",
  } as const;
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full border px-2 py-0.5 font-hud text-[8px] tracking-[0.2em] ${map[tone]}`}
    >
      {pulse && (
        <span
          className={`h-1.5 w-1.5 rounded-full avi-pulse-dot ${
            tone === "gold"
              ? "bg-[#F5B54A]"
              : tone === "green"
                ? "bg-[#4ADE80]"
                : tone === "red"
                  ? "bg-[#F87171]"
                  : "bg-[#22D3EE]"
          }`}
        />
      )}
      {children}
    </span>
  );
}

function DeleteMark({ label }: { label: string }) {
  return (
    <div className="mx-4 rounded-lg border border-dashed border-[#F87171]/50 bg-[#F87171]/5 px-2 py-1.5 text-center">
      <span className="font-hud text-[8px] tracking-[0.22em] text-[#F87171]">
        ✕ {label} — DIHAPUS
      </span>
    </div>
  );
}

function NewTag({ label = "BARU" }: { label?: string }) {
  return (
    <span className="inline-flex items-center gap-1 rounded-full border border-[#F5B54A]/50 bg-[#F5B54A]/15 px-2 py-0.5 font-hud text-[8px] tracking-[0.25em] text-[#F5B54A]">
      <Sparkles className="h-2.5 w-2.5" /> {label}
    </span>
  );
}

function StatusBar() {
  return (
    <div className="relative z-20 flex items-center justify-between px-5 pb-1 pt-3">
      <span className="font-hud text-[9px] tracking-[0.35em] text-[#8A97A8]">
        AVI·OS
      </span>
      <div className="flex items-center gap-1.5 text-[#8A97A8]">
        <Signal className="h-3 w-3" />
        <Wifi className="h-3 w-3" />
        <BatteryFull className="h-3.5 w-3.5" />
      </div>
    </div>
  );
}

function AviOrb({ size = 56 }: { size?: number }) {
  return (
    <div className="relative" style={{ width: size, height: size }}>
      <div
        className="avi-spin absolute inset-0 rounded-full border border-[#22D3EE]/50"
        style={{ borderTopColor: "transparent", borderRightColor: "transparent" }}
      />
      <div
        className="absolute inset-[15%] rounded-full border border-[#F5B54A]/40"
        style={{ borderBottomColor: "transparent", borderLeftColor: "transparent", transform: "rotate(45deg)" }}
      />
      <div className="absolute inset-[30%] rounded-full border border-[#22D3EE]/60 bg-[#22D3EE]/10" />
      <div className="avi-breathe absolute inset-[43%] rounded-full bg-[#7DF3FF] shadow-[0_0_16px_5px_rgba(125,243,255,0.5)]" />
    </div>
  );
}

/* ---------- primitif Jarvis (dari ReactorOrb.kt & HudFrame.kt) ---------- */

function Corners({
  inset = 8,
  arm = 14,
  tone = "cyan",
}: {
  inset?: number;
  arm?: number;
  tone?: "cyan" | "gold";
}) {
  const c = tone === "gold" ? "border-[#F5B54A]/60" : "border-[#22D3EE]/55";
  const dot = tone === "gold" ? "bg-[#F5B54A]" : "bg-[#67E8F9]";
  const base = "pointer-events-none absolute";
  return (
    <>
      <span
        className={`${base} border-l border-t ${c}`}
        style={{ left: inset, top: inset, width: arm, height: arm }}
      />
      <span
        className={`${base} border-r border-t ${c}`}
        style={{ right: inset, top: inset, width: arm, height: arm }}
      />
      <span
        className={`${base} border-l border-b ${c}`}
        style={{ left: inset, bottom: inset, width: arm, height: arm }}
      />
      <span
        className={`${base} border-r border-b ${c}`}
        style={{ right: inset, bottom: inset, width: arm, height: arm }}
      />
      {/* titik sudut emas khas antarmuka Stark */}
      <span className={`${base} h-1 w-1 rounded-full ${dot}`} style={{ left: inset - 1, top: inset - 1 }} />
      <span className={`${base} h-1 w-1 rounded-full ${dot}`} style={{ right: inset - 1, top: inset - 1 }} />
      <span className={`${base} h-1 w-1 rounded-full ${dot}`} style={{ left: inset - 1, bottom: inset - 1 }} />
      <span className={`${base} h-1 w-1 rounded-full ${dot}`} style={{ right: inset - 1, bottom: inset - 1 }} />
    </>
  );
}

function SideRails() {
  return (
    <>
      <span className="pointer-events-none absolute bottom-12 left-2 top-12 w-px bg-[#22D3EE]/10" />
      <span className="pointer-events-none absolute bottom-12 right-2 top-12 w-px bg-[#22D3EE]/10" />
      <span
        className="avi-rail-tick pointer-events-none absolute left-2 h-5 w-px bg-[#67E8F9]/80"
        style={{ boxShadow: "0 0 6px rgba(103,232,249,0.8)" }}
      />
      <span
        className="pointer-events-none absolute right-2 h-5 w-px bg-[#67E8F9]/80"
        style={{
          boxShadow: "0 0 6px rgba(103,232,249,0.8)",
          animation: "avi-rail 7.5s linear infinite",
          animationDelay: "3.75s",
        }}
      />
    </>
  );
}

function Telemetry({ state }: { state: string }) {
  return (
    <div className="relative z-20 flex items-center justify-between px-6 pb-2.5 font-hud text-[6.5px] tracking-[0.28em] text-[#8A97A8]/75">
      <span className="avi-flicker">{state}</span>
      <span>SUARA SIAGA</span>
    </div>
  );
}

/** Inti reaktor berlapis — IDLE berdenyut pelan, WORK cincin orbit cepat, DONE tenang.
 *  Bila diberi pct, busur progres mengelilingi inti: reaktor = progres tugas. */
function ReactorCore({
  state = "idle",
  size = 64,
  label,
  pct,
}: {
  state?: "idle" | "work" | "done";
  size?: number;
  label?: string;
  pct?: number;
}) {
  const ring = state === "done" ? "#4ADE80" : state === "work" ? "#F5B54A" : "#22D3EE";
  const r = 15;
  const circ = 2 * Math.PI * r;
  return (
    <div className="relative" style={{ width: size, height: size }}>
      <div
        className={`absolute inset-0 rounded-full border border-dashed ${
          state === "work" ? "avi-orbit-fast" : "avi-orbit"
        }`}
        style={{ borderColor: `${ring}66` }}
      />
      <div
        className="absolute inset-[10%] rounded-full border"
        style={{
          borderColor: "transparent",
          borderTopColor: `${ring}99`,
          borderLeftColor: `${ring}55`,
          transform: "rotate(-30deg)",
        }}
      />
      {typeof pct === "number" && (
        <svg viewBox="0 0 40 40" className="absolute inset-0 h-full w-full -rotate-90">
          <circle cx="20" cy="20" r={r} fill="none" stroke="rgba(138,151,168,0.22)" strokeWidth="2.4" />
          <circle
            cx="20"
            cy="20"
            r={r}
            fill="none"
            stroke={ring}
            strokeWidth="2.4"
            strokeLinecap="round"
            strokeDasharray={circ}
            strokeDashoffset={circ * (1 - pct)}
          />
        </svg>
      )}
      <div
        className="absolute inset-[30%] rounded-full border"
        style={{ borderColor: `${ring}88`, background: `${ring}18` }}
      />
      {!label && (
        <div
          className="avi-breathe absolute inset-[43%] rounded-full"
          style={{ background: ring, boxShadow: `0 0 14px 4px ${ring}88` }}
        />
      )}
      {label && (
        <div className="absolute inset-0 flex items-center justify-center font-hud text-[9px] text-[#E8EEF5]">
          {label}
        </div>
      )}
    </div>
  );
}

function Switch({ on }: { on: boolean }) {
  return (
    <div
      className={`relative h-4 w-8 shrink-0 rounded-full border ${
        on ? "border-[#22D3EE]/60 bg-[#22D3EE]/25" : "border-[#8A97A8]/40 bg-white/5"
      }`}
    >
      <div
        className={`absolute top-1/2 h-2.5 w-2.5 -translate-y-1/2 rounded-full ${
          on ? "right-0.5 bg-[#7DF3FF] shadow-[0_0_8px_rgba(125,243,255,0.7)]" : "left-0.5 bg-[#8A97A8]"
        }`}
      />
    </div>
  );
}

function MiniChart() {
  const bars = [38, 62, 45, 80, 58, 92];
  return (
    <div className="flex h-11 items-end gap-1">
      {bars.map((h, i) => (
        <div
          key={i}
          className="w-2.5 rounded-t-sm"
          style={{
            height: `${h}%`,
            background: i === bars.length - 1 ? "rgba(245,181,74,0.9)" : "rgba(34,211,238,0.55)",
          }}
        />
      ))}
    </div>
  );
}

/* ---------- bingkai ponsel ---------- */

function Phone({ children }: { children: React.ReactNode }) {
  return (
    <div className="relative w-[300px] shrink-0 rounded-[2.4rem] border border-[#22D3EE]/25 bg-[#0A0F18] p-2 shadow-[0_0_80px_-20px_rgba(34,211,238,0.5)] sm:w-[320px]">
      <div className="absolute left-1/2 top-2 z-30 h-5 w-24 -translate-x-1/2 rounded-b-2xl border-b border-[#22D3EE]/10 bg-[#0A0F18]" />
      <div className="avi-nebula relative aspect-[9/19] overflow-hidden rounded-[1.9rem]">
        <div className="avi-stars absolute inset-0 opacity-70" />
        <div className="avi-grid absolute inset-0 opacity-60" />
        <div className="avi-hex absolute inset-0 opacity-70" />
        <Corners />
        <SideRails />
        <div className="relative z-10 flex h-full flex-col">{children}</div>
        <div className="avi-scanline z-20" />
      </div>
    </div>
  );
}

function PhoneCard({
  no,
  label,
  desc,
  sources,
  notes,
  children,
}: {
  no: string;
  label: string;
  desc: string;
  sources: string[];
  notes: string[];
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col items-center gap-5 rounded-3xl border border-[#22D3EE]/10 bg-[#0A0F18]/60 p-6 sm:p-8">
      <div className="text-center">
        <div className="font-hud text-[10px] tracking-[0.45em] text-[#22D3EE]">{no}</div>
        <h3 className="mt-1 font-orbitron text-sm font-semibold tracking-[0.18em] text-[#E8EEF5]">
          {label}
        </h3>
        <p className="mx-auto mt-1.5 max-w-[300px] font-rajdhani text-[15px] font-medium leading-snug text-[#8A97A8]">
          {desc}
        </p>
      </div>
      {children}
      <div className="flex flex-wrap justify-center gap-1.5">
        {sources.map((s) => (
          <SourceTag key={s}>{s}</SourceTag>
        ))}
      </div>
      <ul className="w-full max-w-[320px] space-y-1.5 border-t border-[#22D3EE]/10 pt-4">
        {notes.map((n) => (
          <li key={n} className="flex gap-2 font-rajdhani text-[13px] leading-snug text-[#8A97A8]">
            <span className="mt-0.5 text-[#F5B54A]">▸</span>
            <span>{n}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

/* ---------- layar 1 : OBROLAN (dibersihkan) ---------- */

function ScreenObrolan() {
  return (
    <Phone>
      <StatusBar />
      <div className="flex items-center justify-between px-5 pt-1">
        <div className="flex items-center gap-2">
          <span className="font-orbitron text-[13px] font-bold tracking-[0.35em] text-[#E8EEF5]">
            AVI
          </span>
          <span className="rounded-full border border-[#22D3EE]/40 bg-[#22D3EE]/10 px-1.5 py-0.5 font-hud text-[8px] tracking-[0.2em] text-[#67E8F9]">
            MK1
          </span>
        </div>
        <Settings className="h-3.5 w-3.5 text-[#8A97A8]" />
      </div>

      <div className="mt-2.5">
        <DeleteMark label="TELEMETRI 'JAM' DI ORBIT" />
      </div>

      <div className="flex-1 space-y-2.5 overflow-hidden px-4 py-3">
        <div className="max-w-[85%] rounded-xl rounded-tl-sm border bg-[#0C2833]/70 px-3 py-2" style={{ borderColor: "rgba(27,94,112,0.9)" }}>
          <div className="font-hud text-[7px] tracking-[0.3em] text-[#67E8F9]">AVI</div>
          <p className="mt-0.5 font-rajdhani text-[11px] leading-snug text-[#E8EEF5]">
            Selamat malam, Pak. Reaktor stabil, semua sistem normal.
          </p>
        </div>
        <div className="ml-auto max-w-[85%] rounded-xl rounded-tr-sm border bg-[#251D0E]/80 px-3 py-2" style={{ borderColor: "rgba(138,106,47,0.9)" }}>
          <div className="text-right font-hud text-[7px] tracking-[0.3em] text-[#FFD98A]">PAK</div>
          <p className="mt-0.5 font-rajdhani text-[11px] leading-snug text-[#E8EEF5]">
            Kerjakan laporan penjualan Q3 jadi PDF. Besok pagi harus siap.
          </p>
        </div>
        <div className="max-w-[92%] rounded-xl rounded-tl-sm border bg-[#0C2833]/70 px-3 py-2" style={{ borderColor: "rgba(27,94,112,0.9)" }}>
          <div className="font-hud text-[7px] tracking-[0.3em] text-[#67E8F9]">AVI</div>
          <p className="mt-0.5 font-rajdhani text-[11px] leading-snug text-[#E8EEF5]">
            Siap, Pak. Itu tugas berat — saya susun rencana dan kerjakan sampai tuntas?
          </p>
          <button className="mt-2 flex w-full items-center justify-center gap-1.5 rounded-lg border border-[#F5B54A]/50 bg-[#F5B54A]/10 px-2 py-1.5">
            <Brain className="h-3 w-3 text-[#F5B54A]" />
            <span className="font-hud text-[8px] tracking-[0.22em] text-[#F5B54A]">
              AJUKAN SEBAGAI TUGAS AGENT
            </span>
          </button>
        </div>
      </div>

      <div className="mb-2">
        <DeleteMark label="CHIP SARAN INSTAN" />
      </div>

      <div className="flex items-center gap-2 px-4 pb-5">
        <div className="flex flex-1 items-center rounded-full border border-[#22D3EE]/25 bg-[#0D141F]/90 px-3.5 py-2.5">
          <span className="font-rajdhani text-[11px] text-[#8A97A8]">Tanya AVI…</span>
        </div>
        <div className="relative flex h-10 w-10 items-center justify-center rounded-full border border-[#22D3EE]/50 bg-[#22D3EE]/10">
          <div className="avi-pulse-dot absolute inset-0 rounded-full border border-[#22D3EE]/30" />
          <div className="avi-orbit absolute -inset-1 rounded-full border border-dashed border-[#22D3EE]/25" />
          <Mic className="h-4 w-4 text-[#67E8F9]" />
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#F5B54A]/15">
          <Send className="h-4 w-4 text-[#F5B54A]" />
        </div>
      </div>
      <Telemetry state="REAKTOR: STABIL" />
    </Phone>
  );
}

/* ---------- layar 2 : AJUKAN TUGAS ---------- */

function ScreenAjukan() {
  return (
    <Phone>
      <StatusBar />
      <div className="flex flex-col items-center px-5 pt-2">
        <div className="avi-float">
          <ReactorCore state="idle" size={50} />
        </div>
        <div className="mt-2 font-orbitron text-[11px] font-bold tracking-[0.4em] text-[#E8EEF5]">
          MODE AGENT
        </div>
        <p className="mt-1 text-center font-rajdhani text-[11px] font-medium text-[#8A97A8]">
          Tugas berat? Saya yang kerjakan sampai tuntas, Pak.
        </p>
      </div>

      <div className="mt-3 space-y-3 px-4">
        <div className="relative rounded-xl border border-[#22D3EE]/20 bg-[#0D141F]/85 p-3">
          <Corners inset={5} arm={9} />
          <HudLabel>JUDUL TUGAS</HudLabel>
          <div className="mt-1.5 rounded-lg border border-[#22D3EE]/20 bg-[#141E2E]/70 px-2.5 py-2 font-rajdhani text-[11px] text-[#E8EEF5]">
            Laporan Penjualan Q3 — PDF
          </div>
          <div className="mt-2.5">
            <HudLabel>INSTRUKSI</HudLabel>
            <div className="mt-1.5 rounded-lg border border-[#22D3EE]/20 bg-[#141E2E]/70 px-2.5 py-2 font-rajdhani text-[10px] leading-snug text-[#B9C4D2]">
              Kumpulkan data penjualan Juli–September, buat analisa tren, render PDF rapi 10+ halaman beserta grafik.
            </div>
          </div>
          <div className="mt-2.5 flex gap-2">
            <span className="flex items-center gap-1 rounded-full border border-[#22D3EE]/30 px-2 py-1 font-hud text-[8px] tracking-wider text-[#67E8F9]">
              <Paperclip className="h-2.5 w-2.5" /> FILE
            </span>
            <span className="flex items-center gap-1 rounded-full border border-[#22D3EE]/30 px-2 py-1 font-hud text-[8px] tracking-wider text-[#67E8F9]">
              <Plus className="h-2.5 w-2.5" /> FOTO
            </span>
          </div>
        </div>

        <div className="rounded-xl border border-[#22D3EE]/20 bg-[#0D141F]/85 p-3">
          <div className="flex items-center justify-between">
            <HudLabel>KEMAMPUAN BERAT</HudLabel>
            <span className="font-hud text-[7px] tracking-[0.2em] text-[#8A97A8]">OPSIONAL</span>
          </div>
          <div className="mt-2 grid grid-cols-2 gap-2">
            {[
              { icon: FileText, label: "Laporan PDF" },
              { icon: Code2, label: "Coding" },
              { icon: ScanSearch, label: "Analisa Kode" },
              { icon: BarChart3, label: "Analisa Data" },
            ].map(({ icon: Icon, label }) => (
              <div
                key={label}
                className="flex items-center gap-1.5 rounded-lg border border-[#22D3EE]/15 bg-[#141E2E]/60 px-2 py-2"
              >
                <Icon className="h-3 w-3 text-[#67E8F9]" />
                <span className="font-rajdhani text-[10px] font-semibold text-[#E8EEF5]">{label}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="rounded-xl border border-[#22D3EE]/15 bg-[#0D141F]/85 p-2.5">
          <div className="flex items-center justify-between">
            <HudLabel>ESTIMASI AWAL</HudLabel>
            <span className="font-hud text-[7px] tracking-[0.2em] text-[#F5B54A]">POLA · MANUS</span>
          </div>
          <div className="mt-1.5 flex items-center gap-2 font-hud text-[8.5px] tracking-[0.14em] text-[#67E8F9]">
            <Gauge className="h-3 w-3 text-[#F5B54A]" />
            <span>±12 LANGKAH · ±40K TOKEN · 3–5 MENIT</span>
          </div>
        </div>

        <div className="flex items-center gap-1.5 px-1">
          <Plug className="h-3 w-3 text-[#8A97A8]" />
          {["BERKAS", "KALENDER", "NOTIFIKASI"].map((c) => (
            <span
              key={c}
              className="rounded-full border border-[#22D3EE]/20 px-2 py-1 font-hud text-[7.5px] tracking-[0.18em] text-[#8A97A8]"
            >
              {c}
            </span>
          ))}
        </div>

        <button className="flex w-full items-center justify-center gap-2 rounded-xl border border-[#F5B54A]/60 bg-[#F5B54A]/15 px-3 py-3 shadow-[0_0_24px_-6px_rgba(245,181,74,0.5)]">
          <Brain className="h-3.5 w-3.5 text-[#F5B54A]" />
          <span className="font-orbitron text-[10px] font-bold tracking-[0.3em] text-[#F5B54A]">
            AJUKAN TUGAS
          </span>
        </button>
        <p className="pb-4 text-center font-rajdhani text-[9px] text-[#8A97A8]">
          Setelah diajukan, AVI menyusun rencana dulu — bukan langsung nekat.
        </p>
      </div>
      <Telemetry state="REAKTOR: SIAGA" />
    </Phone>
  );
}

/* ---------- layar 3 : RENCANA ---------- */

function ScreenRencana() {
  const steps = [
    { icon: FolderOpen, label: "Kumpulkan & bersihkan data penjualan" },
    { icon: BarChart3, label: "Analisa tren bulanan & margin" },
    { icon: Pencil, label: "Susun kerangka & narasi laporan" },
    { icon: BarChart3, label: "Buat grafik ringkasan" },
    { icon: FileDown, label: "Render PDF + QA akhir" },
  ];
  return (
    <Phone>
      <StatusBar />
      <div className="px-4 pt-2">
        <div className="relative rounded-xl border border-[#F5B54A]/30 bg-[#0D141F]/85 p-3">
          <Corners inset={5} arm={9} tone="gold" />
          <div className="flex items-center justify-between">
            <span className="font-hud text-[8px] tracking-[0.3em] text-[#8A97A8]">TUGAS #A-1042</span>
            <StatusBadge tone="gold" pulse>
              MENUNGGU PERSETUJUAN
            </StatusBadge>
          </div>
          <div className="mt-1.5 font-orbitron text-[11px] font-semibold tracking-wider text-[#E8EEF5]">
            LAPORAN PENJUALAN Q3 — PDF
          </div>
          <div className="mt-1 font-hud text-[8px] tracking-[0.2em] text-[#67E8F9]">
            ESTIMASI ± 4 MENIT · 5 LANGKAH
          </div>
        </div>
      </div>

      <div className="mt-3 flex-1 px-4">
        <div className="flex items-center justify-between">
          <HudLabel>RENCANA SAYA</HudLabel>
          <span className="flex items-center gap-1 font-hud text-[8px] tracking-wider text-[#8A97A8]">
            <Pencil className="h-2.5 w-2.5" /> BISA DIUBAH
          </span>
        </div>
        <div className="mt-2 space-y-1.5">
          {steps.map(({ icon: Icon, label }, i) => (
            <div
              key={label}
              className="flex items-center gap-2 rounded-lg border border-[#22D3EE]/15 bg-[#0D141F]/80 px-2.5 py-2"
            >
              <span className="font-hud text-[9px] text-[#67E8F9]">0{i + 1}</span>
              <Icon className="h-3 w-3 shrink-0 text-[#67E8F9]" />
              <span className="flex-1 font-rajdhani text-[10.5px] font-medium leading-tight text-[#E8EEF5]">
                {label}
              </span>
              <span className="h-2.5 w-2.5 shrink-0 rounded-full border border-[#8A97A8]/50" />
            </div>
          ))}
        </div>
      </div>

      <div className="space-y-2 px-4 pb-5">
        <button className="flex w-full items-center justify-center gap-2 rounded-xl border border-[#F5B54A]/60 bg-[#F5B54A]/15 px-3 py-2.5">
          <Check className="h-3.5 w-3.5 text-[#F5B54A]" />
          <span className="font-orbitron text-[9px] font-bold tracking-[0.28em] text-[#F5B54A]">
            SETUJUI &amp; JALANKAN
          </span>
        </button>
        <button className="w-full rounded-xl border border-[#22D3EE]/30 px-3 py-2 font-hud text-[9px] tracking-[0.28em] text-[#67E8F9]">
          MINTA UBAH RENCANA
        </button>
      </div>
      <Telemetry state="REAKTOR: MENYUSUN RENCANA" />
    </Phone>
  );
}

/* ---------- layar 4 : EKSEKUSI LIVE ---------- */

function ScreenEksekusi() {
  return (
    <Phone>
      <StatusBar />
      <div className="flex items-center justify-between px-4 pt-1.5">
        <div>
          <div className="font-orbitron text-[10px] font-semibold tracking-wider text-[#E8EEF5]">
            LAPORAN PENJUALAN Q3
          </div>
          <div className="mt-0.5 font-hud text-[8px] tracking-[0.22em] text-[#67E8F9]">
            EKSEKUSI LIVE · 02:14
          </div>
        </div>
        <ReactorCore state="work" pct={0.5} label="3/6" size={54} />
      </div>

      <div className="mx-4 mt-1.5 rounded-lg border border-[#22D3EE]/15 bg-[#05080D]/80 px-3 py-1.5 font-hud text-[8px] leading-relaxed tracking-wide text-[#67E8F9]/90">
        <div>› membuka data_penjualan.csv…</div>
        <div>› menghitung tren margin Q3…</div>
        <div className="avi-blink text-[#F5B54A]">▌</div>
      </div>

      <div className="mt-2 flex gap-1.5 px-4">
        <span className="flex flex-1 items-center justify-center gap-1 rounded-lg border border-[#22D3EE]/35 bg-[#22D3EE]/10 py-1.5">
          <Pause className="h-3 w-3 text-[#67E8F9]" />
          <span className="font-hud text-[8px] tracking-[0.2em] text-[#67E8F9]">JEDA</span>
        </span>
        <span className="flex flex-1 items-center justify-center gap-1 rounded-lg border border-[#F87171]/35 bg-[#F87171]/10 py-1.5">
          <Square className="h-3 w-3 text-[#F87171]" />
          <span className="font-hud text-[8px] tracking-[0.2em] text-[#F87171]">HENTIKAN</span>
        </span>
      </div>

      <div className="mt-3 flex-1 space-y-1 px-4">
        {[
          { state: "done", icon: FolderOpen, t: "Kumpulkan data penjualan", s: "3 file dibaca" },
          { state: "done", icon: BarChart3, t: "Analisa tren bulanan", s: "tren +23% September" },
          { state: "active", icon: Pencil, t: "Menulis analisa bab 2", s: "berpikir: merangkum tren margin…" },
          { state: "pending", icon: BarChart3, t: "Buat grafik ringkasan", s: null },
          { state: "pending", icon: FileDown, t: "Render PDF", s: null },
          { state: "pending", icon: Check, t: "QA akhir & simpan", s: null },
        ].map((st, i) => {
          const Icon = st.icon;
          const isDone = st.state === "done";
          const isActive = st.state === "active";
          return (
            <div key={i} className="flex gap-2">
              <div className="flex flex-col items-center pt-2">
                <span
                  className={`h-2 w-2 shrink-0 rounded-full ${
                    isDone
                      ? "bg-[#4ADE80]"
                      : isActive
                        ? "avi-pulse-dot bg-[#22D3EE] shadow-[0_0_8px_rgba(34,211,238,0.9)]"
                        : "border border-[#8A97A8]/50"
                  }`}
                />
                {i < 5 && <span className="w-px flex-1 bg-[#22D3EE]/15" />}
              </div>
              <div
                className={`mb-1 flex-1 rounded-lg border px-2.5 py-1.5 ${
                  isActive ? "border-[#22D3EE]/40 bg-[#22D3EE]/8" : "border-transparent"
                }`}
              >
                <div className="flex items-center gap-1.5">
                  <Icon
                    className={`h-3 w-3 shrink-0 ${isDone ? "text-[#4ADE80]" : isActive ? "text-[#67E8F9]" : "text-[#8A97A8]"}`}
                  />
                  <span
                    className={`flex-1 font-rajdhani text-[10.5px] font-semibold leading-tight ${
                      isDone ? "text-[#8A97A8] line-through" : isActive ? "text-[#E8EEF5]" : "text-[#B9C4D2]"
                    }`}
                  >
                    {st.t}
                  </span>
                  {isDone && <Check className="h-3 w-3 text-[#4ADE80]" />}
                  {isActive && <Loader2 className="avi-spin h-3 w-3 text-[#67E8F9]" />}
                </div>
                {st.s && (
                  <div
                    className={`mt-0.5 pl-[18px] font-hud text-[8px] tracking-wide ${
                      isActive ? "text-[#67E8F9]/80 italic" : "text-[#8A97A8]/80"
                    }`}
                  >
                    {st.s}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div className="px-4 pb-5">
        <div className="overflow-hidden rounded-xl border border-[#22D3EE]/25 bg-[#0D141F]/95">
          <div className="flex items-center justify-between border-b border-[#22D3EE]/15 px-3 py-1.5">
            <HudLabel>RUANG KERJA AVI</HudLabel>
            <span className="flex items-center gap-1 font-hud text-[7px] tracking-[0.2em] text-[#4ADE80]">
              <span className="avi-pulse-dot h-1.5 w-1.5 rounded-full bg-[#4ADE80]" /> LIVE
            </span>
          </div>
          <div className="flex items-center gap-3 px-3 py-2">
            <div className="w-14 shrink-0 rounded-md border border-[#22D3EE]/20 bg-[#141E2E] p-1.5">
              <div className="h-1 w-3/4 rounded bg-[#67E8F9]/60" />
              <div className="mt-1 h-0.5 w-full rounded bg-[#8A97A8]/40" />
              <div className="mt-0.5 h-0.5 w-5/6 rounded bg-[#8A97A8]/40" />
              <div className="mt-1.5">
                <MiniChart />
              </div>
            </div>
            <div>
              <div className="font-rajdhani text-[10px] font-semibold text-[#E8EEF5]">
                Merender grafik bab 2
              </div>
              <div className="mt-0.5 font-hud text-[8px] tracking-[0.18em] text-[#67E8F9]">
                LANGKAH 3/6 · 02:14
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="mx-4 mb-2.5 space-y-1.5">
        <div className="flex items-center gap-2 rounded-full border border-[#22D3EE]/25 bg-[#0D141F]/90 px-3 py-1.5">
          <span className="flex-1 font-rajdhani text-[9.5px] text-[#8A97A8]">
            Sisip instruksi cepat — tanpa menghentikan tugas…
          </span>
          <Send className="h-3 w-3 text-[#67E8F9]" />
        </div>
        <div className="flex items-center gap-2 font-hud text-[7px] tracking-[0.2em] text-[#8A97A8]">
          <MessageSquare className="h-2.5 w-2.5 text-[#67E8F9]" />
          <span>/BTW — TANYA SAMPING, TUGAS TETAP JALAN</span>
          <span className="ml-auto text-[#F5B54A]">POLA · DEVIN</span>
        </div>
      </div>
      <Telemetry state="REAKTOR: MENGERJAKAN" />
    </Phone>
  );
}

/* ---------- layar 5 : HASIL & ARTEFAK ---------- */

function ScreenHasil() {
  return (
    <Phone>
      <StatusBar />
      <div className="flex items-center justify-between px-4 pt-2">
        <div>
          <div className="font-orbitron text-[10px] font-semibold tracking-wider text-[#E8EEF5]">
            LAPORAN PENJUALAN Q3
          </div>
          <div className="mt-0.5 font-hud text-[8px] tracking-[0.22em] text-[#4ADE80]">
            SELESAI · 4M 12D · 6/6 LANGKAH
          </div>
        </div>
        <div className="flex items-center gap-2">
          <ReactorCore state="done" size={34} />
          <StatusBadge tone="green" pulse>
            TUNTAS
          </StatusBadge>
        </div>
      </div>

      <div className="mt-2.5 flex items-center gap-1 overflow-hidden px-4">
        {["DATA", "TREN", "NARASI", "GRAFIK", "PDF", "QA"].map((s) => (
          <span key={s} className="flex items-center gap-1 font-hud text-[7.5px] tracking-[0.15em] text-[#4ADE80]">
            {s} <Check className="h-2.5 w-2.5" />
          </span>
        ))}
      </div>

      <div className="mt-3 flex-1 px-4">
        <div className="flex items-center justify-between">
          <HudLabel>ARTEFAK HASIL</HudLabel>
          <div className="flex items-center gap-1">
            {["v3", "v2", "v1"].map((v, i) => (
              <span
                key={v}
                className={`rounded-full border px-1.5 py-0.5 font-hud text-[7.5px] tracking-wider ${
                  i === 0
                    ? "border-[#F5B54A]/60 bg-[#F5B54A]/15 text-[#F5B54A]"
                    : "border-[#8A97A8]/30 text-[#8A97A8]"
                }`}
              >
                {v}
              </span>
            ))}
          </div>
        </div>
        <div className="mt-2 rounded-xl border border-[#F5B54A]/35 bg-[#0D141F]/90 p-3 shadow-[0_0_30px_-10px_rgba(245,181,74,0.4)]">
          <div className="flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg border border-[#F5B54A]/40 bg-[#F5B54A]/10">
              <FileText className="h-4 w-4 text-[#F5B54A]" />
            </div>
            <div className="min-w-0">
              <div className="truncate font-hud text-[9.5px] text-[#E8EEF5]">
                Laporan_Penjualan_Q3.pdf
              </div>
              <div className="font-hud text-[8px] tracking-[0.18em] text-[#8A97A8]">
                1,8 MB · 12 HALAMAN
              </div>
            </div>
          </div>
          <div className="mt-2.5 rounded-lg border border-[#22D3EE]/15 bg-[#141E2E] p-2.5">
            <div className="mx-auto h-1 w-2/3 rounded bg-[#67E8F9]/50" />
            <div className="mt-1.5 flex justify-center">
              <MiniChart />
            </div>
            <div className="mt-2 space-y-1">
              <div className="h-0.5 w-full rounded bg-[#8A97A8]/35" />
              <div className="h-0.5 w-5/6 rounded bg-[#8A97A8]/35" />
              <div className="h-0.5 w-4/6 rounded bg-[#8A97A8]/35" />
            </div>
          </div>
          <div className="mt-2.5 flex gap-1.5">
            <span className="flex flex-1 items-center justify-center gap-1 rounded-lg border border-[#F5B54A]/50 bg-[#F5B54A]/15 py-1.5">
              <Download className="h-3 w-3 text-[#F5B54A]" />
              <span className="font-hud text-[8px] tracking-[0.18em] text-[#F5B54A]">UNDUH</span>
            </span>
            <span className="flex flex-1 items-center justify-center gap-1 rounded-lg border border-[#22D3EE]/30 py-1.5">
              <Share2 className="h-3 w-3 text-[#67E8F9]" />
              <span className="font-hud text-[8px] tracking-[0.18em] text-[#67E8F9]">BAGIKAN</span>
            </span>
            <span className="flex flex-1 items-center justify-center gap-1 rounded-lg border border-[#22D3EE]/30 py-1.5">
              <FolderOpen className="h-3 w-3 text-[#67E8F9]" />
              <span className="font-hud text-[8px] tracking-[0.18em] text-[#67E8F9]">BUKA</span>
            </span>
          </div>
          <div className="mt-2 space-y-1 border-t border-[#22D3EE]/10 pt-2">
            <div className="flex items-center gap-1.5 font-hud text-[7.5px] tracking-[0.16em] text-[#8A97A8]">
              <Video className="h-3 w-3 text-[#4ADE80]" />
              <span className="text-[#4ADE80]">QA OTOMATIS LULUS</span>
              <span>· 12/12 HALAMAN TERVERIFIKASI</span>
            </div>
            <div className="flex items-center gap-1.5 font-hud text-[7.5px] tracking-[0.16em] text-[#8A97A8]">
              <Users className="h-3 w-3 text-[#67E8F9]" />
              <span>AKSES: HANYA PAK · DAPAT LIHAT</span>
              <span className="ml-auto text-[#F5B54A]">v3 TERBARU</span>
            </div>
          </div>
        </div>
      </div>

      <div className="px-4 pb-5">
        <div className="flex items-center gap-2 rounded-full border border-[#22D3EE]/25 bg-[#0D141F]/90 px-3.5 py-2.5">
          <span className="flex-1 font-rajdhani text-[10px] text-[#8A97A8]">
            Minta revisi… mis. “tambah bab stok”
          </span>
          <Send className="h-3.5 w-3.5 text-[#67E8F9]" />
        </div>
      </div>
      <Telemetry state="REAKTOR: TUNTAS · ARTEFAK SIAP" />
    </Phone>
  );
}

/* ---------- layar 5 : RUANG KERJA AVI (computer AVI) — BARU ---------- */

function ScreenRuangKerja() {
  const cmds = [
    { t: "14:02:11", c: "clean_csv penjualan_q3.csv", s: "OK · 1.204 baris", done: true },
    { t: "14:02:39", c: "hitung_tren --margin --bulanan", s: "OK · +23,4%", done: true },
    { t: "14:03:05", c: "render_grafik bab2", s: "berjalan…", done: true },
    { t: "▒▒:▒▒:▒▒", c: "susun_narasi laporan", s: "belum jalan", done: false },
  ];
  return (
    <Phone>
      <StatusBar />
      <div className="flex items-center justify-between px-4 pt-1.5">
        <div className="flex items-center gap-2">
          <Terminal className="h-3.5 w-3.5 text-[#67E8F9]" />
          <span className="font-orbitron text-[10px] font-bold tracking-[0.3em] text-[#E8EEF5]">
            RUANG KERJA AVI
          </span>
        </div>
        <span className="flex items-center gap-1 font-hud text-[7px] tracking-[0.2em] text-[#4ADE80]">
          <span className="avi-pulse-dot h-1.5 w-1.5 rounded-full bg-[#4ADE80]" /> LIVE
        </span>
      </div>

      <div className="mt-2 flex gap-1 px-4">
        {["SHELL", "BERKAS", "PRATINJAU"].map((t, i) => (
          <span
            key={t}
            className={`flex-1 rounded-t-md border px-1 py-1 text-center font-hud text-[7.5px] tracking-[0.18em] ${
              i === 0
                ? "border-[#22D3EE]/45 bg-[#22D3EE]/10 text-[#67E8F9]"
                : "border-[#22D3EE]/10 text-[#8A97A8]"
            }`}
          >
            {t}
          </span>
        ))}
      </div>

      <div className="mx-4 flex-1 rounded-b-lg border border-t-0 border-[#22D3EE]/20 bg-[#05080D]/85 p-2.5">
        <HudLabel>RIWAYAT PERINTAH · LOMPAT WAKTU</HudLabel>
        <div className="mt-1.5 space-y-1 font-hud text-[8px]">
          {cmds.map((c) => (
            <div
              key={c.c}
              className={`rounded border px-1.5 py-1 ${
                c.done
                  ? "border-[#22D3EE]/12 bg-[#0D141F]/70"
                  : "border-dashed border-[#8A97A8]/25 text-[#8A97A8]/50"
              }`}
            >
              <div className="flex justify-between gap-1">
                <span className="text-[#8A97A8]">{c.t}</span>
                <span className={c.done ? "text-[#4ADE80]" : ""}>{c.s}</span>
              </div>
              <div className={c.done ? "text-[#E8EEF5]" : ""}>$ {c.c}</div>
            </div>
          ))}
        </div>
        <div className="mt-2 rounded border border-dashed border-[#8A97A8]/25 px-1.5 py-1 font-hud text-[7px] tracking-[0.16em] text-[#8A97A8]/60">
          KETUK PERINTAH LAMA = LOMPAT KE MOMEN ITU
        </div>
        <div className="mt-2 flex items-center gap-1.5">
          {["CSV", "PNG", "MD"].map((f) => (
            <span
              key={f}
              className="rounded border border-[#22D3EE]/20 bg-[#0D141F] px-1.5 py-0.5 font-hud text-[7px] tracking-widest text-[#67E8F9]"
            >
              {f}
            </span>
          ))}
          <span className="ml-auto font-hud text-[7px] tracking-widest text-[#8A97A8]">3 BERKAS DIBUAT</span>
        </div>
      </div>

      <div className="mx-4 mt-2 rounded-lg border border-[#22D3EE]/20 bg-[#0D141F]/90 px-3 py-2">
        <div className="flex items-center gap-2">
          <Play className="h-3 w-3 text-[#F5B54A]" />
          <div className="relative h-1 flex-1 rounded-full bg-[#22D3EE]/15">
            <div className="absolute left-0 top-0 h-full w-2/3 rounded-full bg-[#22D3EE]/50" />
            <span className="absolute left-2/3 top-1/2 h-2 w-2 -translate-y-1/2 rounded-full bg-[#7DF3FF] shadow-[0_0_8px_rgba(125,243,255,0.9)]" />
          </div>
          <span className="flex items-center gap-1 font-hud text-[7px] tracking-[0.2em] text-[#F5B54A]">
            <FastForward className="h-2.5 w-2.5" /> KE LIVE
          </span>
        </div>
      </div>
      <Telemetry state="AVI MENGOPERASIKAN KOMPUTERNYA SENDIRI" />
    </Phone>
  );
}

/* ---------- layar 8 : IZIN & INTERVENSI — BARU ---------- */

function ScreenIzin() {
  return (
    <Phone>
      <StatusBar />
      <div className="flex-1 px-4 pt-3">
        <div className="relative rounded-xl border border-[#F5B54A]/40 bg-[#0D141F]/95 p-3.5 shadow-[0_0_40px_-10px_rgba(245,181,74,0.5)]">
          <Corners inset={5} arm={9} tone="gold" />
          <div className="flex items-center justify-between">
            <StatusBadge tone="gold" pulse>AVI MEMINTA IZIN</StatusBadge>
            <Bell className="h-3.5 w-3.5 text-[#F5B54A]" />
          </div>
          <div className="mt-2.5 font-hud text-[8px] tracking-[0.2em] text-[#8A97A8]">AKSI DIMINTA</div>
          <div className="mt-1 rounded-lg border border-[#22D3EE]/15 bg-[#05080D]/80 px-2.5 py-2 font-hud text-[8.5px] leading-relaxed text-[#E8EEF5]">
            baca_berkas_eksternal
            <div className="mt-0.5 text-[#67E8F9]">→ /Download/penjualan_q3.xlsx</div>
          </div>
          <p className="mt-2 font-rajdhani text-[10.5px] leading-snug text-[#B9C4D2]">
            Aksi ini di luar tingkat otoritas <span className="text-[#F5B54A]">BAKU</span>, Pak. Tanpa izin,
            langkah ini dilewati dan tugas menyesuaikan jalannya sendiri.
          </p>
          <div className="mt-3 space-y-1.5">
            <div className="w-full rounded-lg border border-[#22D3EE]/50 bg-[#22D3EE]/15 px-3 py-2 text-center font-hud text-[8.5px] tracking-[0.22em] text-[#67E8F9]">
              IZINKAN SEKALI
            </div>
            <div className="w-full rounded-lg border border-[#22D3EE]/25 px-3 py-2 text-center font-hud text-[8.5px] tracking-[0.22em] text-[#B9C4D2]">
              IZINKAN SELAMA TUGAS INI
            </div>
            <div className="w-full rounded-lg border border-[#F87171]/40 px-3 py-2 text-center font-hud text-[8.5px] tracking-[0.22em] text-[#F87171]">
              TOLAK
            </div>
          </div>
        </div>
        <div className="mt-2.5 flex items-center justify-between rounded-lg border border-[#22D3EE]/15 bg-[#0D141F]/70 px-3 py-1.5 font-hud text-[7.5px] tracking-[0.18em] text-[#8A97A8]">
          <span>ANTREAN: 1 PERMINTAAN LAIN</span>
          <span className="text-[#F5B54A]">POLA · CHATGPT AGENT + REPLIT</span>
        </div>
      </div>
      <Telemetry state="OTORITAS: TANGAN PAK" />
    </Phone>
  );
}

/* ---------- layar 7 : RIWAYAT TUGAS ---------- */

function ScreenRiwayat() {
  return (
    <Phone>
      <StatusBar />
      <div className="flex items-center justify-between px-5 pt-2">
        <div className="flex items-center gap-2">
          <History className="h-3.5 w-3.5 text-[#67E8F9]" />
          <span className="font-orbitron text-[11px] font-bold tracking-[0.3em] text-[#E8EEF5]">
            RIWAYAT TUGAS
          </span>
        </div>
        <ShieldCheck className="h-3.5 w-3.5 text-[#8A97A8]" />
      </div>

      <div className="mt-3 flex-1 space-y-3 overflow-hidden px-4">
        <div>
          <HudLabel>HARI INI</HudLabel>
          <div className="mt-1.5 space-y-1.5">
            {[
              {
                icon: FileText,
                t: "📄 Laporan Penjualan Q3 — PDF",
                badge: <StatusBadge tone="green">SELESAI</StatusBadge>,
                meta: "13.42 · PDF",
              },
              {
                icon: Code2,
                t: "🧩 Refactor Modul Login",
                badge: <StatusBadge tone="cyan" pulse>JALAN</StatusBadge>,
                meta: "11.20",
              },
            ].map((r) => {
              const Icon = r.icon;
              return (
                <div
                  key={r.t}
                  className="flex items-center gap-2 rounded-lg border border-[#22D3EE]/15 bg-[#0D141F]/80 px-2.5 py-2"
                >
                  <Icon className="h-3.5 w-3.5 shrink-0 text-[#67E8F9]" />
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-rajdhani text-[10.5px] font-semibold text-[#E8EEF5]">
                      {r.t}
                    </div>
                    <div className="font-hud text-[8px] tracking-[0.18em] text-[#8A97A8]">{r.meta}</div>
                  </div>
                  {r.badge}
                  <ChevronRight className="h-3 w-3 text-[#8A97A8]" />
                </div>
              );
            })}
          </div>
        </div>
        <div className="rounded-lg border border-[#F5B54A]/25 bg-[#0D141F]/70 px-2.5 py-2">
          <div className="flex items-center gap-2">
            <GitBranch className="h-3 w-3 text-[#F5B54A]" />
            <span className="font-hud text-[8px] tracking-[0.18em] text-[#F5B54A]">
              CHECKPOINT 3 · SEBELUM REVISI GRAFIK
            </span>
          </div>
          <div className="mt-1 flex items-center justify-between font-hud text-[7.5px] tracking-[0.16em] text-[#8A97A8]">
            <span>SESI Q3 BISA DIPULIHKAN KE TITIK INI</span>
            <span className="rounded border border-[#F5B54A]/45 px-1.5 py-0.5 text-[#F5B54A]">PULIHKAN</span>
          </div>
        </div>
        <div>
          <HudLabel>KEMARIN</HudLabel>
          <div className="mt-1.5 space-y-1.5">
            {[
              {
                icon: ScanSearch,
                t: "🔍 Audit Kode Pembayaran",
                badge: <StatusBadge tone="green">SELESAI</StatusBadge>,
                meta: "21.05",
              },
              {
                icon: BarChart3,
                t: "📊 Analisa Stok Gudang",
                badge: <StatusBadge tone="red">DIHENTIKAN</StatusBadge>,
                meta: "16.48",
              },
            ].map((r) => {
              const Icon = r.icon;
              return (
                <div
                  key={r.t}
                  className="flex items-center gap-2 rounded-lg border border-[#22D3EE]/10 bg-[#0D141F]/60 px-2.5 py-2"
                >
                  <Icon className="h-3.5 w-3.5 shrink-0 text-[#67E8F9]" />
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-rajdhani text-[10.5px] font-semibold text-[#B9C4D2]">
                      {r.t}
                    </div>
                    <div className="font-hud text-[8px] tracking-[0.18em] text-[#8A97A8]">{r.meta}</div>
                  </div>
                  {r.badge}
                  <ChevronRight className="h-3 w-3 text-[#8A97A8]" />
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <div className="mx-4 mb-2 flex items-center gap-2 rounded-lg border border-dashed border-[#22D3EE]/30 bg-[#0D141F]/60 px-2.5 py-2">
        <CalendarClock className="h-3.5 w-3.5 shrink-0 text-[#67E8F9]" />
        <div className="min-w-0 flex-1">
          <div className="font-rajdhani text-[10px] font-semibold text-[#E8EEF5]">
            Terjadwal · Laporan QC Mingguan
          </div>
          <div className="font-hud text-[7.5px] tracking-[0.16em] text-[#8A97A8]">
            OTOMATIS TIAP SENIN · OPSIONAL
          </div>
        </div>
        <Switch on={true} />
      </div>
      <div className="px-4 pb-5 text-center font-hud text-[8px] tracking-[0.22em] text-[#8A97A8]">
        KETUK UNTUK MEMBUKA ULANG ARTEFAK
      </div>
      <Telemetry state="ARSIP · REAKTOR STABIL" />
    </Phone>
  );
}

/* ---------- layar 7 : PUSAT KENDALI ---------- */

function ScreenKendali() {
  return (
    <Phone>
      <StatusBar />
      <div className="flex items-center gap-2 px-5 pt-2">
        <ShieldCheck className="h-3.5 w-3.5 text-[#67E8F9]" />
        <span className="font-orbitron text-[11px] font-bold tracking-[0.28em] text-[#E8EEF5]">
          PUSAT KENDALI AGENT
        </span>
      </div>

      <div className="mt-3 flex-1 space-y-3 overflow-hidden px-4">
        <div className="rounded-xl border border-[#22D3EE]/15 bg-[#0D141F]/85 p-3">
          <HudLabel>TINGKAT OTORITAS</HudLabel>
          <div className="mt-2 space-y-1.5">
            {[
              { k: "AMAN", d: "Baca-saja: diagnostik, hitung, ingatan", on: false },
              { k: "BAKU", d: "+ aksi ringan: buka app, volume, file", on: true },
              { k: "PENUH", d: "+ komunikasi: telepon, SMS draft", on: false },
            ].map((o) => (
              <div
                key={o.k}
                className={`flex items-center gap-2 rounded-lg border px-2.5 py-2 ${
                  o.on
                    ? "border-[#22D3EE]/45 bg-[#22D3EE]/10 shadow-[0_0_18px_-4px_rgba(34,211,238,0.55)]"
                    : "border-[#8A97A8]/15"
                }`}
              >
                <span
                  className={`h-2.5 w-2.5 shrink-0 rounded-full border ${
                    o.on ? "border-[#67E8F9] bg-[#22D3EE] shadow-[0_0_8px_rgba(34,211,238,0.8)]" : "border-[#8A97A8]/60"
                  }`}
                />
                <div className="flex-1">
                  <span className={`font-hud text-[9px] tracking-[0.25em] ${o.on ? "text-[#67E8F9]" : "text-[#B9C4D2]"}`}>
                    {o.k}
                  </span>
                  <span className="ml-2 font-rajdhani text-[9px] text-[#8A97A8]">{o.d}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="rounded-xl border border-[#22D3EE]/15 bg-[#0D141F]/85 p-3">
          <div className="flex items-center justify-between">
            <HudLabel>MODE MESIN</HudLabel>
            <span className="font-hud text-[7px] tracking-[0.2em] text-[#F5B54A]">POLA · MANUS × DEVIN</span>
          </div>
          <div className="mt-2 flex gap-1">
            {["HEMAT", "BAKU", "MAKSIMAL"].map((m, i) => (
              <span
                key={m}
                className={`flex-1 rounded-md border px-1 py-1.5 text-center font-hud text-[7.5px] tracking-[0.15em] ${
                  i === 2
                    ? "border-[#F5B54A]/55 bg-[#F5B54A]/10 text-[#F5B54A]"
                    : "border-[#8A97A8]/20 text-[#8A97A8]"
                }`}
              >
                {m}
              </span>
            ))}
          </div>
          <div className="mt-2">
            <div className="flex items-center justify-between font-hud text-[7.5px] tracking-[0.18em] text-[#8A97A8]">
              <span className="flex items-center gap-1">
                <Sparkles className="h-2.5 w-2.5 text-[#F5B54A]" /> JATAH TUGAS BERAT
              </span>
              <span className="text-[#F5B54A]">34K / 50K TOKEN</span>
            </div>
            <div className="mt-1 h-1 rounded-full bg-[#F5B54A]/15">
              <div className="h-full w-[68%] rounded-full bg-[#F5B54A]/60" />
            </div>
          </div>
        </div>

        <div className="rounded-xl border border-[#22D3EE]/15 bg-[#0D141F]/85 p-3">
          <HudLabel>PERKAKAS TUGAS BERAT</HudLabel>
          <div className="mt-2 space-y-2">
            {[
              { t: "Laporan PDF", on: true },
              { t: "Tulis & jalankan kode", on: true },
              { t: "Analisa kode", on: true },
              { t: "Akses file HP", on: false },
            ].map((r) => (
              <div key={r.t} className="flex items-center justify-between">
                <span className="font-rajdhani text-[10.5px] font-medium text-[#E8EEF5]">{r.t}</span>
                <Switch on={r.on} />
              </div>
            ))}
          </div>
        </div>

        <div className="rounded-xl border border-[#22D3EE]/15 bg-[#0D141F]/85 p-3">
          <HudLabel>LOG AKSI TERAKHIR</HudLabel>
          <div className="mt-2 space-y-1 font-hud text-[8.5px] tracking-wide">
            <div className="flex justify-between text-[#8A97A8]">
              <span>13.41 · render_pdf</span>
              <span className="text-[#4ADE80]">OK</span>
            </div>
            <div className="flex justify-between text-[#8A97A8]">
              <span>13.39 · analisa_tren</span>
              <span className="text-[#4ADE80]">OK</span>
            </div>
            <div className="mt-1.5 flex items-center justify-between rounded border border-[#F5B54A]/30 bg-[#F5B54A]/5 px-2 py-1 font-hud text-[7.5px] tracking-[0.18em] text-[#F5B54A]">
              <span>1 IZIN MENUNGGU KEPUTUSAN</span>
              <span>LAYAR 8 →</span>
            </div>
          </div>
        </div>
      </div>
      <Telemetry state="OTORITAS: BAKU · PUSAT KENDALI" />
    </Phone>
  );
}

/* ---------- data referensi ---------- */

const REFERENCES = [
  {
    name: "MANUS",
    url: "https://manus.im",
    source: "manus.im · gist sistem desain UI Manus · blog 2.0 — riset ulang, terverifikasi",
    points:
      "Dock computer: thumbnail hidup + status berdenyut + penghitung langkah + timer. Linimasa bisa diputar ulang seperti video — mundur, scrub, “Jump to live”. Meteran kredit ber-ikon kilau, emoji otomatis per tugas, kartu berkas (pratinjau/unduh/bagikan), tugas terjadwal (automations).",
  },
  {
    name: "DEVIN",
    url: "https://docs.devin.ai",
    source: "docs.devin.ai · 7 halaman produk — riset ulang, terverifikasi",
    points:
      "Sesi = Shell / IDE / Browser + tab Progress terpadu. Riwayat shell bisa diklik untuk melompat waktu (perintah masa depan tampak suram). Checkpoint & pulihkan, bukti uji berupa video, obrolan samping “/btw” tanpa memutus kerja, pemilih mesin Lite→Ultra.",
  },
  {
    name: "CHATGPT AGENT",
    url: "https://openai.com/index/introducing-chatgpt-agent",
    source: "openai.com — parsial: halaman resmi menolak akses sandbox (belum terverifikasi penuh)",
    points:
      "Narasi langkah hidup di obrolan; pengguna bisa menyisip instruksi kapan pun; izin untuk aksi sensitif; komputer virtual yang terlihat berjalan. Pola inti dikonfirmasi lewat dokumentasi sekunder.",
  },
  {
    name: "CLAUDE ARTIFACTS",
    url: "https://support.claude.com",
    source: "support.claude.com · 2 artikel resmi — riset ulang, terverifikasi",
    points:
      "Artefak dibuka di panel tersendiri; riwayat versi; menu Ekspor (PDF/Word/Markdown); lembaga akses “Who has access” (lihat/komentar/ubah); tombol “Try fixing with Claude” saat artefak error; galeri templat Docs–Slides–Design–Dashboard.",
  },
  {
    name: "REPLIT AGENT",
    url: "https://docs.replit.com/replitai/agent",
    source: "docs.replit.com · Agent 3 — BARU, terverifikasi",
    points:
      "Persetujuan rencana: “Accept tasks / Revise plan”, lalu bangun di depan vs di latar. Checkpoint kembali ke keadaan sebelumnya; papan tugas Drafts/Active/Ready/Done; slider usaha (effort); persetujuan “Allow once” untuk aksi berbayar.",
  },
  {
    name: "JULES",
    url: "https://jules.google/docs",
    source: "jules.google/docs · Google — BARU, terverifikasi",
    points:
      "Pilih repo + cabang → “Give me a plan” → tinjau & setujui sebelum kode disentuh. Linimasa aktivitas dengan diff, tugas terjadwal, notifikasi saat tuntas atau butuh masukan.",
  },
];

const MISSED = [
  { f: "Linimasa putar-ulang + tombol “Ke Live”", s: "MANUS", l: "Layar 5 · Ruang Kerja" },
  { f: "Riwayat perintah shell — ketuk untuk lompat waktu", s: "DEVIN", l: "Layar 5 · Ruang Kerja" },
  { f: "Tab sesi: Shell / Berkas / Pratinjau", s: "DEVIN", l: "Layar 5 · Ruang Kerja" },
  { f: "Thumbnail hidup + hitungan langkah + timer di dock", s: "MANUS", l: "Layar 4 · Eksekusi" },
  { f: "Narasi langkah hidup + sisip instruksi cepat", s: "CHATGPT AGENT", l: "Layar 4 · Eksekusi" },
  { f: "Tanya samping “/btw” tanpa memutus tugas", s: "DEVIN", l: "Layar 4 · Eksekusi" },
  { f: "Estimasi langkah & token sebelum jalan", s: "MANUS", l: "Layar 2 · Ajukan" },
  { f: "Konektor opsional (berkas, kalender, notifikasi)", s: "MANUS", l: "Layar 2 · Ajukan" },
  { f: "Versi artefak v1 → v3 + pindah versi", s: "CLAUDE", l: "Layar 6 · Hasil" },
  { f: "Akses berbagi berjenjang: lihat / komentar / ubah", s: "CLAUDE", l: "Layar 6 · Hasil" },
  { f: "Bukti QA otomatis pada kartu hasil", s: "DEVIN", l: "Layar 6 · Hasil" },
  { f: "Checkpoint & “PULIHKAN” per sesi", s: "DEVIN", l: "Layar 7 · Riwayat" },
  { f: "Emoji otomatis per tugas di riwayat", s: "MANUS", l: "Layar 7 · Riwayat" },
  { f: "Tugas terjadwal / otomatisasi (opsional)", s: "MANUS · JULES", l: "Layar 7 · Riwayat" },
  { f: "Antrean izin + “Izinkan sekali”", s: "CHATGPT AGENT · REPLIT", l: "Layar 8 · Izin — BARU" },
  { f: "Meteran jatah token + mode mesin HEMAT→MAKSIMAL", s: "MANUS · DEVIN · REPLIT", l: "Layar 9 · Kendali" },
];

const FLOW = [
  { no: "01", t: "AJUKAN TUGAS", d: "Dari obrolan atau form Mode Agent" },
  { no: "02", t: "RENCANA DIBUAT", d: "AVI menyusun daftar langkah" },
  { no: "03", t: "PAK MENYETUJUI", d: "Bisa ubah dulu — tidak nekat jalan" },
  { no: "04", t: "EKSEKUSI LIVE", d: "Reaktor bekerja + linimasa langkah hidup" },
  { no: "05", t: "ARTEFAK", d: "PDF / kode / analisa siap pakai" },
  { no: "06", t: "REVISI / SELESAI", d: "Iterasi pada artefak yang sama — checkpoint tersimpan" },
];

/* ---------- halaman ---------- */

export default function Page() {
  return (
    <div className="avi-nebula-deep relative flex min-h-screen flex-col">
      <div className="avi-stars pointer-events-none absolute inset-0 opacity-40" />
      <div className="avi-grid pointer-events-none absolute inset-0 opacity-40" />

      {/* bar atas */}
      <header className="sticky top-0 z-50 border-b border-[#22D3EE]/15 bg-[#05080D]/85 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-2 px-4 py-3 sm:px-6">
          <div className="flex items-center gap-3">
            <AviOrb size={30} />
            <div>
              <div className="font-orbitron text-[13px] font-bold tracking-[0.3em] text-[#E8EEF5]">
                AVI <span className="text-[#22D3EE]">⌁</span> PROPOSAL DESAIN
              </div>
              <div className="font-hud text-[9px] tracking-[0.3em] text-[#8A97A8]">
                MODE AGENT — TUGAS BERAT · v1.4.0(13) · RISET ULANG
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <StatusBadge tone="gold" pulse>
              KONSULTASI — TANPA IMPLEMENTASI
            </StatusBadge>
          </div>
        </div>
      </header>

      <main className="relative z-10">
        {/* hero */}
        <section className="mx-auto max-w-7xl px-4 pb-10 pt-12 text-center sm:px-6 sm:pt-16">
          <div className="font-hud text-[10px] tracking-[0.5em] text-[#67E8F9]">
            PAPAN KONSULTASI DESAIN — UNTUK DILIHAT, BUKAN DILIKESEN
          </div>
          <h1 className="mx-auto mt-4 max-w-4xl font-orbitron text-3xl font-black leading-tight tracking-wide text-[#E8EEF5] sm:text-5xl">
            MODE AGENT =
            <span className="text-[#F5B54A]"> TUGAS BERAT</span>
          </h1>
          <p className="mx-auto mt-5 max-w-3xl font-rajdhani text-lg font-medium leading-relaxed text-[#B9C4D2] sm:text-xl">
            Laporan PDF, coding, analisa kode — pekerjaan berat yang dikerjakan sampai tuntas,
            bukan sekadar buka aplikasi. Papan ini dibangun ulang setelah riset visual ulang ke
            enam produk agen nyata — Manus, Devin, ChatGPT Agent, Claude, Replit Agent, Jules —
            lalu dijahit ke identitas HUD milik AVI (tema MK1). Tanpa jam, tanpa chip saran,
            sesuai arahan — dan nuansa Jarvis tetap utuh: reaktor tetap jantungnya, bingkai
            Stark tetap rangkanya.
          </p>
          <div className="mt-7 flex flex-wrap items-center justify-center gap-2">
            <SourceTag>9 LAYAR MOCKUP</SourceTag>
            <SourceTag>RISET ULANG · 6 SUMBER</SourceTag>
            <SourceTag>16 FITUR TERLEWAT DITEMUKAN</SourceTag>
            <SourceTag>SATU KELUARGA VISUAL HUD MK1</SourceTag>
            <span className="rounded-full border border-[#F5B54A]/40 bg-[#F5B54A]/10 px-2.5 py-1 font-hud text-[9px] tracking-wider text-[#F5B54A]">
              NUANSA JARVIS DIJAGA
            </span>
          </div>
        </section>

        {/* referensi */}
        <section className="mx-auto max-w-7xl px-4 pb-6 sm:px-6">
          <div className="mb-6 flex items-center gap-3">
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
            <h2 className="font-orbitron text-sm font-bold tracking-[0.35em] text-[#E8EEF5]">
              01 · REFERENSI YANG DIPELAJARI
            </h2>
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            {REFERENCES.map((r) => (
              <div
                key={r.name}
                className="rounded-2xl border border-[#22D3EE]/15 bg-[#0A0F18]/70 p-5 transition-colors hover:border-[#22D3EE]/35"
              >
                <div className="flex items-center justify-between gap-3">
                  <div className="font-orbitron text-[13px] font-bold tracking-[0.25em] text-[#F5B54A]">
                    {r.name}
                  </div>
                  <a
                    href={r.url}
                    target="_blank"
                    rel="noreferrer"
                    className="flex items-center gap-1 font-hud text-[9px] tracking-wider text-[#67E8F9] hover:text-[#E8EEF5]"
                  >
                    SUMBER <ExternalLink className="h-3 w-3" />
                  </a>
                </div>
                <div className="mt-1 font-hud text-[9px] tracking-wider text-[#8A97A8]">
                  {r.source}
                </div>
                <p className="mt-3 font-rajdhani text-[15px] font-medium leading-relaxed text-[#B9C4D2]">
                  {r.points}
                </p>
              </div>
            ))}
          </div>
          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <div className="rounded-2xl border border-dashed border-[#F87171]/40 bg-[#F87171]/5 p-5">
              <div className="flex items-center gap-2">
                <X className="h-4 w-4 text-[#F87171]" />
                <span className="font-orbitron text-[12px] font-bold tracking-[0.25em] text-[#F87171]">
                  JAM — DIHAPUS
                </span>
              </div>
              <p className="mt-2 font-rajdhani text-[15px] font-medium leading-relaxed text-[#B9C4D2]">
                Telemetri “JAM 21.47” di orbit diagnostik layar obrolan dibuang total — tanpa
                pengganti.
              </p>
            </div>
            <div className="rounded-2xl border border-dashed border-[#F87171]/40 bg-[#F87171]/5 p-5">
              <div className="flex items-center gap-2">
                <X className="h-4 w-4 text-[#F87171]" />
                <span className="font-orbitron text-[12px] font-bold tracking-[0.25em] text-[#F87171]">
                  CHIP SARAN — DIHAPUS
                </span>
              </div>
              <p className="mt-2 font-rajdhani text-[15px] font-medium leading-relaxed text-[#B9C4D2]">
                Deret chip “Jam berapa? · Tanggal berapa? · Baterai berapa? · Kondisi HP · Sisa
                penyimpanan” dibuang total — tanpa pengganti.
              </p>
            </div>
          </div>
        </section>

        {/* fitur riset ulang yang terlewat */}
        <section className="mx-auto max-w-7xl px-4 pb-10 sm:px-6">
          <div className="mb-2 flex items-center gap-3">
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#F5B54A]/50 to-transparent" />
            <h2 className="font-orbitron text-sm font-bold tracking-[0.35em] text-[#F5B54A]">
              01B · FITUR DARI RISET ULANG — YANG SEBELUMNYA TERLEWAT
            </h2>
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#F5B54A]/50 to-transparent" />
          </div>
          <p className="mx-auto mb-6 max-w-3xl text-center font-rajdhani text-[15px] font-medium leading-relaxed text-[#8A97A8]">
            Papan lama memakai 4 sumber. Kali ini saya pelajari ulang lebih dalam — 6 produk,
            termasuk Replit Agent &amp; Jules — dan menemukan 16 fitur yang ternyata lolos.
            Semuanya sudah dijahit ke layar-layar di bawah.
          </p>
          <div className="grid gap-2.5 sm:grid-cols-2 lg:grid-cols-3">
            {MISSED.map((m) => (
              <div
                key={m.f}
                className="flex items-start gap-2.5 rounded-xl border border-[#22D3EE]/12 bg-[#0A0F18]/70 px-3.5 py-2.5"
              >
                <Sparkles className="mt-0.5 h-3 w-3 shrink-0 text-[#F5B54A]" />
                <div className="min-w-0">
                  <div className="font-rajdhani text-[13.5px] font-semibold leading-snug text-[#E8EEF5]">
                    {m.f}
                  </div>
                  <div className="mt-0.5 font-hud text-[8px] tracking-[0.16em] text-[#67E8F9]">{m.s}</div>
                  <div className="font-hud text-[8px] tracking-[0.16em] text-[#8A97A8]">→ {m.l}</div>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* nuansa jarvis */}
        <section className="mx-auto max-w-7xl px-4 pb-10 sm:px-6">
          <div className="mb-6 flex items-center gap-3">
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
            <h2 className="font-orbitron text-sm font-bold tracking-[0.35em] text-[#67E8F9]">
              02 · NUANSA JARVIS — YANG DIJAGA
            </h2>
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
          </div>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
            {[
              {
                t: "REAKTOR = JANTUNG",
                d: "Dari ReactorOrb asli app: siaga berdenyut, berpikir cincin orbit, bekerja semua ring. Di Mode Agent reaktor ikut bekerja — progres tugas terbaca di cincinnya.",
              },
              {
                t: "BINGKAI STARK",
                d: "Dari HudFrame asli app: 4 bracket sudut ber-titik emas, rel samping bertick merambat, sapuan hologram melintasi layar.",
              },
              {
                t: "TELEMETRI HIDUP",
                d: "Baris status mono yang bernafas dan sesekali berkedip di kaki tiap layar — status reaktor, bukan jam.",
              },
              {
                t: "SAPAAN “PAK”",
                d: "Kepribadian Jarvis tetap dalam bahasa kita: “Siap, Pak.” — sopan, tenang, selalu siaga.",
              },
              {
                t: "CYAN + EMAS",
                d: "Palet MK1 asli: sian reaktor + emas Stark. Sembilan layar memakai bahasa visual yang sama dengan rumah.",
              },
            ].map((c) => (
              <div key={c.t} className="rounded-2xl border border-[#22D3EE]/15 bg-[#0A0F18]/70 p-4">
                <div className="font-orbitron text-[11px] font-bold tracking-[0.18em] text-[#67E8F9]">
                  {c.t}
                </div>
                <p className="mt-2 font-rajdhani text-[14px] font-medium leading-snug text-[#B9C4D2]">
                  {c.d}
                </p>
              </div>
            ))}
          </div>
        </section>

        {/* alur tugas */}
        <section className="mx-auto max-w-7xl px-4 py-10 sm:px-6">
          <div className="mb-6 flex items-center gap-3">
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
            <h2 className="font-orbitron text-sm font-bold tracking-[0.35em] text-[#E8EEF5]">
              03 · ALUR SATU TUGAS AGENT
            </h2>
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
          </div>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
            {FLOW.map((f) => (
              <div
                key={f.no}
                className="relative rounded-2xl border border-[#22D3EE]/15 bg-[#0A0F18]/70 p-4 text-center"
              >
                <div className="font-hud text-[10px] tracking-[0.3em] text-[#22D3EE]">{f.no}</div>
                <div className="mt-1.5 font-orbitron text-[10px] font-bold tracking-[0.14em] text-[#E8EEF5]">
                  {f.t}
                </div>
                <div className="mt-1.5 font-rajdhani text-[13px] font-medium leading-snug text-[#8A97A8]">
                  {f.d}
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* galeri mockup */}
        <section className="mx-auto max-w-7xl px-4 pb-12 sm:px-6">
          <div className="mb-8 flex items-center gap-3">
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
            <h2 className="font-orbitron text-sm font-bold tracking-[0.35em] text-[#E8EEF5]">
              04 · SEMBILAN LAYAR — TETAP BERNUANSA JARVIS
            </h2>
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
          </div>
          <div className="grid items-start gap-6 lg:grid-cols-2 xl:grid-cols-3">
            <PhoneCard
              no="LAYAR 1"
              label="OBROLAN — DIBERSIHKAN"
              desc="Rumah tetap seperti sekarang: obrolan cepat tanpa alat. Yang berubah hanya: jam dan chip saran lenyap."
              sources={["ARAHAN PAK", "TAMPILAN LAMA DIBERSIHKAN"]}
              notes={[
                "Telemetri JAM di orbit dihapus — ditandai kotak merah putus-putus.",
                "Chip saran instan dihapus — ditandai kotak merah putus-putus.",
                "Jawaban AVI bisa menawarkan pengangkatan tugas ke Mode Agent (tombol emas di gelembung).",
              ]}
            >
              <ScreenObrolan />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 2"
              label="MODE AGENT — AJUKAN TUGAS"
              desc="Pintu masuk kerja berat. Formulir tugas: judul, instruksi, lampiran — bukan pertanyaan kilat."
              sources={["MANUS — task intake", "DEVIN — sesi tugas"]}
              notes={[
                "Kartu “Kemampuan berat” sengaja ditandai OPSIONAL — kalau dianggap sampah, langsung dibuang.",
                "Tugas bisa diajukan juga langsung dari obrolan (layar 1).",
              ]}
            >
              <ScreenAjukan />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 3"
              label="RENCANA — MENUNGGU PERSETUJUAN"
              desc="Sebelum jalan, AVI membuka rencananya. Pak bisa mengubah atau menolak — tidak ada eksekusi nekat."
              sources={["MANUS — todo.md", "DEVIN — plan review"]}
              notes={[
                "Badge emas berdenyut = menunggu keputusan Pak.",
                "Estimasi durasi + jumlah langkah tampil di kepala kartu.",
              ]}
            >
              <ScreenRencana />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 4"
              label="EKSEKUSI LIVE"
              desc="Linimasa langkah hidup gaya HUD: selesai (hijau), aktif (sian berdenyut + baris “berpikir…”), tertunda (redup)."
              sources={[
                "MANUS — step states + computer dock",
                "CHATGPT AGENT — narasi live + intervensi",
                "DEVIN — /btw",
              ]}
              notes={[
                "Reaktor = progres: cincin berputar cepat + busur emas membaca 3/6 di sekeliling inti.",
                "Baru: strip narasi mono “› membuka…”, kolom sisip instruksi cepat, jalur /btw tanya-samping.",
                "Kontrol JEDA / HENTIKAN selalu terjangkau jempol.",
                "Dock “RUANG KERJA AVI” = versi AVI untuk panel computer Manus: pratinjau hidup + status.",
              ]}
            >
              <ScreenEksekusi />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 5"
              label="RUANG KERJA AVI — KOMPUTER AVI"
              desc="Jendela ke “komputer” yang dioperasikan AVI: shell, berkas, pratinjau — plus linimasa yang bisa diputar ulang."
              sources={["MANUS — computer dock & replay", "DEVIN — shell/ide/browser + progress"]}
              notes={[
                "Baru dari riset ulang: riwayat perintah yang bisa diklik untuk melompat waktu — yang belum jalan tampil suram.",
                "Bar putar-ulang gaya Manus: mundur ke momen mana pun, satu tombol “KE LIVE”.",
                "Ini pelengkap dock mini di Layar 4 — dock = sekilas, layar ini = seluruhnya.",
              ]}
            >
              <ScreenRuangKerja />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 6"
              label="HASIL — ARTEFAK"
              desc="Hasil kerja diserahkan sebagai artefak: PDF ber-versi v1→v3, pratinjau, QA otomatis, dan pintu revisi."
              sources={["CLAUDE — versions & sharing", "MANUS — file cards", "DEVIN — bukti uji"]}
              notes={[
                "Pratinjau dokumen + grafik dirender langsung di kartu.",
                "Baru: pil versi v1→v3 (gaya Claude), akses berbagi berjenjang, status QA otomatis.",
                "Iterasi lanjutan lewat kolom “Minta revisi…” pada artefak yang sama.",
              ]}
            >
              <ScreenHasil />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 7"
              label="RIWAYAT TUGAS"
              desc="Semua tugas tersimpan sebagai sesi — lengkap dengan checkpoint yang bisa dipulihkan."
              sources={["DEVIN — sessions + checkpoints", "MANUS — task grid + emoji", "JULES — scheduled"]}
              notes={[
                "Kelompok HARI INI / KEMARIN, badge status per tugas.",
                "Baru: kartu CHECKPOINT + PULIHKAN (gaya Devin), emoji otomatis per tugas (gaya Manus).",
                "Baris tugas terjadwal ditandai OPSIONAL — bisa dibuang jika dianggap sampah.",
              ]}
            >
              <ScreenRiwayat />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 8"
              label="IZIN & INTERVENSI"
              desc="Saat aksi menyentuh area sensitif, AVI berhenti sejenak dan meminta izin — Pak tetap satu-satunya pemberi kunci."
              sources={["CHATGPT AGENT — permission", "REPLIT — allow once"]}
              notes={[
                "Baru dari riset ulang: kartu izin eksplisit — Izinkan sekali / selama tugas ini / Tolak.",
                "Antrean permintaan terlihat; tanpa izin, AVI menyesuaikan jalan sendiri — tidak nekat.",
              ]}
            >
              <ScreenIzin />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 9"
              label="PUSAT KENDALI AGENT"
              desc="Satu rumah untuk otoritas AMAN/BAKU/PENUH, mode mesin, jatah tugas, saklar per-perkakas, dan log aksi."
              sources={["CHATGPT AGENT — izin aksi", "MANUS — credits meter", "REPLIT — effort"]}
              notes={[
                "Tingkat otoritas lanjut dari skema yang sudah jalan di K22.",
                "Baru: MODE MESIN (HEMAT–MAKSIMAL) + meteran JATAH TUGAS BERAT + baris antrean izin.",
                "Log aksi format mono gaya telemetri HUD.",
              ]}
            >
              <ScreenKendali />
            </PhoneCard>
          </div>
        </section>

        {/* usulan */}
        <section className="mx-auto max-w-7xl px-4 pb-16 sm:px-6">
          <div className="mb-6 flex items-center gap-3">
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#F5B54A]/50 to-transparent" />
            <h2 className="font-orbitron text-sm font-bold tracking-[0.35em] text-[#F5B54A]">
              05 · USULAN — HANYA JIKA DESAIN INI DISETUJUI
            </h2>
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#F5B54A]/50 to-transparent" />
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            {[
              {
                t: "A · MESIN TUGAS BERAT",
                d: "Perluasan mesin agen: antrian tugas, rencana, langkah ber-status, penyusunan artefak (PDF / kode / analisa).",
              },
              {
                t: "B · LAPISAN TAMPILAN",
                d: "Sembilan layar di atas dipindahkan ke Jetpack Compose — satu keluarga visual HUD MK1, tanpa elemen asing.",
              },
              {
                t: "C · KENDALI & KEAMANAN",
                d: "Otoritas AMAN/BAKU/PENUH, saklar per-perkakas, log aksi — semua keputusan tetap di tangan Pak.",
              },
            ].map((c) => (
              <div key={c.t} className="rounded-2xl border border-[#F5B54A]/25 bg-[#0A0F18]/70 p-5">
                <div className="font-orbitron text-[12px] font-bold tracking-[0.2em] text-[#F5B54A]">
                  {c.t}
                </div>
                <p className="mt-2.5 font-rajdhani text-[15px] font-medium leading-relaxed text-[#B9C4D2]">
                  {c.d}
                </p>
                <div className="mt-3">
                  <StatusBadge tone="gold">MENUNGGU PERSETUJUAN</StatusBadge>
                </div>
              </div>
            ))}
          </div>

          <div className="mt-6 rounded-2xl border border-dashed border-[#F87171]/45 bg-[#F87171]/5 p-5">
            <div className="font-orbitron text-[12px] font-bold tracking-[0.25em] text-[#F87171]">
              YANG TIDAK SAYA LAKUKAN
            </div>
            <p className="mt-2 font-rajdhani text-[15px] font-medium leading-relaxed text-[#B9C4D2]">
              Tidak satu baris pun dari app AVI yang disentuh. Tidak ada commit, push, atau
              workflow yang didorong. Halaman ini hanya papan gambar — keputusan penuh ada di
              tangan Pak.
            </p>
          </div>
        </section>
      </main>

      {/* kaki sticky */}
      <footer className="relative z-10 mt-auto border-t border-[#22D3EE]/15 bg-[#05080D]/90 pb-[env(safe-area-inset-bottom)] backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-2 px-4 py-4 sm:px-6">
          <div className="font-hud text-[10px] tracking-[0.25em] text-[#8A97A8]">
            AVI · PAPAN KONSULTASI DESAIN · MODE AGENT v1.4.0
          </div>
          <div className="font-hud text-[9px] tracking-[0.2em] text-[#67E8F9]">
            REFERENSI: manus.im · docs.devin.ai · openai.com · support.claude.com · docs.replit.com · jules.google
          </div>
        </div>
      </footer>
    </div>
  );
}
