<script setup lang="ts">
import AppHeader from '@/Layout/AppHeader.vue';
import { theme } from 'ant-design-vue';
import { THEME, useAppStore } from '@/stores/modules/app';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import { computed } from 'vue';
import LayoutMain from '@/Layout/LayoutMain.vue';

const useApp = useAppStore();

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

const assemblySize = computed(() => useApp.assemblySize);
</script>

<template>
  <a-config-provider :component-size="assemblySize" :theme="appTheme" :locale="zhCN">
    <div class="global-app-container">
      <app-header />
      <LayoutMain />
    </div>
  </a-config-provider>
</template>
<style lang="scss" scoped>
@use "@/assets/global.scss";
</style>
