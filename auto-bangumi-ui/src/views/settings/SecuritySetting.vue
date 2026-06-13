<script lang="ts" setup>
import { h, reactive, ref } from 'vue';
import { type FormInstance, Modal } from 'ant-design-vue';
import { updateLoginInfo } from '@/api/modules/user.ts';
import { useUserStore } from '@/stores/modules/user.ts';
import { deepCopy } from '@/utils';
import type { LoginDTO } from '@/api/types';
import { t } from '@/lang/i18n.ts';

const userStore = useUserStore();

type UpdateUserInfo = LoginDTO & {
  newPwdConfirm?: string;
};

const securityRules = reactive({
  username: [{ required: true, message: t('TXT_CODE_5e81d097') }],
  password: [{ required: true, message: t('TXT_CODE_9e3efb9f') }],
  newPwdConfirm: [{ required: true, message: t('TXT_CODE_313a25e2') }]
});
const securityFormData = ref<UpdateUserInfo>({
  username: deepCopy(userStore.userInfo.username) || '',
  password: '',
  newPwdConfirm: ''
});
const securityRuleFormRef = ref<FormInstance>();
const loading = ref<boolean>(false);

const handleSubmitPassword = () => {
  securityRuleFormRef.value?.validate().then(async () => {
    try {
      loading.value = true;
      await updateLoginInfo(securityFormData.value);
      userStore.clear();

      Modal.success({
        title: t('TXT_CODE_e349fb9a'),
        content: h('div', {}, [h('p', t('TXT_CODE_7ec27326'))]),
        onOk() {
          window.location.href = '/';
        }
      });
    } finally {
      loading.value = false;
      securityRuleFormRef.value?.resetFields();
    }
  });
};
</script>

<template>
  <div class="content-box">
    <a-typography-title :level="4" class="mb-24">
      {{ t('TXT_CODE_6473ffdd') }}
    </a-typography-title>
    <div style="text-align: left">
      <a-form ref="securityRuleFormRef" :model="securityFormData" :rules="securityRules" layout="vertical">
        <a-typography-title :level="5">
          {{ t('TXT_CODE_48360885') }}
        </a-typography-title>
        <a-typography-paragraph>
          <a-typography-text type="secondary">
            {{ t('TXT_CODE_0164b7f1') }}
            <a-tag>JAR</a-tag>
            {{ t('TXT_CODE_28b324f1') }}
            <a-tag>--reload</a-tag>
            {{ t('TXT_CODE_29f457f5') }}
            <br />
            {{ t('TXT_CODE_15f2d98f') }}
            <a-tag>admin</a-tag>
            {{ t('TXT_CODE_b391b4b3') }}
            <a-tag>adminadmin</a-tag>
          </a-typography-text>
        </a-typography-paragraph>
        <a-form-item name="username">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_72f1dfa8') }}
          </a-typography-title>
          <a-input v-model:value="securityFormData.username" :placeholder="t('TXT_CODE_5e81d097')" allowClear />
        </a-form-item>
        <a-form-item name="password">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_e771fa8a') }}
          </a-typography-title>
          <a-input v-model:value="securityFormData.password" :placeholder="t('TXT_CODE_9e3efb9f')" allowClear />
        </a-form-item>
        <a-form-item name="newPwdConfirm">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_579ef006') }}
          </a-typography-title>
          <a-input v-model:value="securityFormData.newPwdConfirm" :placeholder="t('TXT_CODE_579ef006')" allowClear />
        </a-form-item>
        <div class="button">
          <a-button :loading="loading" type="primary" @click="handleSubmitPassword">
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
