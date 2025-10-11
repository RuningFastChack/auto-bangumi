<script setup lang="ts">
import { computed, type CSSProperties, h, onMounted, ref, watch } from 'vue';
import BetweenMenus from '@/components/BetweenMenus.vue';
import { useScreen } from '@/hooks/useScreen.ts';
import { useRoute } from 'vue-router';
import CardPanel from '@/components/CardPanel.vue';
import { arrayFilter } from '@/utils/array.ts';
import {
  BookOutlined,
  CheckCircleOutlined,
  CloudSyncOutlined,
  DeleteOutlined,
  DownOutlined,
  EditOutlined,
  HourglassOutlined,
  InboxOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
  StopOutlined
} from '@ant-design/icons-vue';
import type { AntColumnsType } from '@/types/ant.ts';
import type { RssManageList, RssManageQuery } from '@/api/types/rss/rssManage.ts';
import {
  changeRssManageStatusAndComplete,
  findRssManagePage,
  refreshPosterApi,
  refreshRssManageByIds,
  removeRssManage
} from '@/api/modules/rssManage.ts';
import type { IPage } from '@/api/types';
import { WEEK_MAP } from '@/types/dict.ts';
import { isEmpty } from '@/utils';
import { sleep } from '@/utils/common.ts';
import RssManageForm from '@/views/rss/RssManageForm.vue';
import { type ItemType, message, Modal } from 'ant-design-vue';
import { triggerPushLastRssItem } from '@/api/modules/rssItem.ts';
import RssItems from '@/views/rss/RssItems.vue';
import { useRightClickMenu } from '@/hooks/useRightClickMenu.ts';
import { t } from '@/config/lang/i18n.ts';
//region type

//endregion

//region props & emit
const { openRightClickMenu } = useRightClickMenu();

const route = useRoute();

const loading = ref<boolean>(false);

const selectedRssManages = ref<number[]>([]);


type SearchSelectType = 'ALL' | 'COMPLETED' | 'UNFINISHED' | 'OPENED' | 'CLOSE'

const SearchSelect = ref<SearchSelectType>('ALL');

const { isPhone } = useScreen();
//endregion

//region refs & data
const queryParams = ref<RssManageQuery>({
  page: 1,
  limit: 10
});

const SearchData = ref<IPage<RssManageList>>({
  current: 1,
  total: 0,
  limit: 10,
  rows: []
});

const rssManageFormRef = ref<InstanceType<typeof RssManageForm>>();
const rssItemRef = ref<InstanceType<typeof RssItems>>();
//endregion

//region computed
const isMultiple = computed(() => selectedRssManages.value && selectedRssManages.value.length > 1);

const columns = computed(() => {
  return arrayFilter<AntColumnsType>([
    {
      align: 'left',
      dataIndex: 'officialTitle',
      key: 'officialTitle',
      ellipsis: true,
      title: t('TXT_CODE_eedbd0f9'),
      width: 180
    },
    {
      align: 'center',
      dataIndex: 'updateWeek',
      key: 'updateWeek',
      title: t('TXT_CODE_eefeb8c4'),
      width: 90
    },
    { align: 'center', dataIndex: 'season', key: 'season', title: '季度', width: 80 },
    {
      align: 'center',
      dataIndex: 'latestEpisode',
      key: 'latestEpisode',
      title: t('TXT_CODE_18ad2c30'),
      width: 80
    },
    {
      align: 'center',
      dataIndex: 'complete',
      key: 'complete',
      title: t('TXT_CODE_8f72f0f1'),
      width: 80
    },
    {
      align: 'center',
      dataIndex: 'status',
      key: 'status',
      title: t('TXT_CODE_708351ce'),
      width: 80
    },
    {
      align: 'center',
      dataIndex: 'totalEpisode',
      key: 'totalEpisode',
      title: t('TXT_CODE_7df39b03'),
      width: 80
    },
    {
      align: 'center',
      dataIndex: 'sendDate',
      key: 'sendDate',
      title: t('TXT_CODE_b1ffe778'),
      width: 120
    },
    {
      align: 'left', dataIndex: 'savePath', key: 'savePath', title: t('TXT_CODE_196c9daa'),
      ellipsis: true, width: 180
    },
    {
      align: 'center',
      key: 'operation',
      title: t('TXT_CODE_608994aa'),
      width: 180,
      fixed: 'right',
      condition: () => !isMultiple.value
    }
  ]);
});

const dataSource = computed(() => SearchData.value.rows);

const total = computed(() => SearchData.value.total);
//endregion

//region watch

//endregion

