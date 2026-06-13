<script lang="ts" setup>
import { reactive, ref } from 'vue';
import { type FormInstance, message } from 'ant-design-vue';
import { updateConfig } from '@/api/modules/user.ts';
import type { UserConfig } from '@/api/types/user.ts';
import { type DictOptions, DOWN_UTIL_MAP } from '@/types/dict.ts';
import { t } from '@/lang/i18n.ts';

const props = defineProps<{
  config: UserConfig;
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: 'update'): void;
}>();

const systemDownLoadSettingRules = reactive({
  url: [{ required: true, message: t('TXT_CODE_977d5a70') }],
  username: [{ required: true, message: t('TXT_CODE_72f1dfa8') }],
  password: [{ required: true, message: t('TXT_CODE_aac3ac9d') }],
  savePath: [{ required: true, message: t('TXT_CODE_196c9daa') }]
});
const systemDownLoadSettingRef = ref<FormInstance>();

const handleSubmit = () => {
  systemDownLoadSettingRef.value?.validate().then(async () => {
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
      {{ t('TXT_CODE_ebf8d5c7') }}
    </a-typography-title>
    <a-typography-paragraph>
      <p>
        {{ t('TXT_CODE_dda1acd0') }}
      </p>
    </a-typography-paragraph>
    <a-typography-title :level="5">
      {{ t('TXT_CODE_af220178') }}
    </a-typography-title>
    <div class="pb-4 flex">
      <a-select v-model:value="config.downLoadSetting.utilEnum" :placeholder="t('TXT_CODE_af220178')" style="width: 100%">
        <a-select-option
          v-for="[key, item] in Object.entries(DOWN_UTIL_MAP) as [string, DictOptions][]"
          :key="key"
          :title="item.text"
          :value="key"
        >
          {{ item.text }}
        </a-select-option>
      </a-select>
    </div>
    <div style="text-align: left">
      <a-form
        ref="systemDownLoadSettingRef"
        :model="config.downLoadSetting"
        :rules="systemDownLoadSettingRules"
        layout="vertical"
      >
        <a-form-item name="url">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_977d5a70') }}
          </a-typography-title>
          <a-input v-model:value="config.downLoadSetting.url" :placeholder="t('TXT_CODE_977d5a70')" allowClear />
        </a-form-item>
        <a-form-item name="username">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_72f1dfa8') }}
          </a-typography-title>
          <a-input v-model:value="config.downLoadSetting.username" :placeholder="t('TXT_CODE_72f1dfa8')" allowClear />
        </a-form-item>
        <a-form-item name="password">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_aac3ac9d') }}
          </a-typography-title>
          <a-input v-model:value="config.downLoadSetting.password" :placeholder="t('TXT_CODE_aac3ac9d')" allowClear />
        </a-form-item>
        <a-form-item name="savePath">
          <a-typography-title :level="5">
            {{ t('TXT_CODE_196c9daa') }}
          </a-typography-title>
          <a-input v-model:value="config.downLoadSetting.savePath" :placeholder="t('TXT_CODE_196c9daa')" allowClear />
        </a-form-item>
        <a-form-item name="ssl">
          <a-typography-title :level="5"> SSL </a-typography-title>
          <a-typography-paragraph>
            <p>
              {{ t('TXT_CODE_c6db1586') }}
              <a-tag>SSL</a-tag>
            </p>
          </a-typography-paragraph>
          <a-switch
            v-model:checked="config.downLoadSetting.ssl"
            :checked-children="t('TXT_CODE_DICT_YES')"
            :checked-value="true"
            :un-checked-children="t('TXT_CODE_DICT_NO')"
            :un-checked-value="false"
            disabled
          />
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
