<script setup lang="ts">
import BetweenMenus from '@/components/BetweenMenus.vue';
import { useRoute } from 'vue-router';
import { useScreen } from '@/hooks/useScreen.ts';
import CardPanel from '@/components/CardPanel.vue';
import { computed, type CSSProperties, h, onMounted, onUnmounted, ref, watch } from 'vue';
import { QB_STATUS_MAP, QB_TORRENTS_STATUS_MAP } from '@/types/dict.ts';
import {
  type TorrentsInfoListRequest,
  type TorrentsInfoListResponse,
  TorrentsStatusEnum
} from '@/api/types/qb/torrents';
import { findCategoriesList } from '@/api/modules/qb/categories';
import {
  deleteTorrent,
  findTorrentsPage,
  pauseTorrent,
  resumeTorrent
} from '@/api/modules/qb/torrents';
import { arrayFilter } from '@/utils/array.ts';
import type { AntColumnsType } from '@/types/ant.ts';
import { useRightClickMenu } from '@/hooks/useRightClickMenu.ts';
import { type ItemType, message } from 'ant-design-vue';
import { convertFileSize } from '@/utils/fileSize.ts';
import { formatMilliseconds, formatSpeed, formatTimestamp } from '@/utils';
import {
  CaretRightOutlined,
  DeleteOutlined,
  DownOutlined,
  PauseCircleOutlined
} from '@ant-design/icons-vue';
import { t } from '@/config/lang/i18n.ts';
//region type
const { isPhone } = useScreen();
const route = useRoute();
const { openRightClickMenu } = useRightClickMenu();
//endregion

//region props & emit
type CategoriesType = {
  name: string
  value: string
}

//endregion

//region refs & data

let task: ReturnType<typeof setInterval> | undefined;

const SearchForm = ref<TorrentsInfoListRequest>({
  filter: TorrentsStatusEnum.all,
  category: ''
});

const dataSource = ref<TorrentsInfoListResponse[]>([]);

const CategoriesTypeList = ref<CategoriesType[]>(
  [{ name: t('TXT_CODE_DICT_QB_ALL'), value: '' }]
);

const selectedRowKeys = ref<string[]>([]);

const loading = ref<boolean>(false);
const visible = ref<boolean>(false);
const deleteFiles = ref<boolean>(false);
//endregion

//region computed
const columns = computed(() => {
  return arrayFilter<AntColumnsType>([
    {
      align: 'left',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
      title: t('TXT_CODE_8634968f'),
      width: 200
    },
    { align: 'center', dataIndex: 'size', key: 'size', title: t('TXT_CODE_964f06b7'), width: 90 },
    {
      align: 'center',
      dataIndex: 'progress',
      key: 'progress',
      title: t('TXT_CODE_e2ac97d3'),
      width: 100
    },
    { align: 'center', dataIndex: 'state', key: 'state', title: t('TXT_CODE_ecc7c1f8'), width: 90 },
    { align: 'center', dataIndex: 'seed', key: 'seed', title: t('TXT_CODE_e442f33c'), width: 90 },
    { align: 'center', dataIndex: 'user', key: 'user', title: t('TXT_CODE_50f6f4ab'), width: 90 },
    {
      align: 'center',
      dataIndex: 'dlSpeed',
      key: 'dlSpeed',
      title: t('TXT_CODE_c0ca06e4'),
      width: 90
    },
    {
      align: 'center',
      dataIndex: 'upSpeed',
      key: 'upSpeed',
      title: t('TXT_CODE_4a4afc85'),
      width: 90
    },
    { align: 'center', dataIndex: 'eta', key: 'eta', title: t('TXT_CODE_d1675e39'), width: 110 },
    {
      align: 'center',
      dataIndex: 'category',
      key: 'category',
      title: t('TXT_CODE_ede2c8ec'),
      width: 90
    },
    {
      align: 'center',
      dataIndex: 'addedOn',
      key: 'addedOn',
      title: t('TXT_CODE_e1f361ba'),
      width: 200
    },
    {
      align: 'center',
      key: 'operation',
      title: t('TXT_CODE_608994aa'),
      width: 120,
      fixed: 'right',
      condition: () => !isMultiple.value
    }
  ]);
});
const isMultiple = computed(() => selectedRowKeys.value && selectedRowKeys.value.length > 1);
//endregion

