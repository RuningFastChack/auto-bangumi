<script setup lang="ts">
import { computed, ref } from 'vue';
import { message } from 'ant-design-vue';
import { reportErrorMsg } from '@/utils/validator';
import Editor from '@/components/Editor.vue';
import { useKeyboardEvents } from '@/hooks/useKeyboardEvents';
import { useScreen } from '@/hooks/useScreen';
import { FullscreenExitOutlined, FullscreenOutlined } from '@ant-design/icons-vue';
import { getFileContent, updateFileContent } from '@/api/modules/mcs/files';

const emit = defineEmits(['save']);

const open = ref(false);
const isLoading = ref(false);
const openEditor = ref(false);
const editorText = ref('');
const fileName = ref('');
const path = ref('');

const { isPhone } = useScreen();

// eslint-disable-next-line no-unused-vars
let resolve: (t: string) => void;
// eslint-disable-next-line no-unused-vars
let reject: (e: Error) => void;

const props = defineProps<{
  daemonId: string;
  instanceId: string;
}>();

let useKeyboardEventsHooks: ReturnType<typeof useKeyboardEvents> | null = null;

const initKeydownListener = () => {
  useKeyboardEventsHooks = useKeyboardEvents(
    { ctrl: true, alt: false, caseSensitive: false, key: 's' },
    async () => {
      try {
        await submitRequest();
        message.success('已通过快捷键保存！');
        emit('save');
      } catch (err: any) {
        return reportErrorMsg(err.message);
      }
    }
  );
  useKeyboardEventsHooks.startKeydownListener();
};

const openDialog = (_path: string, _fileName: string) => {
  open.value = true;
  path.value = _path;
  fileName.value = _fileName;
  initKeydownListener();
  return new Promise(async (_resolve, _reject) => {
    await render();
    resolve = _resolve;
    reject = _reject;
  });
};

const fullScreen = ref(false);

const render = async () => {
  try {
    isLoading.value = true;
    const { data } = await getFileContent({ target: path.value });
    editorText.value = data;
    openEditor.value = true;
  } catch (err: any) {
    console.error(err.message);
    return reportErrorMsg(err.message);
  } finally {
    isLoading.value = false;
  }
};

const submitRequest = async () => {
  try {
    isLoading.value = true;
    await updateFileContent({
      target: path.value,
      text: editorText.value
    });
  } finally {
    isLoading.value = false;
  }
};

const submit = async () => {
  try {
    await submitRequest();
    message.success('保存成功');
    cancel();
    resolve(editorText.value);
    emit('save');
  } catch (err: any) {
    console.error(err.message);
    reject(err);
    return reportErrorMsg(err.message);
  }
};

const cancel = async () => {
  useKeyboardEventsHooks?.removeKeydownListener();
  open.value = openEditor.value = false;
  resolve(editorText.value);
};

const dialogTitle = computed(() => {
  return fileName.value;
});

defineExpose({
  openDialog
});
</script>

<template>
  <a-modal
    v-model:open="open"
    centered
    cancel-text="放弃"
    ok-text="保存"
    :mask-closable="false"
    :width="fullScreen ? '100%' : '1600px'"
    :confirm-loading="isLoading"
    @ok="submit"
    @cancel="cancel"
  >
    <template #title>
      {{ dialogTitle }}
      <a-button v-if="!isPhone" type="text" size="small" @click="fullScreen = !fullScreen">
        <template #icon>
          <FullscreenExitOutlined v-if="fullScreen" />
          <FullscreenOutlined v-else />
        </template>
      </a-button>
    </template>
    <Editor
      v-if="openEditor"
      ref="EditorComponent"
      v-model:text="editorText"
      :filename="fileName"
      height="80vh"
    />
    <a-skeleton v-else :paragraph="{ rows: 12 }" active />
  </a-modal>
</template>
