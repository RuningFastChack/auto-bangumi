<script setup lang="ts">
import {arrayFilter} from '@/utils/array.ts';
import {type FormInstance, Input, message, Modal} from 'ant-design-vue';

import {
  BarsOutlined,
  BulbOutlined,
  ControlOutlined,
  DashboardOutlined,
  LayoutOutlined,
  LockOutlined,
  MediumOutlined,
  PlusOutlined,
  QuestionCircleOutlined,
  SettingOutlined
} from '@ant-design/icons-vue';
import CardPanel from '@/components/CardPanel.vue';
import LeftMenusPanel from '@/components/LeftMenusPanel.vue';
import {h, onMounted, reactive, ref} from 'vue';
import {findUserConfig, updateConfig, updateLoginInfo} from '@/api/modules/user.ts';
import {useUserStore} from '@/stores/modules/user.ts';
import {deepCopy} from '@/utils';
import type {UserConfig} from '@/api/types/user.ts';
import {type DictOptions, DOWN_UTIL_MAP} from '@/types/dict.ts';
import type {LoginDTO} from '@/api/types';
import {t} from '@/lang/i18n.ts';
//region type
let userStore = useUserStore();

type UpdateUserInfo = LoginDTO & {
  newPwdConfirm?: string
}

//endregion

//region props & emit

//endregion

//region refs & data

const loading = ref<boolean>(false);

const leftMenusPanelRef = ref<InstanceType<typeof LeftMenusPanel>>();

const menus = arrayFilter([
  { title: t('TXT_CODE_9684a760'), key: 'setting', icon: SettingOutlined },
  { title: t('TXT_CODE_dea81dc5'), key: 'rules', icon: BarsOutlined },
  { title: t('TXT_CODE_ebf8d5c7'), key: 'downloader', icon: ControlOutlined },
  { title: t('TXT_CODE_61448c38'), key: 'mcsManage', icon: MediumOutlined },
  { title: t('TXT_CODE_6473ffdd'), key: 'security', icon: LockOutlined },
  { title: t('TXT_CODE_1deef431'), key: 'about', icon: QuestionCircleOutlined }
]);

const aboutLinks = arrayFilter([
  {
    title: 'AutoBangumi',
    icon: BulbOutlined,
    url: 'https://github.com/EstrellaXD/Auto_Bangumi'
  },
  {
    title: 'MCSManage',
    icon: LayoutOutlined,
    url: 'https://github.com/MCSManager/MCSManager'
  },
  {
    title: 'Sz-Admin',
    icon: DashboardOutlined,
    url: 'https://github.com/feiyuchuixue/sz-admin'
  }
]);

const ApacheLicense = `
                           Apache License
                      Version 2.0, January 2004
                  http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."

      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or
          Derivative Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.

      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   APPENDIX: How to apply the Apache License to your work.

      To apply the Apache License to your work, attach the following
      boilerplate notice, with the fields enclosed by brackets "[]"
      replaced with your own identifying information. (Don't include
      the brackets!)  The text should be enclosed in the appropriate
      comment syntax for the file format. We also recommend that a
      file or class name and description of purpose be included on the
      same "printed page" as the copyright notice for easier
      identification within third-party archives.

   Copyright [2025] [AutoBangumi]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
`;

const savePathDesc = t('TXT_CODE_17167b7a');
const episodeReNameRuleDesc = t('TXT_CODE_bd3ae424');

const OriginalIntention = t('TXT_CODE_0f6bc6ed');

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
  }
});

const systemDownLoadSettingRules = reactive({
  url: [{ required: true, message: t('TXT_CODE_977d5a70') }],
  username: [{ required: true, message: t('TXT_CODE_72f1dfa8') }],
  password: [{ required: true, message: t('TXT_CODE_aac3ac9d') }],
  savePath: [{ required: true, message: t('TXT_CODE_196c9daa') }]
});
const systemDownLoadSettingRef = ref<FormInstance>();

const mcsManageRules = reactive({
  url: [{ required: true, message: t('TXT_CODE_4af78325') }],
  mcsManageKey: [{ required: true, message: t('TXT_CODE_a559c48a') }],
  daemonId: [{ required: true, message: t('TXT_CODE_200b9b9f') }],
  instanceId: [{ required: true, message: t('TXT_CODE_0ee7f317') }]
});
const mcsManageRuleFormRef = ref<FormInstance>();
//endregion

//region computed

//endregion

//region watch

//endregion