//region watch

//endregion

//region methods
const initCategories = async () => {
  const { data } = await findCategoriesList();
  for (let value of data) {
    CategoriesTypeList.value.push({ name: value.name, value: value.name });
  }
};

const selectChanged = (_selectedRowKeys: string[]) => {
  selectedRowKeys.value = _selectedRowKeys;
};

const getTableList = async (params: TorrentsInfoListRequest) => {
  try {
    const { data } = await findTorrentsPage(params);
    dataSource.value = data;
  } finally {
  }
};
const oneSelected = (key: string) => {
  const index = selectedRowKeys.value.indexOf(key);
  if (index > -1) return;
  selectedRowKeys.value = [key];
};

const handleRightClickRow = (e: MouseEvent, record: TorrentsInfoListResponse) => {
  e.preventDefault();
  e.stopPropagation();
  oneSelected(record.hash);
  openRightClickMenu(e.clientX, e.clientY, menuList(record));
  return false;
};

const menuList = (record: TorrentsInfoListResponse) =>
  arrayFilter<ItemType & { style?: CSSProperties }>([
    {
      label: t('TXT_CODE_2d0d1442'),
      key: 'pause',
      icon: h(PauseCircleOutlined),
      onClick: () => handlePauseTorrents(),
      condition: () => isMultiple.value ? true : (record.state === 'stalledDL' || record.state === 'downloading')
    },
    {
      label: t('TXT_CODE_DICT_QB_RESUMED'),
      key: 'resume',
      icon: h(CaretRightOutlined),
      onClick: () => handleResumeTorrents(),
      condition: () => isMultiple.value ? true : record.state === 'pausedDL'
    },
    {
      label: t('TXT_CODE_7502550b'),
      key: 'delete',
      icon: h(DeleteOutlined),
      style: {
        color: 'var(--color-red-5)'
      },
      onClick: () => {
        visible.value = true;
      }
    }
  ]);

const handlePauseTorrents = async () => {
  try {
    loading.value = true;
    const { data } = await pauseTorrent(selectedRowKeys.value);
    data ?
      message.success(t('TXT_CODE_c580c012')) :
      message.error(t('TXT_CODE_49475943'));
  } finally {
    loading.value = false;
  }
};
const handleResumeTorrents = async () => {
  try {
    loading.value = true;
    const { data } = await resumeTorrent(selectedRowKeys.value);
    data ?
      message.success(t('TXT_CODE_ef1696c6')) :
      message.error(t('TXT_CODE_27eedb03'));
  } finally {
    loading.value = false;
  }
};
const handleDeleteTorrents = async () => {
  try {
    loading.value = true;
    const { data } = await deleteTorrent({
      hashes: selectedRowKeys.value,
      deleteFiles: deleteFiles.value
    });
    data ?
      message.success(t('TXT_CODE_408ff5e0')) :
      message.error(t('TXT_CODE_bfa350ed'));
  } finally {
    loading.value = false;
    visible.value = false;
  }
};
//endregion

//region otherMethods

defineOptions({ name: 'QBService' });

onMounted(() => {
  initCategories();
  // getTableList(SearchForm.value)
});

task = setInterval(async () => {
  await getTableList(SearchForm.value);
}, 1000);

onUnmounted(() => {
  if (task) clearInterval(task);
  task = undefined;
});

