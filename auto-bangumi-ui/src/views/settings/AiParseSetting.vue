<script lang="ts" setup>
import { message } from 'ant-design-vue';
import { updateConfig } from '@/api/modules/user.ts';
import type { UserConfig } from '@/api/types/user.ts';
import { AI_MODEL_MAP, type DictOptions } from '@/types/dict.ts';
import { t } from '@/lang/i18n.ts';
import { watch } from 'vue';

const props = defineProps<{
  config: UserConfig;
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: 'update'): void;
}>();

// 切换模型供应商时清空不相关的字段
watch(
  () => props.config.aiParseSetting?.provider,
  (newVal, oldVal) => {
    if (!newVal || !oldVal || newVal === oldVal) return;
    props.config.aiParseSetting.baseUrl = '';
    props.config.aiParseSetting.model = '';
    props.config.aiParseSetting.apiKey = '';
  }
);

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
        <template v-if="config.aiParseSetting.provider === 'OLLAMA'">
          <a-form-item name="baseUrl">
            <a-typography-title :level="5">
              {{ t('TXT_CODE_a9b0c1d2') }}
            </a-typography-title>
            <a-typography-paragraph>
              <p>
                {{ t('TXT_CODE_e3f4g5h6') }}
              </p>
            </a-typography-paragraph>
            <a-input v-model:value="config.aiParseSetting.baseUrl" allowClear placeholder="http://localhost:11434/v1" />
          </a-form-item>
          <a-form-item name="model">
            <a-typography-title :level="5">
              {{ t('TXT_CODE_s1t2u3v4') }}
            </a-typography-title>
            <a-input v-model:value="config.aiParseSetting.model" :placeholder="t('TXT_CODE_s1t2u3v4')" allowClear />
          </a-form-item>
        </template>
        <template v-else>
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
            <a-input v-model:value="config.aiParseSetting.model" :placeholder="t('TXT_CODE_s1t2u3v4')" allowClear />
          </a-form-item>
        </template>
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
