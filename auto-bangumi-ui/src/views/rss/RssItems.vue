<script setup lang="ts">
import { useScreen } from '@/hooks/useScreen.ts';
import BetweenMenus from '@/components/BetweenMenus.vue';
import {
  CheckCircleOutlined,
  DeleteOutlined,
  DownOutlined,
  HourglassOutlined,
  InboxOutlined,
  RocketOutlined,
  SearchOutlined,
  StopOutlined
} from '@ant-design/icons-vue';
import { computed, type CSSProperties, h, ref, watch } from 'vue';
import type { RssItemList, RssItemQuery } from '@/api/types/rss/rssItem.ts';
import type { IPage } from '@/api/types';
import { arrayFilter } from '@/utils/array.ts';
import type { AntColumnsType } from '@/types/ant.ts';
import { sleep } from '@/utils/common.ts';
import { isEmpty } from '@/utils';
import {
  findRssItemPage,
  pushRssItemToDownLoad,
  removeRssItemByTorrentCodes,
  updateRssItemToList
} from '@/api/modules/rssItem.ts';
import { type ItemType, message, Modal } from 'ant-design-vue';
import type { Key } from 'ant-design-vue/es/_util/type';
import CardPanel from '@/components/CardPanel.vue';
import { useRightClickMenu } from '@/hooks/useRightClickMenu.ts';
import { t } from '@/lang/i18n.ts';
import type { RssManageList } from '@/api/types/rss/rssManage.ts';
import { removeRssManage } from '@/api/modules/rssManage.ts';
//region type

const { isPhone } = useScreen();
const { openRightClickMenu } = useRightClickMenu();
//endregion

//region props & emit

const visible = ref<boolean>(false);

const loading = ref<boolean>(false);

type SearchSelectType = 'ALL' | 'PUSH' | 'PUSHED' | 'DOWNLOAD' | 'DOWNLOADED'

const SearchSelect = ref<SearchSelectType>('ALL');

const dialogTitle = ref<string>(t('TXT_CODE_b992ba89'));

//endregion

//region refs & data
const queryParams = ref<RssItemQuery>({
  page: 1,
  limit: 10
});

const SearchData = ref<IPage<RssItemList>>({
  current: 1,
  total: 0,
  limit: 10,
  rows: []
});

const SearchTitle = ref<string>('');

const selectedRssItems = ref<string[]>([]);
//endregion

//region computed
const columns = computed(() => {
  return arrayFilter<AntColumnsType>([
    {
      align: 'left', dataIndex: 'name', key: 'name', title: t('TXT_CODE_efb2a8da'),
      ellipsis: true, minWidth: 180
    },
    {
      align: 'center',
      dataIndex: 'episodeNum',
      key: 'episodeNum',
      title: t('TXT_CODE_513e85d7'),
      minWidth: 90
    },
    {
      align: 'left',
      dataIndex: 'translationGroup',
      key: 'translationGroup',
      title: t('TXT_CODE_66d9a5b0'),
      ellipsis: true,
      minWidth: 90,
      condition: () => !isPhone.value
    },
    {
      align: 'center',
      dataIndex: 'downloaded',
      key: 'downloaded',
      title: t('TXT_CODE_53df9ac8'),
      minWidth: 90
    },
    {
      align: 'center',
      dataIndex: 'pushed',
      key: 'pushed',
      title: t('TXT_CODE_dc75dbd2'),
      minWidth: 90
    },
    {
      align: 'left', dataIndex: 'torrentName', key: 'torrentName', title: t('TXT_CODE_e3bbdcd7'),
      ellipsis: true, minWidth: 180,
      condition: () => !isPhone.value
    },
    {
      align: 'left', dataIndex: 'torrentCode', key: 'torrentCode', title: t('TXT_CODE_32bc721a'),
      ellipsis: true, minWidth: 180,
      condition: () => !isPhone.value
    },
    {
      align: 'center',
      key: 'operation',
      title: t('TXT_CODE_608994aa'),
      minWidth: 120,
      fixed: 'right',
      condition: () => !isMultiple.value
    }
  ]);
});

const dataSource = computed(() => SearchData.value.rows);

const total = computed(() => SearchData.value.total);

const isMultiple = computed(() => selectedRssItems.value && selectedRssItems.value.length > 1);
//endregion

//region watch

//endregion

//region methods

const acceptParams = async (id: number, officialTitle: string) => {
  visible.value = true;
  dialogTitle.value = officialTitle;
  queryParams.value.rssManageId = id;
  await query();
};

const getTableList = async (params: RssItemQuery) => {
  loading.value = true;
  try {
    const { data } = await findRssItemPage(params);
    await sleep(500);
    SearchData.value = data;
  } finally {
    loading.value = false;
  }
};

const handleTableChange = async (e: { page: number; limit: number }) => {
  queryParams.value.page = e.page;
  queryParams.value.limit = e.limit;
  await getTableList(queryParams.value);
};

const changeRssItem = async (params: { id: number, downloaded?: string, pushed?: string }) => {
  if (!isEmpty(params.id)) {
    try {
      await updateRssItemToList(params);
    } finally {
      await query();
    }
  }
};

