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
      title: '番剧标题',
      width: 200
    },
    { align: 'center', dataIndex: 'season', key: 'season', title: '季度', width: 90 },
    {
      align: 'center',
      dataIndex: 'lastEpisodeNum',
      key: 'lastEpisodeNum',
      title: '最新剧集',
      width: 100
    },
    { align: 'center', dataIndex: 'status', key: 'status', title: '是否启用', width: 90 },
    {
      align: 'left', dataIndex: 'savePath', key: 'savePath', title: '保存路径',
      ellipsis: true, width: 180
    },
    { align: 'center', dataIndex: 'complete', key: 'complete', title: '是否完结', width: 90 },
    {
      align: 'center',
      dataIndex: 'updateWeek',
      key: 'updateWeek',
      title: '更新星期',
      width: 90
    },
    { align: 'center', dataIndex: 'sendDate', key: 'sendDate', title: '发布日期', width: 120 },
    {
      align: 'center',
      key: 'operation',
      title: '操作',
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
    title: '温馨提示',
    content: '是否删除选中的订阅?',
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      loading.value = true;
      try {
        await removeRssManage(id);
        message.success('删除成功');
      } finally {
        loading.value = false;
        await query();
      }
    }
  });
};

const refreshPoster = () => {
  Modal.confirm({
    title: '温馨提示',
    content: '是否刷新选中的订阅?若无选中,则刷新全部!',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      loading.value = true;
      try {
        await refreshPosterApi(selectedRssManages.value);
        message.success('刷新成功');
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
      label: '新增订阅',
      key: 'new',
      icon: h(PlusOutlined),
      onClick: () => openRssManageForm('新增'),
      condition: () => !isMultiple.value
    },
    {
      label: '启用',
      key: 'enable',
      icon: h(CheckCircleOutlined),
      onClick: () => changeRssManage({ id: record.id, status: '1' }),
      condition: () => record.status === '0' && !isMultiple.value
    },
    {
      label: '禁用',
      key: 'unenable',
      icon: h(StopOutlined),
      onClick: () => changeRssManage({ id: record.id, status: '0' }),
      condition: () => record.status === '1' && !isMultiple.value
    },
    {
      label: '完结',
      key: 'complete',
      icon: h(InboxOutlined),
      onClick: () => changeRssManage({ id: record.id, complete: '1' }),
      condition: () => record.complete === '0' && !isMultiple.value
    },
    {
      label: '发布',
      key: 'release',
      icon: h(HourglassOutlined),
      onClick: () => changeRssManage({ id: record.id, complete: '0' }),
      condition: () => record.complete === '1' && !isMultiple.value
    },
    {
      label: '编辑',
      key: 'edit',
      icon: h(EditOutlined),
      onClick: () => openRssManageForm('编辑', record.id),
      condition: () => !isMultiple.value
    },
    {
      label: '删除',
      key: 'delete',
      icon: h(DeleteOutlined),
      style: {
        color: 'var(--color-red-5)'
      },
      condition: () => !isMultiple.value,
      onClick: () => delRssManage(record.id)
    },
    {
      label: '刷新海报',
      key: 'refreshPoster',
      icon: h(ReloadOutlined),
      onClick: () => refreshPoster()
    },
    {
      label: '订阅记录',
      key: 'subRecord',
      icon: h(BookOutlined),
      condition: () => !isMultiple.value,
      onClick: () => openRssItem(record.id, record.officialTitle)
    },
    {
      label: '刷新订阅',
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
        Object.assign(queryParams.value, { page: 1, limit: 10, complete: '1' });
        getTableList(queryParams.value);
        break;
      case 'UNFINISHED':
        Object.assign(queryParams.value, { page: 1, limit: 10, complete: '0' });
        getTableList(queryParams.value);
        break;
      case 'OPENED':
        Object.assign(queryParams.value, { page: 1, limit: 10, status: '1' });
        getTableList(queryParams.value);
        break;
      case 'CLOSE':
        Object.assign(queryParams.value, { page: 1, limit: 10, status: '0' });
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
            <a-typography-title class="mb-0" :level="4">{{ route.meta.mainTitle }}</a-typography-title>
          </template>
          <template #center>
            <div class="search-input">
              <a-input-group compact>
                <a-select v-model:value="SearchSelect" style="width: 100px">
                  <a-select-option value="ALL">所有</a-select-option>
                  <a-select-option value="COMPLETED">已完结</a-select-option>
                  <a-select-option value="UNFINISHED">未完结</a-select-option>
                  <a-select-option value="OPENED">已启用</a-select-option>
                  <a-select-option value="CLOSE">未启用</a-select-option>
                </a-select>
                <a-input
                  v-model:value.trim.lazy="queryParams.officialTitle"
                  placeholder="番剧标题"
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
            <a-button type="default" :loading="loading" @click="reload">刷新列表</a-button>
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
                批量操作
                <DownOutlined />
              </a-button>
            </a-dropdown>
            <a-button v-else type="dashed" dashed @click="openRssManageForm('新增')">
              新增订阅
            </a-button>
            <a-tooltip placement="top">
              <template #title>
                <span>推送可更新番剧</span>
              </template>
              <a-button type="primary" @click="triggerPushLastRssItem">
                推送番剧
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
                    操作
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
                              changeRssManage({id:record.id,complete:checked})
                            }"
                          checked-value="1"
                          un-checked-value="0"
                          checked-children="是"
                          un-checked-children="否" />
              </template>
              <template v-if="column.key === 'complete'">
                <a-switch v-model:checked="record.complete"
                          @click="(checked:'0'|'1')=>{
                              changeRssManage({id:record.id,complete:checked})
                            }"
                          checked-value="1"
                          un-checked-value="0"
                          checked-children="是"
                          un-checked-children="否" />
              </template>
              <template v-if="column.key === 'season'">
                <span>Season {{ record.season }}</span>
              </template>
              <template v-if="column.key === 'lastEpisodeNum'">
                <span>Episode {{ record.lastEpisodeNum }}</span>
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