//region methods

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
    onOk: async () => {
      if (!systemConfigFormData.value.filterSetting?.filterReg?.includes(inputVal.value)) {
        systemConfigFormData.value.filterSetting?.filterReg?.push(inputVal.value);
        await handleSubmitFilterSetting();
      }
    }
  });
};

const delFilter = async (index: number) => {
  systemConfigFormData.value.filterSetting?.filterReg?.splice(index, 1);
  await handleSubmitFilterSetting();
};

const changeMenus = () => {
};

const initUserConfig = async () => {
  loading.value = true;
  try {
    const { data } = await findUserConfig();
    Object.assign(systemConfigFormData.value, data || {});
    userStore.updateUserInfo({ config: data });
  } finally {
    loading.value = false;
  }
};

const handleSubmitDownLoadSetting = () => {
  systemDownLoadSettingRef.value?.validate()
    .then(async () => {
      loading.value = true;
      try {
        await updateConfig(systemConfigFormData.value);
        await initUserConfig();
        message.success(t('TXT_CODE_10194e6a'));
      } finally {
        loading.value = false;
      }
    });
};

const handleSubmitMcsManageSetting = () => {
  mcsManageRuleFormRef.value?.validate()
    .then(async () => {
      loading.value = true;
      try {
        await updateConfig(systemConfigFormData.value);
        await initUserConfig();
        message.success(t('TXT_CODE_10194e6a'));
      } finally {
        loading.value = false;
      }
    });
};

