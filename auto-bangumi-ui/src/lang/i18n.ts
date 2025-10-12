// I18n init configuration (Frontend)

import {createI18n} from 'vue-i18n';

// DO NOT I18N
// If you want to add the language of your own country, you need to add the code here.
export const SUPPORTED_LANGS = [
  {
    label: `English`,
    value: `en_us`
  },
  {
    label: `日本語`,
    value: `ja_jp`
  },
  {
    label: `简体中文`,
    value: `zh_cn`
  },
  {
    label: `繁體中文`,
    value: `zh_tw`
  }
];

export const LANGUAGE_KEY = 'LANGUAGE';

let i18n: any;

export function toStandardLang(lang?: string) {
  if (!lang) return 'en_us';
  return lang.replace('-', '_').toLowerCase();
}


// I18n init configuration
// If you want to add the language of your own country, you need to add the code here.
async function initI18n(lang: string) {
  lang = toStandardLang(lang);

  const messages: Record<string, any> = {};
  const langFiles = import.meta.glob('./languages/*.json');
  for (const path in langFiles) {
    if (toStandardLang(path).includes(lang) && typeof langFiles[path] === 'function') {
      messages[lang] = await langFiles[path]();
    }
  }

  i18n = createI18n({
    globalInjection: true,
    locale: lang,
    fallbackLocale: toStandardLang('en_us'),
    messages: messages
  });
}

export function getI18nInstance() {
  return i18n;
}

const getSupportLanguages = (): string[] => {
  return SUPPORTED_LANGS.map((v) => v.value);
};

const searchSupportLanguage = (lang: string) => {
  const findLang = getSupportLanguages().find((v) => v.includes(toStandardLang(lang)));
  if (findLang) return findLang;
  return 'en_us';
};

const setLanguage = (lang: string) => {
  lang = toStandardLang(lang);
  localStorage.setItem(LANGUAGE_KEY, lang);
  window.location.reload();
};

const getCurrentLang = (): string => {
  const curLang = String(i18n.global.locale).toLowerCase();
  return searchSupportLanguage(curLang);
};

// Only for first install page
const getInitLanguage = (): string => {
  const curLang = String(i18n.global.locale).toLowerCase();
  const lang = searchSupportLanguage(curLang);
  if (lang !== 'zh_cn' && lang !== 'zh_tw') {
    return 'en_us';
  }
  return lang;
};

const isCN = () => {
  return (
    getCurrentLang() === 'zh_cn' ||
    getCurrentLang() === 'zh_tw' ||
    window.navigator.language.includes('zh')
  );
};

const isEN = () => {
  return getCurrentLang() === 'en_us';
};

const $t = (...args: any[]): string => {
  return (i18n.global.t as Function)(...args);
};
const t = $t;

(window as any).setLang = setLanguage;

export {
  $t,
  getCurrentLang,
  getInitLanguage,
  getSupportLanguages,
  initI18n,
  isCN,
  isEN,
  searchSupportLanguage,
  setLanguage,
  t
};
