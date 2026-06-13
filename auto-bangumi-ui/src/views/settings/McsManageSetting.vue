<script lang="ts" setup>
import { reactive, ref } from 'vue';
import { type FormInstance, message } from 'ant-design-vue';
import { MediumOutlined } from '@ant-design/icons-vue';
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

const mcsManageRules = reactive({
  url: [{ required: true, message: t('TXT_CODE_4af78325') }],
  mcsManageKey: [{ required: true, message: t('TXT_CODE_a559c48a') }],
  daemonId: [{ required: true, message: t('TXT_CODE_200b9b9f') }],
  instanceId: [{ required: true, message: t('TXT_CODE_0ee7f317') }]
});
const mcsManageRuleFormRef = ref<FormInstance>();

const handleSubmit = () => {
  mcsManageRuleFormRef.value?.validate().then(async () => {
    try {
      await updateConfig(props.config);
      emit('update');
      message.success(t('TXT_CODE_10194e6a'));
    } catch (e) {
      // error handled by interceptor
    }
  });
};
</script>

<template>
  <div class="content-box">
    <a-typography-title :level="4" class="mb-24">
      {{ t('TXT_CODE_6b826a0e') }}
    </a-typography-title>
    <div style="text-align: left">
      <a-form ref="mcsManageRuleFormRef" :model="config.mcsManageSetting" :rules="mcsManageRules" layout="vertical">
        <a-typography-title :level="5">
          {{ t('TXT_CODE_bc823422') }}
        </a-typography-title>
        <a-typography-paragraph>
          <a-typography-text type="secondary">
            <a
              href="https://docs.mcsmanager.com/zh_cn/apis/api_instance.html#%E5%AE%9E%E4%BE%8B%E8%AF%A6%E6%83%85"
              target="_blank"
            >
              <a-button>
                <MediumOutlined />
                {{ t('TXT_CODE_af89b8f0') }}
              </a-button>
            </a>
          </a-typography-text>
        </a-typography-paragraph>
        <a-form-item name="mcsManageKey">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_8556fd2c') }}
          </a-typography-title>
          <a-input v-model:value="config.mcsManageSetting.url" allowClear placeholder="http://localhost:24444" />
        </a-form-item>
        <a-form-item name="mcsManageKey">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_05e1cdd3') }}
          </a-typography-title>
          <a-input v-model:value="config.mcsManageSetting.mcsManageKey" :placeholder="t('TXT_CODE_a559c48a')" allowClear />
        </a-form-item>
        <a-form-item name="daemonId">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_bb0a7e2f') }}
          </a-typography-title>
          <a-input v-model:value="config.mcsManageSetting.daemonId" :placeholder="t('TXT_CODE_200b9b9f')" allowClear />
        </a-form-item>
        <a-form-item name="instanceId">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_b19b8a1e') }}
          </a-typography-title>
          <a-input v-model:value="config.mcsManageSetting.instanceId" :placeholder="t('TXT_CODE_0ee7f317')" allowClear />
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
