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

    const setTheme = (val: THEME) => {
      isDark.value = val;
      window.location.reload();
    };

    const setSize = (val: SizeType) => {
      assemblySize.value = val;
    };

    return {
      isDark,
      assemblySize,
      setTheme,
      setSize
    };
  },
  {
    persist: piniaPersistConfig('app', ['isDark', 'assemblySize'])
  });