//region methods
const getTableList = async (params: RssManageQuery) => {
  loading.value = true;
  try {
    let newParams = formatParams(params);
    const { data } = await findRssManagePage(newParams);
    await sleep(500);
    SearchData.value = data;
  } finally {
    loading.value = false;
  }
};

const formatParams = (params: RssManageQuery) => {
  let newParams = JSON.parse(JSON.stringify(params));
  if (newParams.sendData) {
    newParams.sendDateForm = newParams.sendData[0];
    newParams.sendDateTo = newParams.sendData[1];
  }
  delete newParams.sendData;
  return newParams;
};

const handleTableChange = async (e: { page: number; limit: number }) => {
  queryParams.value.page = e.page;
  queryParams.value.limit = e.limit;
  await getTableList(queryParams.value);
};

const changeRssManage = async (params: { id: number, status?: string, complete?: string }) => {
  if (!isEmpty(params.id)) {
    try {
      await changeRssManageStatusAndComplete(params);
    } finally {
      await query();
    }
  }
};

const reload = async () => {
  SearchSelect.value = 'ALL';
  selectedRssManages.value.length = 0;
  Object.assign(queryParams.value, { page: 1, limit: 10, officialTitle: '' });
  await getTableList(queryParams.value);
};

const query = async () => {
  const params: RssManageQuery = {
    page: 1,
    limit: 10,
    ...queryParams.value
  };
  await getTableList(params);
};

const openRssManageForm = (title: string, id?: number) => {
  rssManageFormRef.value?.acceptParams(title, id);
};

const delRssManage = (id: number) => {
  Modal.confirm({
    title: t('TXT_CODE_cad8f6c3'),
    content: t('TXT_CODE_f2236242'),
    okText: t('TXT_CODE_BUTTON_DESC_CONFIRM'),
    cancelText: t('TXT_CODE_BUTTON_DESC_CANCEL'),
    okType: 'danger',
    onOk: async () => {
      loading.value = true;
      try {
        await removeRssManage(id);
        message.success(t('TXT_CODE_408ff5e0'));
      } finally {
        loading.value = false;
        await query();
      }
    }
  });
};

const refreshPoster = () => {
  Modal.confirm({
    title: t('TXT_CODE_cad8f6c3'),
    content: t('TXT_CODE_1af25c1b'),
    okText: t('TXT_CODE_BUTTON_DESC_CONFIRM'),
    cancelText: t('TXT_CODE_BUTTON_DESC_CANCEL'),
    onOk: async () => {
      loading.value = true;
      try {
        await refreshPosterApi(selectedRssManages.value);
        message.success(t('TXT_CODE_20034af4'));
      } finally {
        loading.value = false;
        await query();
      }
    }
  });
};

const refreshRssManage = async () => {
  loading.value = true;
  try {
    await refreshRssManageByIds(selectedRssManages.value);
  } finally {
    loading.value = false;
    await query();
  }
};

const openRssItem = (id: number, officialTitle: string) => {
  rssItemRef.value?.acceptParams(id, officialTitle);
};

const menuList = (record: RssManageList) =>
  arrayFilter<ItemType & { style?: CSSProperties }>([
    {
      label: t('TXT_CODE_6a905beb'),
      key: 'new',
      icon: h(PlusOutlined),
      onClick: () => openRssManageForm(t('TXT_CODE_84c8197b')),
      condition: () => !isMultiple.value
    },
    {
      label: t('TXT_CODE_389ded38'),
      key: 'enable',
      icon: h(CheckCircleOutlined),
      onClick: () => changeRssManage({ id: record.id, status: '1' }),
      condition: () => record.status === '0' && !isMultiple.value
    },
    {
      label: t('TXT_CODE_51d131da'),
      key: 'unenable',
      icon: h(StopOutlined),
      onClick: () => changeRssManage({ id: record.id, status: '0' }),
      condition: () => record.status === '1' && !isMultiple.value
    },
    {
      label: t('TXT_CODE_cf656d4b'),
      key: 'complete',
      icon: h(InboxOutlined),
      onClick: () => changeRssManage({ id: record.id, complete: '1' }),
      condition: () => record.complete === '0' && !isMultiple.value
    },
    {
      label: t('TXT_CODE_370edf53'),
      key: 'release',
      icon: h(HourglassOutlined),
      onClick: () => changeRssManage({ id: record.id, complete: '0' }),
      condition: () => record.complete === '1' && !isMultiple.value
    },
    {
      label: t('TXT_CODE_09e045a9'),
      key: 'edit',
      icon: h(EditOutlined),
      onClick: () => openRssManageForm(t('TXT_CODE_09e045a9'), record.id),
      condition: () => !isMultiple.value
    },
    {
      label: t('TXT_CODE_7502550b'),
      key: 'delete',
      icon: h(DeleteOutlined),
      style: {
        color: 'var(--color-red-5)'
      },
      condition: () => !isMultiple.value,
      onClick: () => delRssManage(record.id)
    },
    {
      label: t('TXT_CODE_269f15bd'),
      key: 'refreshPoster',
      icon: h(ReloadOutlined),
      onClick: () => refreshPoster()
    },
    {
      label: t('TXT_CODE_3d77fc59'),
      key: 'subRecord',
      icon: h(BookOutlined),
      condition: () => !isMultiple.value,
      onClick: () => openRssItem(record.id, record.officialTitle)
    },
    {
      label: t('TXT_CODE_c6c8f9b9'),
      key: 'refreshSub',
      icon: h(CloudSyncOutlined),
      onClick: () => refreshRssManage()
    }
  ]);

