<script lang="ts" setup>
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import CardPanel from '@/components/CardPanel.vue';
import { t } from '@/lang/i18n.ts';
</script>

<template>
  <card-panel>
    <template #title>{{ t('TXT_CODE_PAGE_403_TITLE') }}</template>
    <template #body>
      <div class="flex justify-center align-center h-100">
        <div>
          <a-typography-paragraph>
            <ExclamationCircleOutlined />
            {{ t('TXT_CODE_PAGE_403_DESCRIPTION') }}
          </a-typography-paragraph>
        </div>
      </div>
    </template>
  </card-panel>
</template>

<style lang="scss" scoped>
.icon {
  text-align: center;
  color: var(--color-gray-7);
}
</style>
