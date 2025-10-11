<script setup lang="ts">
import { useUserStore } from '@/stores/modules/user.ts';
import type { FormInstance } from 'ant-design-vue';
import { Input, message, Modal } from 'ant-design-vue';
import { h, reactive, ref } from 'vue';
import { createRssManage, findRssManageDetail, updateRssManage } from '@/api/modules/rssManage.ts';
import type { RSS, RssManage } from '@/api/types/rss/rssManage.ts';
import { getRandomId } from '@/utils/randId.ts';
import { analysisMikan } from '@/api/modules/analysis.ts';
import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  ClearOutlined,
  PlusOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue';
import { type DictOptions, RSS_TYPE_MAP, WEEK_MAP } from '@/types/dict.ts';
import { useScreen } from '@/hooks/useScreen.ts';
import { t } from '@/config/lang/i18n.ts';

const { isPhone } = useScreen();
//region type

//endregion

//region props & emit
const labelCol = { span: 3 };
const wrapperCol = { span: 21 };
const emit = defineEmits<{
  (e: 'success', val: void): void;
}>();
//endregion

//region refs & data

const userStore = useUserStore();

const visible = ref<boolean>(false);

const loading = ref<boolean>(false);

const dialogTitle = ref<string>(t('TXT_CODE_84c8197b'));

const ruleFormRef = ref<FormInstance>();

const rules = reactive({
  officialTitle: [{ required: true, message: t('TXT_CODE_b5e7f231') }],
  season: [{ required: true, message: t('TXT_CODE_dac8a2e5') }],
  sendDate: [{ required: true, message: t('TXT_CODE_d7cf7d70') }],
  status: [{ required: true, message: t('TXT_CODE_6f5546c4') }],
  complete: [{ required: true, message: t('TXT_CODE_a5917239') }],
  updateWeek: [{ required: true, message: t('TXT_CODE_925d58b0') }]
});

const rssRules = reactive({
  rss: [{ required: true, message: t('TXT_CODE_43df94e7') }],
  translationGroup: [{ required: true, message: t('TXT_CODE_9fe7189c') }],
  subGroupId: [{ required: true, message: t('TXT_CODE_4427cb2f') }],
  status: [{ required: true, message: t('TXT_CODE_6f5546c4') }],
  type: [{ required: true, message: t('TXT_CODE_47fcc538') }]
});

const paramsProps = ref<RssManage>({
  id: undefined,
  officialTitle: '',
  season: '1',
  status: '1',
  filter: [],
  posterLink: '',
  savePath: '',
  complete: '0',
  updateWeek: 1,
  sendDate: '',
  rssList: [],
  config: {
    latestEpisode: '0',
    totalEpisode: '0'
  }
});
//endregion

//region computed

//endregion

//region watch

//endregion

//region methods
const acceptParams = async (title: string, rssManageId?: number) => {
  visible.value = true;
  dialogTitle.value = title;
  paramsProps.value = {
    id: undefined,
    officialTitle: '',
    season: '1',
    status: '1',
    filter: [...(userStore.userInfo.config?.filterSetting?.filterReg ?? [])],
    posterLink: '',
    savePath: '',
    complete: '0',
    updateWeek: 1,
    sendDate: '',
    rssList: [],
    config: {
      latestEpisode: '0',
      totalEpisode: '0'
    }
  };
  if (rssManageId) {
    loading.value = true;
    const { data } = await findRssManageDetail(rssManageId);
    loading.value = false;
    paramsProps.value = {
      id: data.id,
      officialTitle: data.officialTitle,
      season: data.season,
      status: data.status,
      filter: data.filter ? [...data.filter] : [],
      posterLink: data.posterLink,
      savePath: data.savePath,
      complete: data.complete,
      updateWeek: data.updateWeek,
      sendDate: data.sendDate,
      rssList: data.rssList.map(item => ({ ...item, id: getRandomId() })) || [],
      config: data.config
    };
  }
  if (!rssManageId) {
    addRss();
  }
};

const addRss = () => {
  paramsProps.value.rssList?.push({
    id: getRandomId(),
    rss: '',
    translationGroup: '',
    sort: paramsProps.value.rssList.length,
    status: '1',
    type: 'Mikan',
    subGroupId: ''
  });
};

const delRss = (index: number) => {
  paramsProps.value.rssList?.splice(index, 1);
};

const upLevel = (item: RSS) => {
  if ((item.sort ?? 0) <= 0) return;
  const currentSort = item.sort ?? 0;
  const target = paramsProps.value?.rssList?.find(r => r.sort === currentSort - 1);
  if (target) {
    target.sort = currentSort;
    item.sort = currentSort - 1;
  }
  resetSort();
};

