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
} from "lucide-react";

/* ============================================================
   PAPAN KONSULTASI DESAIN — AVI MODE AGENT v1.4.0
   Bukan implementasi. Semua layar = mockup visual saja.
   Pola diambil dari produk agen nyata (lihat bagian Referensi).
   ============================================================ */

const C = {
  bg: "#070B12",
  panel: "#0D141F",
  panelLight: "#141E2E",
  accent: "#22D3EE",
  accentSoft: "#67E8F9",
  gold: "#F5B54A",
  goldSoft: "#FFD98A",
  text: "#E8EEF5",
  dim: "#8A97A8",
  danger: "#F87171",
  userBubble: "#251D0E",
  userBorder: "#8A6A2F",
  aviBubble: "#0C2833",
  aviBorder: "#1B5E70",
  glow: "#7DF3FF",
};

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

function Ring({ done, total }: { done: number; total: number }) {
  const r = 15;
  const circ = 2 * Math.PI * r;
  const pct = done / total;
  return (
    <div className="relative h-12 w-12">
      <svg viewBox="0 0 40 40" className="h-12 w-12 -rotate-90">
        <circle cx="20" cy="20" r={r} fill="none" stroke="rgba(138,151,168,0.25)" strokeWidth="3" />
        <circle
          cx="20"
          cy="20"
          r={r}
          fill="none"
          stroke={C.accent}
          strokeWidth="3"
          strokeLinecap="round"
          strokeDasharray={circ}
          strokeDashoffset={circ * (1 - pct)}
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center font-hud text-[9px] text-[#E8EEF5]">
        {done}/{total}
      </div>
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
          <Mic className="h-4 w-4 text-[#67E8F9]" />
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#F5B54A]/15">
          <Send className="h-4 w-4 text-[#F5B54A]" />
        </div>
      </div>
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
          <AviOrb size={44} />
        </div>
        <div className="mt-2 font-orbitron text-[11px] font-bold tracking-[0.4em] text-[#E8EEF5]">
          MODE AGENT
        </div>
        <p className="mt-1 text-center font-rajdhani text-[11px] font-medium text-[#8A97A8]">
          Tugas berat? Saya yang kerjakan sampai tuntas, Pak.
        </p>
      </div>

      <div className="mt-3 space-y-3 px-4">
        <div className="rounded-xl border border-[#22D3EE]/20 bg-[#0D141F]/85 p-3">
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
        <div className="rounded-xl border border-[#F5B54A]/30 bg-[#0D141F]/85 p-3">
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
        <Ring done={3} total={6} />
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
        <StatusBadge tone="green" pulse>
          TUNTAS
        </StatusBadge>
      </div>

      <div className="mt-2.5 flex items-center gap-1 overflow-hidden px-4">
        {["DATA", "TREN", "NARASI", "GRAFIK", "PDF", "QA"].map((s) => (
          <span key={s} className="flex items-center gap-1 font-hud text-[7.5px] tracking-[0.15em] text-[#4ADE80]">
            {s} <Check className="h-2.5 w-2.5" />
          </span>
        ))}
      </div>

      <div className="mt-3 flex-1 px-4">
        <HudLabel>ARTEFAK HASIL</HudLabel>
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
    </Phone>
  );
}

/* ---------- layar 6 : RIWAYAT TUGAS ---------- */

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
                t: "Laporan Penjualan Q3 — PDF",
                badge: <StatusBadge tone="green">SELESAI</StatusBadge>,
                meta: "13.42 · PDF",
              },
              {
                icon: Code2,
                t: "Refactor Modul Login",
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
        <div>
          <HudLabel>KEMARIN</HudLabel>
          <div className="mt-1.5 space-y-1.5">
            {[
              {
                icon: ScanSearch,
                t: "Audit Kode Pembayaran",
                badge: <StatusBadge tone="green">SELESAI</StatusBadge>,
                meta: "21.05",
              },
              {
                icon: BarChart3,
                t: "Analisa Stok Gudang",
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

      <div className="px-4 pb-5 text-center font-hud text-[8px] tracking-[0.22em] text-[#8A97A8]">
        KETUK UNTUK MEMBUKA ULANG ARTEFAK
      </div>
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
                  o.on ? "border-[#22D3EE]/45 bg-[#22D3EE]/10" : "border-[#8A97A8]/15"
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
            <div className="flex justify-between text-[#8A97A8]">
              <span>13.36 · baca_file</span>
              <span className="text-[#4ADE80]">OK</span>
            </div>
          </div>
        </div>
      </div>
      <div className="pb-5" />
    </Phone>
  );
}

/* ---------- data referensi ---------- */

const REFERENCES = [
  {
    name: "MANUS",
    url: "https://manus.im",
    source: "manus.im · panduan desain UI Manus (rincian komponen terverifikasi)",
    points:
      "Rencana berupa todo nyata; langkah ber-status (tertunda → aktif berdenyut → selesai); ikon aktivitas per alat; dock “computer” dengan thumbnail hidup + penghitung langkah + timer; kartu artefak (pratinjau, unduh, bagikan).",
  },
  {
    name: "DEVIN",
    url: "https://cognition.com/blog/introducing-devin-2-2",
    source: "cognition.com — antarmuka sesi Devin",
    points:
      "Satu tugas = satu sesi dengan linimasa sendiri; rencana ditinjau pengguna sebelum eksekusi; status agregat berbadge; riwayat sesi yang bisa dibuka ulang.",
  },
  {
    name: "CHATGPT AGENT",
    url: "https://openai.com/index/introducing-chatgpt-agent",
    source: "openai.com — pengantar ChatGPT agent",
    points:
      "Narasi langkah hidup di dalam obrolan (“mencari…”, “membaca file…”); pemakai bisa menyisip/mengintervensi kapan pun; kontrol jeda–hentikan; izin untuk aksi sensitif.",
  },
  {
    name: "CLAUDE ARTIFACTS",
    url: "https://support.claude.com/en/articles/17153992-what-are-artifacts-and-how-do-i-use-them",
    source: "support.claude.com — artikel artifacts",
    points:
      "Hasil kerja disajikan sebagai artefak dengan pratinjau tersendiri; iterasi/revisi dilanjutkan langsung pada artefak yang sama.",
  },
];

const FLOW = [
  { no: "01", t: "AJUKAN TUGAS", d: "Dari obrolan atau form Mode Agent" },
  { no: "02", t: "RENCANA DIBUAT", d: "AVI menyusun daftar langkah" },
  { no: "03", t: "PAK MENYETUJUI", d: "Bisa ubah dulu — tidak nekat jalan" },
  { no: "04", t: "EKSEKUSI LIVE", d: "Linimasa langkah + ruang kerja hidup" },
  { no: "05", t: "ARTEFAK", d: "PDF / kode / analisa siap pakai" },
  { no: "06", t: "REVISI / SELESAI", d: "Iterasi lanjutan pada artefak yang sama" },
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
                MODE AGENT — TUGAS BERAT · v1.4.0(13)
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
            bukan sekadar buka aplikasi. Seluruh tampilan di bawah dirancang dari pola produk
            agen nyata — Manus, Devin, ChatGPT Agent, Claude — lalu dijahit ke identitas HUD
            milik AVI (tema MK1). Tanpa jam, tanpa chip saran, sesuai arahan.
          </p>
          <div className="mt-7 flex flex-wrap items-center justify-center gap-2">
            <SourceTag>7 LAYAR MOCKUP</SourceTag>
            <SourceTag>SATU KELUARGA VISUAL HUD MK1</SourceTag>
            <SourceTag>SEMUANYA DARI REFERENSI</SourceTag>
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

        {/* alur tugas */}
        <section className="mx-auto max-w-7xl px-4 py-10 sm:px-6">
          <div className="mb-6 flex items-center gap-3">
            <span className="h-px flex-1 bg-gradient-to-r from-transparent via-[#22D3EE]/40 to-transparent" />
            <h2 className="font-orbitron text-sm font-bold tracking-[0.35em] text-[#E8EEF5]">
              02 · ALUR SATU TUGAS AGENT
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
              03 · TUJUH LAYAR — KETUK DAN AMATI
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
              sources={["MANUS — step states + computer dock", "CHATGPT AGENT — narasi live"]}
              notes={[
                "Cincin progres + penghitung langkah + timer di kepala layar.",
                "Kontrol JEDA / HENTIKAN selalu terjangkau jempol.",
                "Dock “RUANG KERJA AVI” = versi AVI untuk panel computer Manus: pratinjau hidup + status.",
              ]}
            >
              <ScreenEksekusi />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 5"
              label="HASIL — ARTEFAK"
              desc="Hasil kerja diserahkan sebagai artefak: PDF dengan pratinjau, ukuran, jumlah halaman — plus pintu revisi."
              sources={["CLAUDE — artifacts", "MANUS — file cards"]}
              notes={[
                "Pratinjau dokumen + grafik dirender langsung di kartu.",
                "Iterasi lanjutan lewat kolom “Minta revisi…” pada artefak yang sama.",
              ]}
            >
              <ScreenHasil />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 6"
              label="RIWAYAT TUGAS"
              desc="Semua tugas tersimpan sebagai sesi: selesai, jalan, dihentikan — bisa dibuka ulang beserta artefaknya."
              sources={["DEVIN — sessions", "MANUS — task grid"]}
              notes={[
                "Kelompok HARI INI / KEMARIN, badge status per tugas.",
                "Tugas yang sedang jalan berdenyut sian.",
              ]}
            >
              <ScreenRiwayat />
            </PhoneCard>

            <PhoneCard
              no="LAYAR 7"
              label="PUSAT KENDALI AGENT"
              desc="Satu rumah untuk otoritas AMAN/BAKU/PENUH, saklar per-perkakas tugas berat, dan log aksi."
              sources={["CHATGPT AGENT — izin aksi", "DISKUSI SEBELUMNYA — kendali"]}
              notes={[
                "Tingkat otoritas lanjut dari skema yang sudah jalan di K22.",
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
              04 · USULAN — HANYA JIKA DESAIN INI DISETUJUI
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
                d: "Tujuh layar di atas dipindahkan ke Jetpack Compose — satu keluarga visual HUD MK, tanpa elemen asing.",
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
            REFERENSI: manus.im · cognition.com · openai.com · support.claude.com
          </div>
        </div>
      </footer>
    </div>
  );
}
