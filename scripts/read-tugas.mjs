import ZAI from 'z-ai-web-dev-sdk';

const URLS = [
  'https://engincanveske.substack.com/p/chatgpts-hidden-gem-scheduled-tasks',
  'https://medium.com/@CherryZhouTech/gemini-scheduled-actions-your-ai-assistant-now-plans-ahead-d62fc13382c1',
];

const KEY = /(manage|settings|pause|pauses|notif|edit|delete|remove|drawer|scheduled|schedule|prompt|conversat|auto|chat|list)/ig;

const zai = await ZAI.create();
for (const u of URLS) {
  try {
    const r = await zai.functions.invoke('page_reader', { url: u });
    const text = String(r.data.html || '').replace(/<script[\s\S]*?<\/script>/gi, ' ').replace(/<style[\s\S]*?<\/style>/gi, ' ').replace(/<[^>]+>/g, ' ').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ');
    // ambil paragraf yang mengandung kata kunci UX
    const chunks = text.split(/(?<=[.!?]) /);
    const hits = chunks.filter(s => /(manage|settings|pause|notif|edit|delete|scheduled|drawer|prompt)/i.test(s)).slice(0, 25);
    console.log('===== ' + u + ' =====');
    console.log(hits.join('\n').slice(0, 3200));
    console.log('');
  } catch (e) {
    console.log('===== ' + u + ' ===== FAIL: ' + e.message);
  }
}
