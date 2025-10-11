<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useFileManager } from '@/hooks/useFileManager';
import { t } from '@/config/lang/i18n.ts';

const props = defineProps<{
  emitResult: () => void;
  destroyComponent: () => void;
  fileName: string;
  frontDir: string;
}>();

const { getFileLink } = useFileManager();

const isOpen = ref(true);
const imgLink = ref('');
const downloadBtnLoading = ref(false);

const onClose = () => {
  isOpen.value = false;
  props.emitResult();
  props.destroyComponent();
};

const onDownload = async () => {
  downloadBtnLoading.value = true;
  imgLink.value = (await getFileLink(props.fileName, props.frontDir)) || '';
  downloadBtnLoading.value = false;
  window.open(imgLink.value);
};

onMounted(async () => {
  imgLink.value = (await getFileLink(props.fileName, props.frontDir)) || '';
});
</script>

<template>
  <a-modal :open="isOpen" :title="t('TXT_CODE_10088738')" @ok="onClose" @cancel="onClose">
    <div class="image-view">
      <a-spin :spinning="!imgLink">
        <a-image :src="imgLink" />
      </a-spin>
    </div>
    <div class="image-name">
      {{ props.fileName }}
    </div>
    <template #footer>
      <a-button @click="onClose">{{ t('TXT_CODE_BUTTON_DESC_CLOSE') }}</a-button>
      <a-button type="primary" :loading="downloadBtnLoading" @click="onDownload">
        {{ t('TXT_CODE_BUTTON_DESC_DOWNLOAD') }}
      </a-button>
    </template>
  </a-modal>
</template>

<style lang="scss" scoped>
.image-view {
  margin-bottom: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-name {
  text-align: center;
}
</style>
