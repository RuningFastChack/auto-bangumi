import { defineStore } from 'pinia';
import piniaPersistConfig from '@/stores/helper/persist.ts';
import { ref } from 'vue';

export enum THEME {
  LIGHT = 'light',
  DARK = 'dark'
}

export type SizeType = 'small' | 'middle' | 'large';


export const useAppStore = defineStore(
  'app',
  () => {
    const isDark = ref<THEME>(THEME.LIGHT);

    const assemblySize = ref<SizeType>('small');

    const language = ref<string>('zh_cn');

    const setTheme = (val: THEME) => {
      isDark.value = val;
      window.location.reload();
    };

    const setSize = (val: SizeType) => {
      assemblySize.value = val;
    };

    const setLanguage = (val: string) => {
      language.value = val;
    };

    return {
      isDark,
      assemblySize,
      language,
      setLanguage,
      setTheme,
      setSize
    };
  },
  {
    persist: piniaPersistConfig('app', ['isDark', 'assemblySize','language'])
  });