watch(() => SearchForm.value,
  () => getTableList(SearchForm.value),
  { deep: true, immediate: true });
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
              <a-select v-model:value="SearchForm.filter" style="width: 130px">
                <a-select-option v-for="([key, item], index) in Object.entries(QB_STATUS_MAP)"
                                 :key="index"
                                 :value="key">{{ item.text }}
                </a-select-option>
              </a-select>
              <a-select v-model:value="SearchForm.category" style="width: 130px">
                <a-select-option v-for="(value,index) in CategoriesTypeList"
                                 :key="index"
                                 :value="value.value">{{ value.name }}
                </a-select-option>
              </a-select>
            </div>
          </template>
          <template #right>
            <a-dropdown v-if="isMultiple">
              <template #overlay>
                <a-menu
                  mode="vertical"
                  :items="
                    menuList({} as TorrentsInfoListResponse)
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
              :row-selection="{
                selectedRowKeys: selectedRowKeys,
                onChange: selectChanged
              }"
              :row-key="(record: TorrentsInfoListResponse) => record.hash"
              :data-source="dataSource"
              :columns="columns"
              :scroll="{ x: 1800 }"
              size="small"
              :custom-row="(record: TorrentsInfoListResponse) => {
                    return {
                      onContextmenu: (e: MouseEvent) => handleRightClickRow(e, record as TorrentsInfoListResponse)
                    };}"
              v-slot:bodyCell="{ column, record }: { column: any; record: TorrentsInfoListResponse }"
            >
              <template v-if="column.key === 'name'">
                <a-tooltip placement="top">
                  <template #title>
                    <span>{{ record.name }}</span>
                  </template>
                  <span>{{ record.name }}</span>
                </a-tooltip>
              </template>
              <template v-if="column.key === 'size'">
                {{ convertFileSize(String(record.size)) }}
              </template>
              <template v-if="column.key === 'progress'">
                <a-progress
                  :percent="Math.round(record.progress * 100)"
                />
              </template>
              <template v-if="column.key === 'state'">
                <a-tag>
                  {{ QB_TORRENTS_STATUS_MAP[record.state].text }}
                </a-tag>
              </template>
              <template v-if="column.key === 'seed'">
                <span>{{ record.numSeeds || 0 }}</span>(<span>{{ record.numComplete || 0 }}</span>)
              </template>
              <template v-if="column.key === 'user'">
                <span>{{ record.numLeechs || 0 }}</span>(<span>{{ record.numIncomplete || 0
                }}</span>)
              </template>
              <template v-if="column.key === 'dlSpeed'">
                {{ formatSpeed(record.dlSpeed || 0) }}
              </template>
              <template v-if="column.key === 'upSpeed'">
                {{ formatSpeed(record.upSpeed || 0) }}
              </template>
              <template v-if="column.key === 'eta'">
                {{ formatMilliseconds(record.eta) }}
              </template>
              <template v-if="column.key === 'addedOn'">
                {{ formatTimestamp(record.addedOn) }}
              </template>
              <template v-if="column.key === 'operation' && !isMultiple">
                <a-dropdown v-if="isPhone">
                  <template #overlay>
                    <a-menu mode="vertical"
                            :items="menuList(record as TorrentsInfoListResponse)"></a-menu>
                  </template>
                  <a-button size="middle">
                    {{ t('TXT_CODE_608994aa') }}
                    <DownOutlined />
                  </a-button>
                </a-dropdown>
                <a-space v-else>
                  <a-tooltip
                    v-for="(item, i) in (menuList(record as TorrentsInfoListResponse) as any).filter(
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
                              oneSelected(record.hash);
                              item.onClick();
                            }
                          "
                    >
                    </a-button>
                  </a-tooltip>
                </a-space>
              </template>
            </a-table>
          </template>
        </card-panel>
      </a-col>
    </a-row>
    <a-modal
      v-model:open="visible" title="提示" :ok-text="t('TXT_CODE_7502550b')" :cancel-text="t('TXT_CODE_BUTTON_DESC_CANCEL')"
      @ok="handleDeleteTorrents"
    >
      <a-checkbox v-model:checked="deleteFiles">{{ t('TXT_CODE_994faa01')}}</a-checkbox>
    </a-modal>
  </div>
</template>

<style scoped lang="scss">
.search-input {
  text-align: center;
}
</style>
