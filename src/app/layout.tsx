import type { Metadata } from "next";
import { Orbitron, Rajdhani, JetBrains_Mono } from "next/font/google";
import "./globals.css";

const orbitron = Orbitron({
  variable: "--font-orbitron",
  subsets: ["latin"],
});

const rajdhani = Rajdhani({
  variable: "--font-rajdhani",
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700"],
});

const jbMono = JetBrains_Mono({
  variable: "--font-jbmono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "AVI — Proposal Desain Mode Agent v1.4.0",
  description:
    "Papan konsultasi desain tampilan Mode Agent AVI: tugas berat (laporan PDF, coding, analisa kode). Dirancang dari referensi Manus, Devin, ChatGPT Agent, dan Claude — tanpa implementasi.",
  icons: {
    icon: "https://z-cdn.chatglm.cn/z-ai/static/logo.svg",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="id" suppressHydrationWarning>
      <body
        className={`${orbitron.variable} ${rajdhani.variable} ${jbMono.variable} antialiased bg-[#05080D] text-[#E8EEF5]`}
      >
        {children}
      </body>
    </html>
  );
}
