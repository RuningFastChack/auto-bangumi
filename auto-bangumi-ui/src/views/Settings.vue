<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { arrayFilter } from '@/utils/array.ts';
import {
  BarsOutlined,
  BulbOutlined,
  ControlOutlined,
  LockOutlined,
  MediumOutlined,
  QuestionCircleOutlined,
  SettingOutlined
} from '@ant-design/icons-vue';
import CardPanel from '@/components/CardPanel.vue';
import LeftMenusPanel from '@/components/LeftMenusPanel.vue';
import { findUserConfig } from '@/api/modules/user.ts';
import { useUserStore } from '@/stores/modules/user.ts';
import { t } from '@/lang/i18n.ts';

import GeneralSetting from './settings/GeneralSetting.vue';
import FilterSetting from './settings/FilterSetting.vue';
import DownloadSetting from './settings/DownloadSetting.vue';
import McsManageSetting from './settings/McsManageSetting.vue';
import AiParseSetting from './settings/AiParseSetting.vue';
import SecuritySetting from './settings/SecuritySetting.vue';
import AboutSetting from './settings/AboutSetting.vue';

import type { UserConfig } from '@/api/types/user.ts';

//region type
const userStore = useUserStore();

//endregion

//region refs & data
const loading = ref<boolean>(false);
const leftMenusPanelRef = ref<InstanceType<typeof LeftMenusPanel>>();
const systemConfigFormData = ref<UserConfig>({
  mcsManageSetting: {
    url: '',
    mcsManageKey: '',
    instanceId: '',
    daemonId: ''
  },
  generalSetting: {
    savePathRule: '',
    episodeTitleRule: '',
    rssTimeOut: 3600,
    sendingTimeLimit: 1800,
    enable: true
  },
  filterSetting: {
    enable: true,
    filterReg: []
  },
  downLoadSetting: {
    utilEnum: 'QB',
    url: 'http://127.0.0.1:80',
    username: 'admin',
    password: 'adminadmin',
    savePath: '/',
    ssl: false
  },
  aiParseSetting: {
    enabled: false,
    provider: 'DEEPSEEK',
    apiKey: '',
    model: 'qwen3:8b',
    baseUrl: ''
  },
  messageConfig: {
    enabled: false,
    pushType: 'OPEN_CLAW',
    config: {}
  },
  systemInfo: {
    version: '',
    buildTime: ''
  }
});

const menus = arrayFilter([
  { title: t('TXT_CODE_9684a760'), key: 'setting', icon: SettingOutlined },
  { title: t('TXT_CODE_dea81dc5'), key: 'rules', icon: BarsOutlined },
  { title: t('TXT_CODE_ebf8d5c7'), key: 'downloader', icon: ControlOutlined },
  { title: t('TXT_CODE_b6a1c2f3'), key: 'aiParse', icon: BulbOutlined },
  { title: t('TXT_CODE_61448c38'), key: 'mcsManage', icon: MediumOutlined },
  { title: t('TXT_CODE_6473ffdd'), key: 'security', icon: LockOutlined },
  { title: t('TXT_CODE_1deef431'), key: 'about', icon: QuestionCircleOutlined }
]);
//endregion

//region methods
const changeMenus = () => {};

const normalizeOpenClawConfig = (config: Record<string, any> = {}) => {
  const normalized = { ...config };
  if (!normalized.gatewayUrl && normalized.apiUrl) {
    normalized.gatewayUrl = normalized.apiUrl;
  }
  if (!normalized.token && normalized.authorization) {
    normalized.token = normalized.authorization.replace(/^Bearer\s+/i, '');
  }
  if (!normalized.authType) {
    normalized.authType = normalized.password ? 'PASSWORD' : 'TOKEN';
  }
  if (normalized.delivery && typeof normalized.delivery !== 'string') {
    normalized.delivery = JSON.stringify(normalized.delivery, null, 2);
  }
  delete normalized.apiUrl;
  delete normalized.authorization;
  return normalized;
};

const normalizeServerChanConfig = (config: Record<string, any> = {}) => ({
  sendKey: config.sendKey ?? config.sendkey ?? '',
  apiUrl: config.apiUrl ?? 'https://sctapi.ftqq.com/{sendKey}.send',
  title: config.title ?? 'Auto Bangumi'
});

const normalizeMessagePushConfig = (
  pushType: UserConfig['messageConfig']['pushType'] = 'OPEN_CLAW',
  config: Record<string, any> = {}
) => {
  if (pushType === 'SERVER_CHAN') {
    return normalizeServerChanConfig(config);
  }
  return normalizeOpenClawConfig(config);
};

const initUserConfig = async () => {
  loading.value = true;
  try {
    const { data } = await findUserConfig();
    Object.assign(systemConfigFormData.value, data || {});
    const messageConfig = systemConfigFormData.value.messageConfig as UserConfig['messageConfig'] & {
      openClawMessageConfig?: Record<string, any>;
    };
    systemConfigFormData.value.messageConfig = {
      enabled: messageConfig?.enabled ?? false,
      pushType: messageConfig?.pushType ?? 'OPEN_CLAW',
      config: normalizeMessagePushConfig(
        messageConfig?.pushType ?? 'OPEN_CLAW',
        messageConfig?.config ?? messageConfig?.openClawMessageConfig ?? {}
      )
    };
    userStore.updateUserInfo({ config: systemConfigFormData.value });
  } finally {
    loading.value = false;
  }
};

const handleUpdate = async () => {
  await initUserConfig();
};

onMounted(() => initUserConfig());
// eslint-disable-next-line vue/multi-word-component-names
defineOptions({ name: 'Settings' });
//endregion
</script>

<template>
  <div>
    <card-panel class="CardWrapper" style="height: 100%" full-height :padding="false">
      <template #body>
        <left-menus-panel ref="leftMenusPanelRef" @change="changeMenus" :menus="menus">
          <template #setting>
            <GeneralSetting :config="systemConfigFormData" :loading="loading" @update="handleUpdate" />
          </template>
          <template #rules>
            <FilterSetting :config="systemConfigFormData" :loading="loading" @update="handleUpdate" />
          </template>
          <template #downloader>
            <DownloadSetting :config="systemConfigFormData" :loading="loading" @update="handleUpdate" />
          </template>
          <template #aiParse>
            <AiParseSetting :config="systemConfigFormData" :loading="loading" @update="handleUpdate" />
          </template>
          <template #mcsManage>
            <McsManageSetting :config="systemConfigFormData" :loading="loading" @update="handleUpdate" />
          </template>
          <template #security>
            <SecuritySetting />
          </template>
          <template #about>
            <AboutSetting :config="systemConfigFormData" />
          </template>
        </left-menus-panel>
      </template>
    </card-panel>
  </div>
</template>

<style scoped lang="scss">
div {
  height: 100%;
  position: relative;
}
</style>