const downLevel = (item: RSS) => {
  const maxSort = paramsProps.value?.rssList.length - 1;
  const currentSort = item.sort ?? 0;
  if (currentSort >= maxSort) return;
  const target = paramsProps.value?.rssList?.find(r => r.sort === currentSort + 1);
  if (target) {
    target.sort = currentSort;
    item.sort = currentSort + 1;
  }
  resetSort();
};

// 重新编号，保证 sort 连续从 0 开始
const resetSort = () => {
  paramsProps.value?.rssList
    ?.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    .forEach((item, index) => {
      item.sort = index;
    });
};

const handleSubmit = () => {
  loading.value = true;
  ruleFormRef.value?.validate()
    .then(async () => {
      let id = paramsProps.value.id;
      if (id) {
        await updateRssManage(paramsProps.value);
        visible.value = false;
        message.success(t('TXT_CODE_20ceb846'));
        emit('success');
      }
      if (!id) {
        await createRssManage(paramsProps.value);
        visible.value = false;
        message.success(t('TXT_CODE_17fc6ee1'));
        emit('success');
      }
    })
    .catch((error: string) => {
      console.error(error);
    })
    .finally(() => {
      loading.value = false;
    });
};

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
      if (!paramsProps.value.filter?.includes(inputVal.value)) {
        paramsProps.value.filter?.push(inputVal.value);
      }
    }
  });
};

const delFilter = (index: number) => {
  paramsProps.value.filter?.splice(index, 1);
};

const analysisRss = async (rss: RSS) => {
  switch (rss.type) {
    case 'Mikan':
      loading.value = true;
      try {
        const { data } = await analysisMikan(rss.rss);
        rss.subGroupId = data.subGroupId;
        rss.translationGroup = data.translationGroup;
        paramsProps.value.officialTitle = data.title;
        paramsProps.value.sendDate = data.sendData;
        paramsProps.value.posterLink = data.posterLink;
        paramsProps.value.updateWeek = data.updateWeek;
        paramsProps.value.season = data.season;
      } finally {
        loading.value = false;
      }
      break;
  }
};
//endregion

//region otherMethods

defineOptions({ name: 'RssManageForm' });

defineExpose({
  acceptParams
});
//endregion

</script>