const query = async () => {
  const params: RssItemQuery = {
    page: 1,
    limit: 10,
    ...queryParams.value
  };
  await getTableList(params);
};
const reload = async () => {
  SearchSelect.value = 'ALL';
  selectedRssItems.value.length = 0;
  Object.assign(queryParams.value, {
    page: 1,
    limit: 10,
    name: '',
    torrentName: '',
    translationGroup: ''
  });
  await getTableList(queryParams.value);
};

const pushRssItem = async () => {
  if (isEmpty(selectedRssItems.value)) {
    return message.error(t('TXT_CODE_10b3eb83'));
  }
  try {
    loading.value = true;
    await pushRssItemToDownLoad(selectedRssItems.value);
  } finally {
    setTimeout(async () => {
      await query();
      loading.value = false;
    }, 1000);
  }
};

const delRssItem = ()=>{
  Modal.confirm({
    title: t('TXT_CODE_cad8f6c3'),
    content: t('TXT_CODE_754c6a74'),
    okText: t('TXT_CODE_BUTTON_DESC_CONFIRM'),
    cancelText: t('TXT_CODE_BUTTON_DESC_CANCEL'),
    okType: 'danger',
    onOk: async () => {
      loading.value = true;
      try {
        await removeRssItemByTorrentCodes(selectedRssItems.value)
        message.success(t('TXT_CODE_408ff5e0'));
      } finally {
        loading.value = false;
        await query();
      }
    }
  });
}

const menuList = (record: RssItemList) =>
  arrayFilter<ItemType & { style?: CSSProperties }>([
    {
      label: t('TXT_CODE_1b1c9472'),
      key: 'DOWNLOADED',
      icon: h(CheckCircleOutlined),
      onClick: () => changeRssItem({ id: record.id, downloaded: '1' }),
      condition: () => record.downloaded === '0' && !isMultiple.value
    },
    {
      label: t('TXT_CODE_c94d5184'),
      key: 'DOWNLOAD',
      icon: h(StopOutlined),
      onClick: () => changeRssItem({ id: record.id, downloaded: '0' }),
      condition: () => record.downloaded === '1' && !isMultiple.value
    },
    {
      label: t('TXT_CODE_b68ecac9'),
      key: 'PUSHED',
      icon: h(InboxOutlined),
      onClick: () => changeRssItem({ id: record.id, pushed: '1' }),
      condition: () => record.pushed === '0' && !isMultiple.value
    },
    {
      label: t('TXT_CODE_52a4105a'),
      key: 'PUSH',
      icon: h(HourglassOutlined),
      onClick: () => changeRssItem({ id: record.id, pushed: '0' }),
      condition: () => record.pushed === '1' && !isMultiple.value
    },
    {
      label: t('TXT_CODE_7502550b'),
      key: 'DELETE',
      icon: h(DeleteOutlined),
      style: {
        color: 'var(--color-red-5)'
      },
      onClick: () => delRssItem()
    },
    {
      label: t('TXT_CODE_8653dcc4'),
      key: 'refreshPoster',
      icon: h(RocketOutlined),
      onClick: () => pushRssItem()
    }
  ]);

const oneSelected = (key: string) => {
  const index = selectedRssItems.value.indexOf(key);
  if (index > -1) return;
  selectedRssItems.value = [key];
};
const handleRightClickRow = (e: MouseEvent, record: RssItemList) => {
  e.preventDefault();
  e.stopPropagation();
  oneSelected(record.torrentCode);
  openRightClickMenu(e.clientX, e.clientY, menuList(record));
  return false;
};
//endregion

//region otherMethods

watch(() => SearchSelect.value,
  (value: SearchSelectType) => {
    switch (value) {
      case 'ALL':
        Object.assign(queryParams.value, { page: 1, limit: 10, pushed: '', downloaded: '' });
        break;
      case 'PUSH':
        Object.assign(queryParams.value, { page: 1, limit: 10, pushed: '0', downloaded: '' });
        getTableList(queryParams.value);
        break;
      case 'PUSHED':
        Object.assign(queryParams.value, { page: 1, limit: 10, pushed: '1', downloaded: '' });
        getTableList(queryParams.value);
        break;
      case 'DOWNLOAD':
        Object.assign(queryParams.value, { page: 1, limit: 10, pushed: '', downloaded: '0' });
        getTableList(queryParams.value);
        break;
      case 'DOWNLOADED':
        Object.assign(queryParams.value, { page: 1, limit: 10, pushed: '', downloaded: '1' });
        getTableList(queryParams.value);
        break;
    }
  }, { deep: true });


watch(() => SearchTitle.value,
  (value: string) => {
    Object.assign(queryParams.value, {
      page: 1,
      limit: 10,
      translationGroup: value,
      torrentName: value,
      name: value
    });
    getTableList(queryParams.value);
  }, { deep: true });