const oneSelected = (key: number) => {
  const index = selectedRssManages.value.indexOf(key);
  if (index > -1) return;
  selectedRssManages.value = [key];
};

const handleRightClickRow = (e: MouseEvent, record: RssManageList) => {
  e.preventDefault();
  e.stopPropagation();
  oneSelected(record.id);
  openRightClickMenu(e.clientX, e.clientY, menuList(record));
  return false;
};

watch(() => SearchSelect.value,
  (value: SearchSelectType) => {
    switch (value) {
      case 'ALL':
        Object.assign(queryParams.value, { page: 1, limit: 10, complete: '', status: '' });
        getTableList(queryParams.value);
        break;
      case 'COMPLETED':
        Object.assign(queryParams.value, { page: 1, limit: 10, complete: '1', status: '' });
        getTableList(queryParams.value);
        break;
      case 'UNFINISHED':
        Object.assign(queryParams.value, { page: 1, limit: 10, complete: '0', status: '' });
        getTableList(queryParams.value);
        break;
      case 'OPENED':
        Object.assign(queryParams.value, { page: 1, limit: 10, complete: '', status: '1' });
        getTableList(queryParams.value);
        break;
      case 'CLOSE':
        Object.assign(queryParams.value, { page: 1, limit: 10, complete: '', status: '0' });
        getTableList(queryParams.value);
        break;
    }
  }, { deep: true });
//endregion

//region otherMethods

defineOptions({ name: 'RssManage' });

onMounted(() => getTableList(queryParams.value));
//endregion

</script>