<template>
  <a-drawer
    v-model:open="visible"
    :title="dialogTitle"
    :mask-closable="false"
    destroy-on-close
    :width="isPhone?'100%':'80%'"
  >
    <a-spin :spinning="loading">
      <a-form
        ref="ruleFormRef"
        :model="paramsProps"
        :rules="rules"
        labelAlign="right"
        :labelCol="labelCol"
        :wrapperCol="wrapperCol"
      >
        <a-form-item :label="t('TXT_CODE_b992ba89')" name="officialTitle">
          <a-input v-model:value="paramsProps.officialTitle" :placeholder="t('TXT_CODE_b5e7f231')"
                   allowClear />
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_70a588d7')" name="season">
          <template #tooltip>
            <a-tooltip placement="top" :title="t('TXT_CODE_4d31db07')">
              <QuestionCircleOutlined />
            </a-tooltip>
          </template>
          <a-input
            v-model:value="paramsProps.season" allowClear />
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_708351ce')" name="status">
          <a-switch
            v-model:checked="paramsProps.status"
            checkedValue="1"
            unCheckedValue="0"
            :checked-children="t('TXT_CODE_DICT_YES')"
            :un-checked-children="t('TXT_CODE_DICT_NO')"
          />
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_dea81dc5')" name="filter">
          <a-button type="primary" @click="addFilter">
            <template #icon>
              <PlusOutlined />
            </template>
            {{ t('TXT_CODE_ff0e598f') }}
          </a-button>
          <div style="margin-top: 8px">
            <a-tag
              v-for="(item, index) in paramsProps.filter"
              :key="item"
              closable
              @close="delFilter(index)"
            >
              {{ item }}
            </a-tag>
          </div>
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_196c9daa')" name="savePath">
          <a-input v-model:value="paramsProps.savePath" :placeholder="t('TXT_CODE_fece9cd9')"
                   allowClear />
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_8f72f0f1')" name="complete">
          <a-switch
            v-model:checked="paramsProps.complete"
            checkedValue="1"
            unCheckedValue="0"
            :checked-children="t('TXT_CODE_DICT_YES')"
            :un-checked-children="t('TXT_CODE_DICT_NO')"
          />
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_eefeb8c4')" name="updateWeek">
          <a-select v-model:value="paramsProps.updateWeek" allowClear
                    :placeholder="t('TXT_CODE_eefeb8c4')">
            <a-select-option
              v-for="([key,item]) in Object.entries(WEEK_MAP) as [string, DictOptions][]"
              :key="key"
              :value="Number(key)"
              :title="item.text"
            >
              {{ item.text }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_b1ffe778')" name="sendDate">
          <a-input v-model:value="paramsProps.sendDate" :placeholder="t('TXT_CODE_d7cf7d70')"
                   allowClear />
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_7df39b03')" name="config.totalEpisode">
          <a-input v-model:value="paramsProps.config.totalEpisode"
                   :placeholder="t('TXT_CODE_7df39b03')"
                   allowClear />
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_18ad2c30')" name="config.latestEpisode">
          <a-input v-model:value="paramsProps.config.latestEpisode"
                   :placeholder="t('TXT_CODE_18ad2c30')"
                   allowClear />
        </a-form-item>

        <a-form-item :label="t('TXT_CODE_f032059f')">
          <a-button type="primary" @click="addRss">
            <template #icon>
              <PlusOutlined />
            </template>
            {{ t('TXT_CODE_efdf57c1') }}
          </a-button>
        </a-form-item>
      </a-form>
      <!-- rssList 子表单 -->
      <a-space direction="vertical" :size="5" style="width: 100%">
        <a-row class="rss-list-container" :gutter="[8,8]"
               justify="start" align="middle"
               v-for="(rss, index) in paramsProps.rssList" :key="index">
          <a-col :xs="24" :sm="24" :md="3">
            <div :style="{textAlign:isPhone?'left':'right'}">
              {{ t('TXT_CODE_e7a601ef', { num: index + 1 }) }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="21">
            <a-form :model="rss"
                    class="rss-items-form"
                    :rules="rssRules"
                    :layout="isPhone?'vertical':'inline'"
            >
              <a-form-item name="rss">
                <a-tooltip>
                  <template #title>{{ rss.rss || t('TXT_CODE_2c1fca48') }}</template>
                  <a-input v-model:value="rss.rss" @change="analysisRss(rss)"
                           :placeholder="t('TXT_CODE_43df94e7')"
                           allowClear />
                </a-tooltip>
              </a-form-item>
              <a-form-item name="subGroupId">
                <a-input v-model:value="rss.subGroupId" :placeholder="t('TXT_CODE_b9a788e3')"
                         allowClear />
              </a-form-item>
              <a-form-item name="translationGroup">
                <a-input v-model:value="rss.translationGroup" :placeholder="t('TXT_CODE_9fe7189c')"
                         allowClear />
              </a-form-item>
              <a-form-item name="status">
                <a-switch
                  v-model:checked="rss.status"
                  checkedValue="1"
                  unCheckedValue="0"
                  :checked-children="t('TXT_CODE_389ded38')"
                  :un-checked-children="t('TXT_CODE_51d131da')"
                />
              </a-form-item>
              <a-form-item name="type">
                <a-select v-model:value="rss.type" style="width: 100%" :placeholder="t('TXT_CODE_9d565011')">
                  <a-select-option
                    v-for="([key,item]) in Object.entries(RSS_TYPE_MAP) as [string, DictOptions][]"
                    :key="key"
                    :value="key"
                    :title="item.text"
                  >
                    {{ item.text }}
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item style="flex: 1">
                <a-flex gap="small" wrap="wrap">
                  <a-button v-show="rss.sort > 0 && paramsProps.rssList.length >=2" type="primary"
                            @click="upLevel(rss)">
                    <template #icon>
                      <ArrowUpOutlined />
                    </template>
                  </a-button>
                  <a-button v-show="paramsProps.rssList.length >=2" type="default"
                            @click="downLevel(rss)">
                    <template #icon>
                      <ArrowDownOutlined />
                    </template>
                  </a-button>
                  <a-button danger @click="delRss(index)">
                    <template #icon>
                      <ClearOutlined />
                    </template>
                  </a-button>
                </a-flex>
              </a-form-item>
            </a-form>
          </a-col>
        </a-row>
      </a-space>
    </a-spin>
    <template #extra>
      <a-button type="primary" @click="handleSubmit">{{ t('TXT_CODE_58b814f8') }}</a-button>
    </template>
  </a-drawer>
</template>

<style scoped lang="scss">
.rss-list-container {
  background-color: var(--color-gray-5);
  padding: 5px;
  border-radius: 5px;
}

.rss-items-form {
  .ant-form-item:last-of-type {
    flex: 1;
  }
}

.rss-items-form.ant-form-vertical {
  .ant-form-item {
    margin-bottom: 5px;
  }
}

.rss-items-form.ant-form-inline {
  .ant-form-item {
    margin-bottom: 2px;
  }
}
</style>
