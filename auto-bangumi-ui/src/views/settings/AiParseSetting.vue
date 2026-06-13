<script lang="ts" setup>
import { message } from 'ant-design-vue';
import { updateConfig } from '@/api/modules/user.ts';
import type { UserConfig } from '@/api/types/user.ts';
import { AI_MODEL_MAP, type DictOptions } from '@/types/dict.ts';
import { t } from '@/lang/i18n.ts';

const props = defineProps<{
  config: UserConfig;
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: 'update'): void;
}>();

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
      {{ t('TXT_CODE_b6a1c2f3') }}
    </a-typography-title>
    <div style="text-align: left">
      <a-form :model="config.aiParseSetting" layout="vertical">
        <a-form-item name="enabled">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_c4d5e6f7') }}
          </a-typography-title>
          <a-typography-paragraph>
            <p>
              {{ t('TXT_CODE_g8h9i0j1') }}
            </p>
          </a-typography-paragraph>
          <a-switch
            v-model:checked="config.aiParseSetting.enabled"
            :checked-children="t('TXT_CODE_DICT_YES')"
            :checked-value="true"
            :un-checked-children="t('TXT_CODE_DICT_NO')"
            :un-checked-value="false"
          />
        </a-form-item>
        <a-form-item name="provider">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_k2l3m4n5') }}
          </a-typography-title>
          <a-select v-model:value="config.aiParseSetting.provider" style="width: 100%">
            <a-select-option
              v-for="[key, item] in Object.entries(AI_MODEL_MAP) as [string, DictOptions][]"
              :key="key"
              :title="item.text"
              :value="key"
            >
              {{ item.text }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="apiKey">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_o6p7q8r9') }}
          </a-typography-title>
          <a-input-password v-model:value="config.aiParseSetting.apiKey" :placeholder="t('TXT_CODE_o6p7q8r9')" allowClear />
        </a-form-item>
        <a-form-item name="model">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_s1t2u3v4') }}
          </a-typography-title>
          <a-typography-paragraph>
            <p>
              {{ t('TXT_CODE_w5x6y7z8') }}
            </p>
          </a-typography-paragraph>
          <a-input v-model:value="config.aiParseSetting.model" allowClear placeholder="deepseek-v4-flash" />
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
