<script setup lang="ts">
import AppHeader from '@/Layout/AppHeader.vue';
import { theme } from 'ant-design-vue';
import { THEME, useAppStore } from '@/stores/modules/app';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import enUS from 'ant-design-vue/es/locale/en_US';
import duration from 'dayjs/plugin/duration';
import 'dayjs/locale/zh-cn';
import 'dayjs/locale/en';
import dayjs from 'dayjs';
import { computed, ref } from 'vue';
import LayoutMain from '@/Layout/LayoutMain.vue';

dayjs.extend(duration);

const useApp = useAppStore();
const locale = ref(enUS);

const appTheme = {
  algorithm: theme.defaultAlgorithm,
  token: {
    fontSizeLG: 14,
    fontSizeSM: 12,
    fontSizeXL: 18
  }
};

if (useApp.isDark === THEME.DARK) {
  appTheme.algorithm = theme.darkAlgorithm;
  document.body.classList.add('app-dark-theme');
} else {
  appTheme.algorithm = theme.defaultAlgorithm;
  document.body.classList.add('app-light-theme');
}

if (useApp.language.toLowerCase() === 'zh_cn') {
  dayjs.locale('zh-cn');
  locale.value = zhCN;
} else {
  dayjs.locale('en-us');
  locale.value = enUS;
}

const assemblySize = computed(() => useApp.assemblySize);
</script>

<template>
  <a-config-provider :component-size="assemblySize" :theme="appTheme" :locale="locale">
    <div class="global-app-container">
      <app-header />
      <LayoutMain />
    </div>
  </a-config-provider>
</template>
<style lang="scss" scoped>
@use "@/assets/global.scss";
</style>
