<script setup lang="ts">
import { computed } from 'vue';

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
    文件 {{ props.fileName }} 已经在目录中存在
    <div style="margin-top: 16px; margin-bottom: -8px">
      <a-checkbox v-model:checked="overwriteRef">
        覆盖
      </a-checkbox>
      <a-checkbox
        v-if="props.count && props.count > 1"
        v-model:checked="allRef"
        style="margin-left: 5px"
      >
        以及之后 {{ props.count - 1 }} 个
      </a-checkbox>
    </div>
  </div>
</template>

<style scoped lang="scss"></style>
