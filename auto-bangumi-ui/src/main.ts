import { initI18n } from '@/lang/i18n.ts';

(async function() {
  const cache = localStorage.getItem('app');
  if (cache) {
    const state = JSON.parse(cache);
    const language = state.language;
    await initI18n(language || 'zh_cn');
  }
  const module = await import('./mount');
  await module.mountApp();
})();
