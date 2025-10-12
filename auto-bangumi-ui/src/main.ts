import {initI18n} from '@/lang/i18n.ts';

(async function() {
  const cache = localStorage.getItem('app');
  let language = 'zh_cn'
  if (cache) {
    const state = JSON.parse(cache);
    language = state.language;
  }
  await initI18n(language);
  const module = await import('./mount');
  await module.mountApp();
})();
