<script lang="ts" setup>
import { h, ref } from 'vue';
import { Input, message, Modal } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { updateConfig } from '@/api/modules/user.ts';
import type { UserConfig } from '@/api/types/user.ts';
import { t } from '@/lang/i18n.ts';

const props = defineProps<{
  config: UserConfig;
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: 'update'): void;
}>();

const addFilter = () => {
  const inputVal = ref('');
  Modal.confirm({
    title: t('TXT_CODE_ff0e598f'),
    okText: t('TXT_CODE_585cb161'),
    cancelText: t('TXT_CODE_BUTTON_DESC_CANCEL'),
    content: () =>
      h(Input, {
        placeholder: t('TXT_CODE_a53e1804'),
        value: inputVal.value,
        onInput: (e: any) => {
          inputVal.value = e.target.value;
        }
      }),
    onOk: () => {
      if (!props.config.filterSetting?.filterReg?.includes(inputVal.value)) {
        props.config.filterSetting?.filterReg?.push(inputVal.value);
      }
    }
  });
};

const delFilter = (index: number) => {
  props.config.filterSetting?.filterReg?.splice(index, 1);
};

const handleSubmit = async () => {
  try {
    await updateConfig(props.config);
    emit('update');
    message.success(t('TXT_CODE_f5ed98cf'));
  } catch (e) {
    // error handled by interceptor
  }
};
</script>

<template>
  <div class="content-box">
    <a-typography-title :level="4" class="mb-24">
      {{ t('TXT_CODE_dea81dc5') }}
    </a-typography-title>
    <a-typography-paragraph>
      <p>
        {{ t('TXT_CODE_51ef8fb5') }}
      </p>
    </a-typography-paragraph>
    <div style="text-align: left">
      <a-form :model="config.filterSetting" layout="vertical">
        <a-form-item name="enable">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_a66abdc7') }}
          </a-typography-title>
          <a-typography-paragraph>
            <p>
              {{ t('TXT_CODE_a19c36ab') }}
            </p>
          </a-typography-paragraph>
          <a-switch
            v-model:checked="config.filterSetting.enable"
            :checked-children="t('TXT_CODE_DICT_YES')"
            :checked-value="true"
            :un-checked-children="t('TXT_CODE_DICT_NO')"
            :un-checked-value="false"
          />
        </a-form-item>
        <a-form-item name="filterReg">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_dea81dc5') }}
          </a-typography-title>
          <a-typography-paragraph>
            <p>
              {{ t('TXT_CODE_2ad8d65a') }}
              <br />
              {{ t('TXT_CODE_7b3809e0') }}
              <br />
              {{ t('TXT_CODE_ce53913e') }}
            </p>
          </a-typography-paragraph>
          <a-button type="primary" @click="addFilter">
            <template #icon>
              <PlusOutlined />
            </template>
            {{ t('TXT_CODE_ff0e598f') }}
          </a-button>
          <div style="margin-top: 8px">
            <a-tag v-for="(item, index) in config.filterSetting.filterReg" :key="index" closable @close="delFilter(index)">
              {{ item }}
            </a-tag>
          </div>
        </a-form-item>
        <div class="button">
          <a-button :loading="loading" type="primary" @click="handleSubmit">
            {{ t('TXT_CODE_BUTTON_DESC_SAVE') }}
          </a-button>
        </div>
      </a-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.content-box {
  padding: 16px;
  overflow-y: auto;
}
</style>
