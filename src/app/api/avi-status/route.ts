import { NextResponse } from 'next/server';

const REPO = 'anztdree/AVI';
const PAT = process.env.GITHUB_PAT || '';

export async function GET() {
  const headers: Record<string, string> = {
    Accept: 'application/vnd.github+json',
    'User-Agent': 'AVI-Dashboard',
  };
  if (PAT) headers.Authorization = `token ${PAT}`;

  try {
    const [runsRes, repoRes] = await Promise.all([
      fetch(`https://api.github.com/repos/${REPO}/actions/runs?per_page=3`, {
        headers,
        cache: 'no-store',
      }),
      fetch(`https://api.github.com/repos/${REPO}`, { headers, cache: 'no-store' }),
    ]);

    if (!runsRes.ok) {
      return NextResponse.json(
        { ok: false, error: `GitHub API: ${runsRes.status}` },
        { status: 200 }
      );
    }

    const runsData = await runsRes.json();
    const runs = (runsData.workflow_runs || []).map((r: any) => ({
      id: r.id,
      number: r.run_number,
      status: r.status,
      conclusion: r.conclusion,
      url: r.html_url,
      createdAt: r.created_at,
    }));

    let repoInfo: any = null;
    if (repoRes.ok) {
      const r = await repoRes.json();
      repoInfo = {
        full_name: r.full_name,
        stars: r.stargazers_count,
        private: r.private,
        url: r.html_url,
      };
    }

    // Info release terbaru (APK siap unduh tanpa login)
    let release: any = null;
    try {
      const relRes = await fetch(
        `https://api.github.com/repos/${REPO}/releases/latest`,
        { headers, cache: 'no-store' }
      );
      if (relRes.ok) {
        const rel = await relRes.json();
        const apkAsset = (rel.assets || []).find((a: any) =>
          a.name.endsWith('.apk')
        );
        if (apkAsset) {
          release = {
            tag: rel.tag_name,
            name: rel.name,
            url: rel.html_url,
            apkName: apkAsset.name,
            apkDownloadUrl: apkAsset.browser_download_url,
            apkSizeMb:
              Math.round((apkAsset.size / 1024 / 1024) * 10) / 10,
          };
        }
      }
    } catch {
      // abaikan
    }

    // Jika build sukses, cek apakah artifact APK tersedia
    let artifact: any = null;
    const successRun = runs.find(
      (r: any) => r.status === 'completed' && r.conclusion === 'success'
    );
    if (successRun && PAT) {
      try {
        const artRes = await fetch(
          `https://api.github.com/repos/${REPO}/actions/runs/${successRun.id}/artifacts`,
          { headers, cache: 'no-store' }
        );
        if (artRes.ok) {
          const artData = await artRes.json();
          const apk = (artData.artifacts || []).find(
            (a: any) => a.name === 'AVI-debug-apk'
          );
          if (apk) {
            artifact = {
              id: apk.id,
              name: apk.name,
              sizeMb: Math.round((apk.size_in_bytes / 1024 / 1024) * 10) / 10,
              expired: apk.expired,
            };
          }
        }
      } catch {
        // abaikan
      }
    }

    return NextResponse.json({ ok: true, runs, repoInfo, artifact, release });
  } catch (e: any) {
    return NextResponse.json(
      { ok: false, error: e?.message || 'fetch gagal' },
      { status: 200 }
    );
  }
}
