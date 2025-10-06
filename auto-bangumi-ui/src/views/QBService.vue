<script setup lang="ts">
import BetweenMenus from "@/components/BetweenMenus.vue";
import {useRoute} from 'vue-router';
import {useScreen} from "@/hooks/useScreen.ts";
import CardPanel from "@/components/CardPanel.vue";
import {computed, type CSSProperties, onMounted, onUnmounted, ref, watch} from "vue";
import {QB_STATUS_MAP, QB_TORRENTS_STATUS_MAP} from "@/types/dict.ts";
import {
  type TorrentsInfoListRequest,
  type TorrentsInfoListResponse,
  TorrentsStatusEnum
} from "@/api/types/qb/torrents";
import {findCategoriesList} from "@/api/modules/qb/categories";
import {findTorrentsPage} from "@/api/modules/qb/torrents";
import {arrayFilter} from "@/utils/array.ts";
import type {AntColumnsType} from "@/types/ant.ts";
import {useRightClickMenu} from "@/hooks/useRightClickMenu.ts";
import type {ItemType} from "ant-design-vue";
import {convertFileSize} from "@/utils/fileSize.ts";
import {formatMilliseconds, formatSpeed, formatTimestamp} from "@/utils";
//region type
const {isPhone} = useScreen();
const route = useRoute();
const {openRightClickMenu} = useRightClickMenu();
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
  category: '',
})

const dataSource = ref<TorrentsInfoListResponse[]>([])

const CategoriesTypeList = ref<CategoriesType[]>(
  [{name: '全部', value: ''}]
);

const selectedRowKeys = ref<string[]>([])
//endregion

//region computed
const columns = computed(() => {
  return arrayFilter<AntColumnsType>([
    {align: 'left', dataIndex: 'name', key: 'name', ellipsis: true, title: '种子名称', width: 200},
    {align: 'center', dataIndex: 'size', key: 'size', title: '选定大小', width: 90},
    {
      align: 'center',
      dataIndex: 'progress',
      key: 'progress',
      title: '下载进度',
      width: 100
    },
    {align: 'center', dataIndex: 'state', key: 'state', title: '状态', width: 90},
    {align: 'center', dataIndex: 'seed', key: 'seed', title: '种子', width: 90},
    {align: 'center', dataIndex: 'user', key: 'user', title: '用户', width: 90},
    {
      align: 'center',
      dataIndex: 'dlSpeed',
      key: 'dlSpeed',
      title: '下载速度',
      width: 90
    },
    {
      align: 'center',
      dataIndex: 'upSpeed',
      key: 'upSpeed',
      title: '上传速度',
      width: 90
    },
    {align: 'center', dataIndex: 'eta', key: 'eta', title: '剩余时间', width: 90},
    {
      align: 'center',
      dataIndex: 'category',
      key: 'category',
      title: '分类',
      width: 90
    },
    {
      align: 'center',
      dataIndex: 'addedOn',
      key: 'addedOn',
      title: '添加时间',
      width: 200
    },
  ])
})
//endregion

//region watch

//endregion

//region methods
const initCategories = async () => {
  const {data} = await findCategoriesList();
  for (let value of data) {
    CategoriesTypeList.value.push({name: value.name, value: value.name})
  }
}

const selectChanged = (_selectedRowKeys: string[]) => {
  selectedRowKeys.value = _selectedRowKeys;
};

const getTableList = async (params: TorrentsInfoListRequest) => {
  try {
    const {data} = await findTorrentsPage(params)
    dataSource.value = data
  } finally {
  }
}
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
  arrayFilter<ItemType & { style?: CSSProperties }>([]);
//endregion

//region otherMethods

defineOptions({name: 'QBService'});

onMounted(() => {
  initCategories()
  // getTableList(SearchForm.value)
})

task = setInterval(async () => {
  await getTableList(SearchForm.value);
}, 3000);

onUnmounted(() => {
  if (task) clearInterval(task);
  task = undefined;
});

watch(() => SearchForm.value,
  () => getTableList(SearchForm.value),
  {deep: true, immediate: true})
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
            <div>
              <a-form :model="SearchForm" layout="inline">
                <a-form-item label="状态">
                  <a-select v-model:value="SearchForm.filter" style="width: 100px">
                    <a-select-option v-for="([key, item], index) in Object.entries(QB_STATUS_MAP)"
                                     :key="index"
                                     :value="key">{{ item.text }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="分类">
                  <a-select v-model:value="SearchForm.category" style="width: 100px">
                    <a-select-option v-for="(value,index) in CategoriesTypeList"
                                     :key="index"
                                     :value="value.value">{{ value.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-form>
            </div>
          </template>
          <template #right>
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
              :row-key="(record: TorrentsInfoListResponse) => record.name"
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
                <span>{{ record.numSeeds||0 }}</span>(<span>{{ record.numComplete||0 }}</span>)
              </template>
              <template v-if="column.key === 'user'">
                <span>{{ record.numLeechs||0 }}</span>(<span>{{ record.numIncomplete||0 }}</span>)
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
            </a-table>
          </template>
        </card-panel>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped lang="scss">

</style>