defineOptions({ name: 'RssItems' });

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
    :width="isPhone?'100%':'80%'"
    destroy-on-close>
    <div class="container">
      <a-row :gutter="[24, 24]" style="height: 100%">
        <a-col :span="24">
          <between-menus>
            <template #left>
              <div class="search-input">
                <a-input-group compact>
                  <a-select v-model:value="SearchSelect" style="width: 100px">
                    <a-select-option value="ALL">{{ t('TXT_CODE_b39a2cce') }}</a-select-option>
                    <a-select-option value="PUSH">{{ t('TXT_CODE_52a4105a') }}</a-select-option>
                    <a-select-option value="PUSHED">{{ t('TXT_CODE_b68ecac9') }}</a-select-option>
                    <a-select-option value="DOWNLOAD">{{ t('TXT_CODE_c94d5184') }}</a-select-option>
                    <a-select-option value="DOWNLOADED">{{ t('TXT_CODE_1b1c9472') }}
                    </a-select-option>
                  </a-select>
                  <a-input
                    v-model:value.trim.lazy="SearchTitle"
                    :placeholder="t('TXT_CODE_f1142188')"
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
              <a-button type="default" :loading="loading" @click="reload">刷新</a-button>
              <a-dropdown v-if="isMultiple">
                <template #overlay>
                  <a-menu
                    mode="vertical"
                    :items="
                    menuList({} as RssItemList)
                  "
                  >
                  </a-menu>
                </template>
                <a-button type="primary">
                  {{ t('TXT_CODE_f769ec55') }}
                  <DownOutlined />
                </a-button>
              </a-dropdown>
            </template>
          </between-menus>
        </a-col>
        <a-col :span="24">
          <card-panel>
            <template #body>
              <a-table
                :loading="loading"
                :scroll="{x: 'max-content'}"
                :row-selection="{
                selectedRowKeys:selectedRssItems,
                onChange:(selectedRowKeys:Key[])=>{
                    selectedRssItems = selectedRowKeys as string[];
                  }
                }"
                :data-source="dataSource"
                :columns="columns"
                :preserve-selected-row-keys="true"
                :row-key="(record:RssItemList)=>record.torrentCode"
                :pagination="{
                  current:queryParams.page,
                  pageSize:queryParams.limit,
                  hideOnSinglePage: false,
                  showSizeChanger: true,
                  total:total
                }"
                @change="
                  handleTableChange({
                    page: $event.current || 0,
                    limit: $event.pageSize || 0
                  })
                "
                :custom-row="(record: RssItemList) => {
                    return {
                      onContextmenu: (e: MouseEvent) => handleRightClickRow(e, record)
                    };}"
                v-slot:bodyCell="{ column, record }: { column: any; record: RssItemList }"
              >
                <template v-if="column.key === 'operation' && !isMultiple">
                  <a-dropdown v-if="isPhone">
                    <template #overlay>
                      <a-menu mode="vertical" :items="menuList(record as RssItemList)"></a-menu>
                    </template>
                    <a-button size="middle">
                      {{ t('TXT_CODE_608994aa') }}
                      <DownOutlined />
                    </a-button>
                  </a-dropdown>
                  <a-space v-else>
                    <a-tooltip
                      v-for="(item, i) in (menuList(record as RssItemList) as any).filter(
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
                              oneSelected(record.torrentCode);
                              item.onClick();
                            }
                          "
                      >
                      </a-button>
                    </a-tooltip>
                  </a-space>
                </template>
                <template v-if="column.key === 'torrentCode'">
                  <a-tooltip placement="top">
                    <template #title>
                      <span>{{ record.torrentCode }}</span>
                    </template>
                    <span>{{ record.torrentCode }}</span>
                  </a-tooltip>
                </template>
                <template v-if="column.key === 'torrentName'">
                  <a-tooltip placement="top">
                    <template #title>
                      <span>{{ record.torrentName }}</span>
                    </template>
                    <span>{{ record.torrentName }}</span>
                  </a-tooltip>
                </template>
                <template v-if="column.key === 'name'">
                  <a-tooltip placement="top">
                    <template #title>
                      <span>{{ record.name }}</span>
                    </template>
                    <span>{{ record.name }}</span>
                  </a-tooltip>
                </template>
                <template v-if="column.key === 'downloaded'">
                  <a-switch v-model:checked="record.downloaded"
                            @click="(checked:'0'|'1')=>{
                              changeRssItem({id:record.id,downloaded:checked})
                            }"
                            checked-value="1"
                            un-checked-value="0"
                            :checked-children="t('TXT_CODE_DICT_YES')"
                            :un-checked-children="t('TXT_CODE_DICT_NO')" />
                </template>
                <template v-if="column.key === 'pushed'">
                  <a-switch v-model:checked="record.pushed"
                            @click="(checked:'0'|'1')=>{
                              changeRssItem({id:record.id,pushed:checked})
                            }"
                            checked-value="1"
                            un-checked-value="0"
                            :checked-children="t('TXT_CODE_DICT_YES')"
                            :un-checked-children="t('TXT_CODE_DICT_NO')" />
                </template>
              </a-table>
            </template>
          </card-panel>
        </a-col>
      </a-row>
    </div>
  </a-drawer>
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
