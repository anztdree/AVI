import ZAI from 'z-ai-web-dev-sdk';

const QUERIES = [
  'ChatGPT Tasks scheduled tasks how to manage view pause notifications',
  'Gemini app scheduled actions manage pause delete',
  'Gemini Deep Research plan approve browsing report UX',
  'Manus AI task list sidebar replay UX',
  'Claude artifacts panel versions revision UX',
  'voice assistant task management UI pattern drawer chat first',
];

const zai = await ZAI.create();
const out = [];
for (const q of QUERIES) {
  try {
    const r = await zai.functions.invoke('web_search', { query: q, num: 6 });
    out.push({ q, results: r.map(x => ({ name: x.name, url: x.url, snippet: x.snippet, host: x.host_name })) });
    console.error('OK: ' + q);
  } catch (e) {
    out.push({ q, error: String(e) });
    console.error('FAIL: ' + q + ' -> ' + e.message);
  }
}
console.log(JSON.stringify(out, null, 1));
