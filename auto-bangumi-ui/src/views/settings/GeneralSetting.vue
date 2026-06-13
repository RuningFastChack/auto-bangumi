<script lang="ts" setup>
import { message } from 'ant-design-vue';
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

const savePathDesc = t('TXT_CODE_17167b7a');
const episodeReNameRuleDesc = t('TXT_CODE_bd3ae424');

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
      {{ t('TXT_CODE_9684a760') }}
    </a-typography-title>
    <div style="text-align: left">
      <a-form :model="config.generalSetting" layout="vertical">
        <a-form-item name="enable">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_466e199a') }}
          </a-typography-title>
          <a-typography-paragraph>
            <p>
              {{ t('TXT_CODE_98af1455') }}
            </p>
          </a-typography-paragraph>
          <a-switch
            v-model:checked="config.generalSetting.enable"
            :checked-children="t('TXT_CODE_DICT_YES')"
            :checked-value="true"
            :un-checked-children="t('TXT_CODE_DICT_NO')"
            :un-checked-value="false"
          />
        </a-form-item>
        <a-form-item name="rssTimeOut">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_13a7199a') }}
          </a-typography-title>
          <a-input-number v-model:value="config.generalSetting.rssTimeOut" allowClear />
        </a-form-item>
        <a-form-item name="savePathRule">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_1293cc3a') }}
          </a-typography-title>
          <a-typography-paragraph>
            <pre style="font-size: 13px">{{ savePathDesc }}</pre>
          </a-typography-paragraph>
          <a-input v-model:value="config.generalSetting.savePathRule" allowClear />
        </a-form-item>
        <a-form-item name="episodeTitleRule">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_b8f12f7e') }}
          </a-typography-title>
          <a-typography-paragraph>
            <pre style="font-size: 13px">{{ episodeReNameRuleDesc }}</pre>
          </a-typography-paragraph>
          <a-input v-model:value="config.generalSetting.episodeTitleRule" allowClear />
        </a-form-item>
        <a-form-item name="sendingTimeLimit">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_23cfaee9') }}
          </a-typography-title>
          <a-input v-model:value="config.generalSetting.sendingTimeLimit" allowClear />
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