<template>
  <div style="height: 100%" class="container">
    <a-row :gutter="[24, 24]" style="height: 100%">
      <a-col :span="24">
        <between-menus>
          <template v-if="!isPhone" #left>
            <a-typography-title class="mb-0" :level="4">{{ route.meta.mainTitle }}
            </a-typography-title>
          </template>
          <template #center>
            <div class="search-input">
              <a-input-group compact>
                <a-select v-model:value="SearchSelect" style="width: 100px">
                  <a-select-option value="ALL">{{ t('TXT_CODE_b39a2cce') }}</a-select-option>
                  <a-select-option value="COMPLETED">{{ t('TXT_CODE_3f3bc788') }}</a-select-option>
                  <a-select-option value="UNFINISHED">{{ t('TXT_CODE_57cae95c') }}</a-select-option>
                  <a-select-option value="OPENED">{{ t('TXT_CODE_af627d8e') }}</a-select-option>
                  <a-select-option value="CLOSE">{{ t('TXT_CODE_be37408c') }}</a-select-option>
                </a-select>
                <a-input
                  v-model:value.trim.lazy="queryParams.officialTitle"
                  :placeholder="t('TXT_CODE_eedbd0f9')"
                  allow-clear
                  style="width: calc(100% - 100px)"
                  @change="query()"
                >
                  <template #suffix>
                    <search-outlined />
                  </template>
                </a-input>
              </a-input-group>
            </div>
          </template>
          <template #right>
            <a-button type="default" :loading="loading" @click="reload">{{ t('TXT_CODE_e4bfc6eb')
              }}
            </a-button>
            <a-dropdown v-if="isMultiple">
              <template #overlay>
                <a-menu
                  mode="vertical"
                  :items="
                    menuList({} as RssManageList)
                  "
                >
                </a-menu>
              </template>
              <a-button type="primary">
                {{ t('TXT_CODE_f769ec55') }}
                <DownOutlined />
              </a-button>
            </a-dropdown>
            <a-button v-else type="dashed" dashed
                      @click="openRssManageForm(t('TXT_CODE_84c8197b'))">
              {{ t('TXT_CODE_6a905beb') }}
            </a-button>
            <a-tooltip placement="top">
              <template #title>
                <span>{{ t('TXT_CODE_b0a2b343') }}</span>
              </template>
              <a-button type="primary" @click="triggerPushLastRssItem">
                {{ t('TXT_CODE_1c6d2625') }}
              </a-button>
            </a-tooltip>
          </template>
        </between-menus>
      </a-col>
      <a-col :span="24">
        <card-panel>
          <template #body>
            <a-table
              :loading="loading"
              :scroll="{ x: 1800 }"
              :row-selection="{
                selectedRowKeys:selectedRssManages,
                onChange:(selectedRowKeys:number[])=>{
                    selectedRssManages = selectedRowKeys
                  }
                }"
              :data-source="dataSource"
              :columns="columns"
              :preserve-selected-row-keys="true"
              :row-key="(record:RssManageList)=>record.id"
              :pagination="{
                  current:queryParams.page,
                  pageSize:queryParams.limit,
                  hideOnSinglePage: false,
                  showSizeChanger: true,
                  total:total
                }"
              :custom-row="(record: RssManageList) => {
                    return {
                      onContextmenu: (e: MouseEvent) => handleRightClickRow(e, record)
                    };}"
              @change="
                  handleTableChange({
                    page: $event.current || 0,
                    limit: $event.pageSize || 0
                  })
                "
              v-slot:bodyCell="{ column, record }: { column: any; record: RssManageList }"
            >
              <template v-if="column.key === 'operation' && !isMultiple">
                <a-dropdown v-if="isPhone">
                  <template #overlay>
                    <a-menu mode="vertical" :items="menuList(record as RssManageList)"></a-menu>
                  </template>
                  <a-button size="middle">
                    {{ t('TXT_CODE_608994aa') }}
                    <DownOutlined />
                  </a-button>
                </a-dropdown>
                <a-space v-else>
                  <a-tooltip
                    v-for="(item, i) in (menuList(record as RssManageList) as any).filter(
                          (menu: any) => !menu.children
                        )"
                    :key="i"
                    :title="item.label"
                  >
                    <a-button
                      v-if="item.key !=='new'"
                      :icon="item.icon"
                      type="text"
                      size="small"
                      :style="item.style"
                      @click="
                            () => {
                              oneSelected(record.id);
                              item.onClick();
                            }
                          "
                    >
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
              <template v-if="column.key === 'officialTitle'">
                <a-tooltip placement="top">
                  <template #title>
                    <span>{{ record.officialTitle }}</span>
                  </template>
                  <span>{{ record.officialTitle }}</span>
                </a-tooltip>
              </template>
              <template v-if="column.key === 'savePath'">
                <a-tooltip placement="top">
                  <template #title>
                    <span>{{ record.savePath }}</span>
                  </template>
                  <span>{{ record.savePath }}</span>
                </a-tooltip>
              </template>
              <template v-if="column.key === 'updateWeek'">
                <a-tag :color="WEEK_MAP[record.updateWeek].color">
                  {{ WEEK_MAP[record.updateWeek].text }}
                </a-tag>
              </template>
              <template v-if="column.key === 'status'">
                <a-switch v-model:checked="record.status"
                          @click="(checked:'0'|'1')=>{
                              changeRssManage({id:record.id,status:checked})
                            }"
                          checked-value="1"
                          un-checked-value="0"
                          :checked-children="t('TXT_CODE_DICT_YES')"
                          :un-checked-children="t('TXT_CODE_DICT_NO')" />
              </template>
              <template v-if="column.key === 'complete'">
                <a-switch v-model:checked="record.complete"
                          @click="(checked:'0'|'1')=>{
                              changeRssManage({id:record.id,complete:checked})
                            }"
                          checked-value="1"
                          un-checked-value="0"
                          :checked-children="t('TXT_CODE_DICT_YES')"
                          :un-checked-children="t('TXT_CODE_DICT_NO')" />
              </template>
              <template v-if="column.key === 'season'">
                <span>Season {{ record.season }}</span>
              </template>
              <template v-if="column.key === 'latestEpisode'">
                <span>Episode {{ record.config.latestEpisode }}</span>
              </template>
              <template v-if="column.key === 'totalEpisode'">
                <span>Episode {{ record.config.totalEpisode }}</span>
              </template>
            </a-table>
          </template>
        </card-panel>
      </a-col>
    </a-row>
    <rss-manage-form ref="rssManageFormRef" @success="query()" />
    <rss-items ref="rssItemRef" />
  </div>
</template>

<style scoped lang="scss">
.search-input {
  transition: all 0.4s;
  text-align: center;
  width: 80%;

  &:hover {
    width: 100%;
  }

  @media (max-width: 992px) {
    width: 100% !important;
  }
}
</style>
