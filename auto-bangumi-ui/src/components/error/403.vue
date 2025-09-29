<script lang="ts" setup>
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import CardPanel from '@/components/CardPanel.vue';

</script>

<template>
  <card-panel>
    <template #title>页面错误</template>
    <template #body>
      <div class="flex justify-center align-center h-100">
        <div>
          <a-typography-paragraph>
            <ExclamationCircleOutlined />
            页面运行错误，请刷新网页重试
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
