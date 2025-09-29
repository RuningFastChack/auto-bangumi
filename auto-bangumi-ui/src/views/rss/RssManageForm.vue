<script setup lang="ts">
import { useUserStore } from '@/stores/modules/user.ts';
import type { FormInstance } from 'ant-design-vue';
import { Input, message, Modal } from 'ant-design-vue';
import { h, reactive, ref } from 'vue';
import { createRssManage, findRssManageDetail, updateRssManage } from '@/api/modules/rssManage.ts';
import type { RSS, RssManage } from '@/api/types/rss/rssManage.ts';
import { getRandomId } from '@/utils/randId.ts';
import { analysisMikan } from '@/api/modules/analysis.ts';
import { isEmpty } from '@/utils';
import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  ClearOutlined,
  PlusOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue';
import { type DictOptions, RSS_TYPE_MAP, WEEK_MAP } from '@/types/dict.ts';
import { useScreen } from '@/hooks/useScreen.ts';

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

const dialogTitle = ref<'新增' | '修改'>('新增');

const ruleFormRef = ref<FormInstance>();

const rules = reactive({
  officialTitle: [{ required: true, message: '请填写动画标题' }],
  season: [{ required: true, message: '请填写季度' }],
  sendDate: [{ required: true, message: '请填写发布日期' }],
  status: [{ required: true, message: '请选择是否启用' }],
  complete: [{ required: true, message: '请选择是否完结' }],
  updateWeek: [{ required: true, message: '请选择更新星期' }]
});

const rssRules = reactive({
  rss: [{ required: true, message: '请填写订阅链接' }],
  translationGroup: [{ required: true, message: '请填写字幕组' }],
  subGroupId: [{ required: true, message: '请填写字幕组ID' }],
  status: [{ required: true, message: '请选择是否启用' }],
  type: [{ required: true, message: '请选择链接类型' }]
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
  rssList: []
});
//endregion

//region computed

//endregion

//region watch

//endregion

//region methods
const acceptParams = async (title: '新增' | '修改', rssManageId?: number) => {
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
    rssList: []
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
      rssList: data.rssList.map(item => ({ ...item, id: getRandomId() })) || []
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
        message.success('编辑成功');
        emit('success');
      }
      if (!id) {
        await createRssManage(paramsProps.value);
        visible.value = false;
        message.success('登记成功');
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
    title: '添加规则',
    okText: '添加',
    cancelText: '取消',
    content: () =>
      h(Input, {
        placeholder: '请输入规则',
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
        <a-form-item label="动画标题" name="officialTitle">
          <a-input v-model:value="paramsProps.officialTitle" placeholder="请填写动画标题"
                   allowClear />
        </a-form-item>

        <a-form-item label="季度" name="season">
          <template #tooltip>
            <a-tooltip placement="top">
              <template #title>
                <span>建议此处写数字</span>
              </template>
              <QuestionCircleOutlined />
            </a-tooltip>
          </template>
          <a-input
            v-model:value="paramsProps.season" allowClear />
        </a-form-item>

        <a-form-item label="是否启用" name="status">
          <a-switch
            v-model:checked="paramsProps.status"
            checkedValue="1"
            unCheckedValue="0"
            checked-children="是"
            un-checked-children="否"
          />
        </a-form-item>

        <a-form-item label="下载规则" name="filter">
          <a-button type="primary" @click="addFilter">
            <template #icon>
              <PlusOutlined />
            </template>
            添加规则
          </a-button>
          <div style="margin-top: 8px">
            <a-tag
              v-for="(item, index) in paramsProps.filter"
              :key="index"
              closable
              @close="delFilter(index)"
            >
              {{ item }}
            </a-tag>
          </div>
        </a-form-item>

        <a-form-item label="保存路径" name="savePath">
          <a-input v-model:value="paramsProps.savePath" placeholder="请填写保存路径" allowClear />
        </a-form-item>

        <a-form-item label="是否完结" name="complete">
          <a-switch
            v-model:checked="paramsProps.complete"
            checkedValue="1"
            unCheckedValue="0"
            checked-children="是"
            un-checked-children="否"
          />
        </a-form-item>

        <a-form-item label="更新星期" name="updateWeek">
          <a-select v-model:value="paramsProps.updateWeek" allowClear placeholder="更新星期">
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

        <a-form-item label="发布日期" name="sendDate">
          <a-input v-model:value="paramsProps.sendDate" placeholder="请填写发布日期" allowClear />
        </a-form-item>

        <a-form-item label="订阅链接">
          <a-button type="primary" @click="addRss">
            <template #icon>
              <PlusOutlined />
            </template>
            添加链接
          </a-button>
        </a-form-item>
      </a-form>
      <!-- rssList 子表单 -->
      <a-space direction="vertical" :size="5" style="width: 100%">
        <a-row class="rss-list-container" :gutter="[8,8]"
               justify="start" align="middle"
               v-for="(rss, index) in paramsProps.rssList" :key="index">
          <a-col :xs="24" :sm="24" :md="3">
            <div :style="{textAlign:isPhone?'left':'right'}">链接{{ index + 1 }}：</div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="21">
            <a-form :model="rss"
                    class="rss-items-form"
                    :rules="rssRules"
                    :layout="isPhone?'vertical':'inline'"
            >
              <a-form-item name="rss">
                <a-tooltip>
                  <template #title>{{ rss.rss || '尚未填写链接' }}</template>
                  <a-input v-model:value="rss.rss" @change="analysisRss(rss)"
                           placeholder="请填写订阅链接"
                           allowClear />
                </a-tooltip>
              </a-form-item>
              <a-form-item name="subGroupId">
                <a-input v-model:value="rss.subGroupId" placeholder="字幕分组ID" allowClear />
              </a-form-item>
              <a-form-item name="translationGroup">
                <a-input v-model:value="rss.translationGroup" placeholder="请填写字幕组"
                         allowClear />
              </a-form-item>
              <a-form-item name="status">
                <a-switch
                  v-model:checked="rss.status"
                  checkedValue="1"
                  unCheckedValue="0"
                  checked-children="启用"
                  un-checked-children="禁用"
                />
              </a-form-item>
              <a-form-item name="type">
                <a-select v-model:value="rss.type" style="width: 100%" placeholder="链接类型">
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
      <a-button type="primary" @click="handleSubmit">提交</a-button>
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
