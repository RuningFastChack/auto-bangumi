<script lang="ts" setup>
import { message } from 'ant-design-vue';
import { updateConfig } from '@/api/modules/user.ts';
import type { UserConfig } from '@/api/types/user.ts';
import { type DictOptions, MESSAGE_PUSH_TYPE_MAP, OPEN_CLAW_AUTH_TYPE_MAP } from '@/types/dict.ts';
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
      {{ t('TXT_CODE_MESSAGE_PUSH_SETTING') }}
    </a-typography-title>
    <div style="text-align: left">
      <a-form :model="config.messageConfig" layout="vertical">
        <a-form-item name="enabled">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_MESSAGE_PUSH_ENABLE') }}
          </a-typography-title>
          <a-typography-paragraph>
            <p>
              {{ t('TXT_CODE_MESSAGE_PUSH_ENABLE_DESC') }}
            </p>
          </a-typography-paragraph>
          <a-switch
            v-model:checked="config.messageConfig.enabled"
            :checked-children="t('TXT_CODE_DICT_YES')"
            :checked-value="true"
            :un-checked-children="t('TXT_CODE_DICT_NO')"
            :un-checked-value="false"
          />
        </a-form-item>
        <a-form-item name="pushType">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_MESSAGE_PUSH_TYPE') }}
          </a-typography-title>
          <a-select v-model:value="config.messageConfig.pushType" style="width: 100%">
            <a-select-option
              v-for="[key, item] in Object.entries(MESSAGE_PUSH_TYPE_MAP) as [string, DictOptions][]"
              :key="key"
              :title="item.text"
              :value="key"
            >
              {{ item.text }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <template v-if="config.messageConfig.pushType === 'OPEN_CLAW'">
          <a-form-item name="gatewayUrl">
            <a-typography-title :level="5">
              {{ t('TXT_CODE_MESSAGE_PUSH_API_URL') }}
            </a-typography-title>
            <a-input
              v-model:value="config.messageConfig.config.gatewayUrl"
              allowClear
              placeholder="http://127.0.0.1:38789/tools/invoke"
            />
          </a-form-item>
          <a-form-item name="authType">
            <a-typography-title :level="5">
              {{ t('TXT_CODE_MESSAGE_PUSH_AUTH_TYPE') }}
            </a-typography-title>
            <a-select v-model:value="config.messageConfig.config.authType" style="width: 100%">
              <a-select-option
                v-for="[key, item] in Object.entries(OPEN_CLAW_AUTH_TYPE_MAP) as [string, DictOptions][]"
                :key="key"
                :title="item.text"
                :value="key"
              >
                {{ item.text }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item v-if="config.messageConfig.config.authType === 'PASSWORD'" name="password">
            <a-typography-title :level="5">
              {{ t('TXT_CODE_MESSAGE_PUSH_PASSWORD') }}
            </a-typography-title>
            <a-input-password
              v-model:value="config.messageConfig.config.password"
              :placeholder="t('TXT_CODE_MESSAGE_PUSH_PASSWORD')"
              allowClear
            />
          </a-form-item>
          <a-form-item v-else name="token">
            <a-typography-title :level="5">
              {{ t('TXT_CODE_MESSAGE_PUSH_TOKEN') }}
            </a-typography-title>
            <a-input-password
              v-model:value="config.messageConfig.config.token"
              :placeholder="t('TXT_CODE_MESSAGE_PUSH_TOKEN')"
              allowClear
            />
          </a-form-item>
          <a-form-item name="sessionKey">
            <a-typography-title :level="5">
              {{ t('TXT_CODE_MESSAGE_PUSH_SESSION_KEY') }}
            </a-typography-title>
            <a-input
              v-model:value="config.messageConfig.config.sessionKey"
              allowClear
              placeholder="agent:main:openclaw-weixin:direct:xxx@im.wechat"
            />
          </a-form-item>
          <a-form-item name="delivery">
            <a-typography-title :level="5">
              {{ t('TXT_CODE_MESSAGE_PUSH_DELIVERY') }}
            </a-typography-title>
            <a-typography-paragraph>
              <p>
                {{ t('TXT_CODE_MESSAGE_PUSH_DELIVERY_DESC') }}
              </p>
            </a-typography-paragraph>
            <a-textarea
              v-model:value="config.messageConfig.config.delivery"
              :auto-size="{ minRows: 3, maxRows: 8 }"
              allowClear
              placeholder='{"channel":"wechat","to":""}'
            />
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
