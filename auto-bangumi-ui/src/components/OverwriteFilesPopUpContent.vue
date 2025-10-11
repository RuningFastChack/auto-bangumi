<script setup lang="ts">
import { computed } from 'vue';
import { t } from '@/lang/i18n.ts';

const props = defineProps<{
  count?: number;
  fileName: string;
  all?: boolean;
  overwrite: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:all', value: boolean): void;
  (e: 'update:overwrite', value: boolean): void;
}>();

const overwriteRef = computed({
  get: () => props.overwrite,
  set: (value: boolean) => emit('update:overwrite', value)
});
const allRef = computed({
  get: () => props.all,
  set: (value: boolean) => emit('update:all', value)
});
</script>

<template>
  <div class="flex-col">
    {{ t('TXT_CODE_1049cbd1', { name: props.fileName }) }}
    <div style="margin-top: 16px; margin-bottom: -8px">
      <a-checkbox v-model:checked="overwriteRef">
        {{ t('TXT_CODE_10693964') }}
      </a-checkbox>
      <a-checkbox
        v-if="props.count && props.count > 1"
        v-model:checked="allRef"
        style="margin-left: 5px"
      >
        {{ t('TXT_CODE_107695d', { num: props.count - 1 }) }}
      </a-checkbox>
    </div>
  </div>
</template>

<style scoped lang="scss"></style>