const handleSubmitPassword = () => {
  securityRuleFormRef.value?.validate()
    .then(async () => {
      try {
        loading.value = true;
        await updateLoginInfo(securityFormData.value);
        userStore.clear();

        Modal.success({
          title: t('TXT_CODE_e349fb9a'),
          content: h('div', {}, [
            h('p', t('TXT_CODE_7ec27326'))
          ]),
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

const handleSubmitGeneralSetting = async () => {
  loading.value = true;
  try {
    await updateConfig(systemConfigFormData.value);
    await initUserConfig();
    message.success(t('TXT_CODE_f5ed98cf'));
  } finally {
    loading.value = false;
  }
};

const handleSubmitFilterSetting = async () => {
  loading.value = true;
  try {
    await updateConfig(systemConfigFormData.value);
    await initUserConfig();
    message.success(t('TXT_CODE_f5ed98cf'));
  } finally {
    loading.value = false;
  }
};


//region otherMethods
defineOptions({ name: 'Settings' });

onMounted(() => initUserConfig());
//endregion

</script>

<template>
  <div>
    <card-panel class="CardWrapper" style="height: 100%" full-height :padding="false">
      <template #body>
        <left-menus-panel ref="leftMenusPanelRef" @change="changeMenus" :menus="menus">
          <template #setting>
            <div class="content-box">
              <a-typography-title :level="4" class="mb-24">
                {{t('TXT_CODE_9684a760')}}
              </a-typography-title>
              <div style="text-align: left">
                <a-form :model="systemConfigFormData.generalSetting"
                        layout="vertical">
                  <a-form-item name="enable">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_466e199a') }}
                    </a-typography-title>
                    <a-typography-paragraph>
                      <p>
                        {{ t('TXT_CODE_98af1455') }}
                      </p>
                    </a-typography-paragraph>
                    <a-switch v-model:checked="systemConfigFormData.generalSetting.enable"
                              @click="handleSubmitGeneralSetting"
                              :checked-value="true"
                              :un-checked-value="false"
                              :checked-children="t('TXT_CODE_DICT_YES')"
                              :un-checked-children="t('TXT_CODE_DICT_NO')" />
                  </a-form-item>
                  <a-form-item name="rssTimeOut">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_13a7199a') }}
                    </a-typography-title>
                    <a-input-number v-model:value="systemConfigFormData.generalSetting.rssTimeOut"
                                    @change.lazy="handleSubmitGeneralSetting"
                                    allowClear />
                  </a-form-item>
                  <a-form-item name="savePathRule">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_1293cc3a') }}
                    </a-typography-title>
                    <a-typography-paragraph>
                      <pre style="font-size: 13px">{{ savePathDesc }}</pre>
                    </a-typography-paragraph>
                    <a-input v-model:value="systemConfigFormData.generalSetting.savePathRule"
                             allowClear/>
                  </a-form-item>
                  <a-form-item name="episodeTitleRule">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_b8f12f7e') }}
                    </a-typography-title>
                    <a-typography-paragraph>
                      <pre style="font-size: 13px">{{ episodeReNameRuleDesc }}</pre>
                    </a-typography-paragraph>
                    <a-input v-model:value="systemConfigFormData.generalSetting.episodeTitleRule"
                             allowClear/>
                  </a-form-item>
                  <a-form-item name="sendingTimeLimit">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_23cfaee9') }}
                    </a-typography-title>
                    <a-input v-model:value="systemConfigFormData.generalSetting.sendingTimeLimit"
                             allowClear/>
                  </a-form-item>
                  <div class="button">
                    <a-button :loading="loading" type="primary"
                              @click="handleSubmitGeneralSetting">
                      {{ t('TXT_CODE_BUTTON_DESC_SAVE') }}
                    </a-button>
                  </div>
                </a-form>
              </div>
            </div>
          </template>
          <template #rules>
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
                <a-form :model="systemConfigFormData.filterSetting"
                        layout="vertical">
                  <a-form-item name="enable">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_a66abdc7') }}
                    </a-typography-title>
                    <a-typography-paragraph>
                      <p>
                        {{ t('TXT_CODE_a19c36ab') }}
                      </p>
                    </a-typography-paragraph>
                    <a-switch v-model:checked="systemConfigFormData.filterSetting.enable"
                              @click="handleSubmitFilterSetting"
                              :checked-value="true"
                              :un-checked-value="false"
                              :checked-children="t('TXT_CODE_DICT_YES')"
                              :un-checked-children="t('TXT_CODE_DICT_NO')" />
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
                      <a-tag
                        v-for="(item, index) in systemConfigFormData.filterSetting.filterReg"
                        :key="index"
                        closable
                        @close="delFilter(index)"
                      >
                        {{ item }}
                      </a-tag>
                    </div>
                  </a-form-item>
                </a-form>
              </div>
            </div>
          </template>
          <template #downloader>
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
                <a-select v-model:value="systemConfigFormData.downLoadSetting.utilEnum"
                          style="width: 100%" :placeholder="t('TXT_CODE_af220178')">
                  <a-select-option
                    v-for="([key,item]) in Object.entries(DOWN_UTIL_MAP) as [string, DictOptions][]"
                    :key="key"
                    :value="key"
                    :title="item.text"
                  >
                    {{ item.text }}
                  </a-select-option>
                </a-select>
              </div>
              <div style="text-align: left">
                <a-form :model="systemConfigFormData.downLoadSetting"
                        :rules="systemDownLoadSettingRules"
                        ref="systemDownLoadSettingRef"
                        layout="vertical">
                  <a-form-item name="url">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_977d5a70') }}
                    </a-typography-title>
                    <a-input v-model:value="systemConfigFormData.downLoadSetting.url"
                             :placeholder="t('TXT_CODE_977d5a70')"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="username">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_72f1dfa8') }}
                    </a-typography-title>
                    <a-input v-model:value="systemConfigFormData.downLoadSetting.username"
                             :placeholder="t('TXT_CODE_72f1dfa8')"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="password">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_aac3ac9d') }}
                    </a-typography-title>
                    <a-input v-model:value="systemConfigFormData.downLoadSetting.password"
                             :placeholder="t('TXT_CODE_aac3ac9d')"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="savePath">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_196c9daa') }}
                    </a-typography-title>
                    <a-input v-model:value="systemConfigFormData.downLoadSetting.savePath"
                             :placeholder="t('TXT_CODE_196c9daa')"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="ssl">
                    <a-typography-title :level="5">
                      SSL
                    </a-typography-title>
                    <a-typography-paragraph>
                      <p>
                        {{ t('TXT_CODE_c6db1586') }}
                        <a-tag>SSL</a-tag>
                      </p>
                    </a-typography-paragraph>
                    <a-switch v-model:checked="systemConfigFormData.downLoadSetting.ssl"
                              disabled
                              :checked-value="true"
                              :un-checked-value="false"
                              :checked-children="t('TXT_CODE_DICT_YES')"
                              :un-checked-children="t('TXT_CODE_DICT_NO')" />
                  </a-form-item>
                  <div class="button">
                    <a-button type="primary" :loading="loading"
                              @click="handleSubmitDownLoadSetting">
                      {{ t('TXT_CODE_BUTTON_DESC_SAVE') }}
                    </a-button>
                  </div>
                </a-form>
              </div>
            </div>
          </template>
          <template #mcsManage>
            <div class="content-box">
              <a-typography-title :level="4" class="mb-24">
                {{ t('TXT_CODE_6b826a0e') }}
              </a-typography-title>
              <div style="text-align: left">
                <a-form :model="systemConfigFormData.mcsManageSetting"
                        :rules="mcsManageRules"
                        ref="mcsManageRuleFormRef"
                        layout="vertical">
                  <a-typography-title :level="5">
                    {{ t('TXT_CODE_bc823422') }}
                  </a-typography-title>
                  <a-typography-paragraph>
                    <a-typography-text type="secondary">
                      <a
                        href="https://docs.mcsmanager.com/zh_cn/apis/api_instance.html#%E5%AE%9E%E4%BE%8B%E8%AF%A6%E6%83%85"
                        target="_blank">
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
                    <a-input v-model:value="systemConfigFormData.mcsManageSetting.url"
                             placeholder="http://localhost:24444"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="mcsManageKey">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_05e1cdd3') }}
                    </a-typography-title>
                    <a-input v-model:value="systemConfigFormData.mcsManageSetting.mcsManageKey"
                             :placeholder="t('TXT_CODE_a559c48a')"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="daemonId">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_bb0a7e2f') }}
                    </a-typography-title>
                    <a-input v-model:value="systemConfigFormData.mcsManageSetting.daemonId"
                             :placeholder="t('TXT_CODE_200b9b9f')"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="instanceId">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_b19b8a1e') }}
                    </a-typography-title>
                    <a-input v-model:value="systemConfigFormData.mcsManageSetting.instanceId"
                             :placeholder="t('TXT_CODE_0ee7f317')"
                             allowClear />
                  </a-form-item>
                  <div class="button">
                    <a-button type="primary" :loading="loading"
                              @click="handleSubmitMcsManageSetting">
                      {{ t('TXT_CODE_BUTTON_DESC_SAVE') }}
                    </a-button>
                  </div>
                </a-form>
              </div>
            </div>
          </template>
          <template #security>
            <div class="content-box">
              <a-typography-title :level="4" class="mb-24">
                {{ t('TXT_CODE_6473ffdd') }}
              </a-typography-title>
              <div style="text-align: left">
                <a-form :model="securityFormData"
                        :rules="securityRules"
                        ref="securityRuleFormRef"
                        layout="vertical">
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
                    <a-input v-model:value="securityFormData.username" :placeholder="t('TXT_CODE_5e81d097')"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="password">
                    <a-typography-title :level="5">
                      {{t('TXT_CODE_e771fa8a')}}
                    </a-typography-title>
                    <a-input v-model:value="securityFormData.password" :placeholder="t('TXT_CODE_9e3efb9f')"
                             allowClear />
                  </a-form-item>
                  <a-form-item name="newPwdConfirm">
                    <a-typography-title :level="5">
                      {{ t('TXT_CODE_579ef006') }}
                    </a-typography-title>
                    <a-input v-model:value="securityFormData.newPwdConfirm" :placeholder="t('TXT_CODE_579ef006')"
                             allowClear />
                  </a-form-item>
                  <div class="button">
                    <a-button type="primary" :loading="loading" @click="handleSubmitPassword">
                      {{ t('TXT_CODE_BUTTON_DESC_SAVE') }}
                    </a-button>
                  </div>
                </a-form>
              </div>
            </div>
          </template>
          <template #about>
            <div class="content-box">
              <a-typography-title :level="4" class="mb-24">
                {{ t('TXT_CODE_1deef431') }}
              </a-typography-title>
              <a-typography-paragraph>
                <p>
                  {{t('TXT_CODE_1b5f5536')}}
                </p>
              </a-typography-paragraph>
              <div class="pb-4 flex">
                <a-flex gap="small" wrap="wrap">
                  <div v-for="item in aboutLinks" :key="item.url">
                    <a :href="item.url" target="_blank">
                      <a-button>
                        <component :is="item.icon" />
                        {{ item.title }}
                      </a-button>
                    </a>
                  </div>
                </a-flex>
              </div>
              <a-typography-paragraph>
                <p>
                  {{ t('TXT_CODE_7cda1b69') }}
                </p>
                <pre style="font-size: 13px">{{ OriginalIntention }}</pre>
              </a-typography-paragraph>
              <a-typography-paragraph>
                <p>
                  {{ t('TXT_CODE_ca3f035a') }}
                </p>
                <pre style="font-size: 13px">{{ ApacheLicense }}</pre>
              </a-typography-paragraph>
            </div>
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

.content-box {
  padding: 16px;
  overflow-y: auto;
}
</style>
