<script setup lang="ts">
import {useScreen} from '@/hooks/useScreen.ts';
import {computed, type CSSProperties, h, onMounted, onUnmounted, ref} from 'vue';
import {arrayFilter} from '@/utils/array.ts';
import type {AntColumnsType} from '@/types/ant.ts';
import {filterFileName, getFileExtName, getFileIcon, isCompressFile} from '@/utils/fileManager.ts';
import {convertFileSize} from '@/utils/fileSize.ts';
import dayjs from 'dayjs';
import {useFileManager} from '@/hooks/useFileManager.ts';
import {useRightClickMenu} from '@/hooks/useRightClickMenu.ts';
import {useUserStore} from '@/stores/modules/user.ts';
import FileEditor from '@/components/FileEditor.vue';
import {type ItemType, message, Modal} from 'ant-design-vue';
import {
  CopyOutlined,
  DeleteOutlined,
  DownloadOutlined,
  DownOutlined,
  EditOutlined,
  ExclamationCircleOutlined,
  FileOutlined,
  FileZipOutlined,
  FolderOutlined,
  FormOutlined,
  KeyOutlined,
  PlusOutlined,
  ScissorOutlined,
  SearchOutlined,
  UploadOutlined
} from '@ant-design/icons-vue';
import type {MscFileList} from '@/api/types/mcs/files';
import BetweenMenus from '@/components/BetweenMenus.vue';
import {useRoute} from 'vue-router';
import CardPanel from '@/components/CardPanel.vue';
import {t} from '@/config/lang/i18n.ts';
//region type

const route = useRoute();
const { isPhone } = useScreen();
const { userInfo } = useUserStore();


const instanceId = userInfo.config?.mcsManageSetting.instanceId;

const daemonId = userInfo.config?.mcsManageSetting.daemonId;

const {
  dialog,
  percentComplete,
  spinning,
  fileStatus,
  permission,
  selectedRowKeys,
  queryParams,
  total,
  SearchData,
  dataSource,
  breadcrumbs,
  clipboard,
  currentDisk,
  isMultiple,
  selectChanged,
  getFileList,
  touchFile,
  reloadList,
  setClipBoard,
  paste,
  resetName,
  deleteFile,
  zipFile,
  unzipFile,
  beforeUpload,
  downloadFile,
  handleChangeDir,
  handleSearchChange,
  selectedFile,
  rowClickTable,
  handleTableChange,
  getFileStatus,
  changePermission,
  toDisk,
  oneSelected,
  isImage,
  showImage
} = useFileManager();

const { openRightClickMenu } = useRightClickMenu();
//endregion

//region props & emit

//endregion

//region refs & data
let task: ReturnType<typeof setInterval> | undefined;

let uploading = false;

const FileEditorDialog = ref<InstanceType<typeof FileEditor>>();

const opacity = ref(false);
//endregion

//region computed

const isShowDiskList = computed(
  () =>
    fileStatus.value?.disks.length &&
    fileStatus.value?.platform === 'win32' &&
    fileStatus.value?.isGlobalInstance
);

const columns = computed(() => {
  return arrayFilter<AntColumnsType>([
    {
      align: 'left',
      title: t('TXT_CODE_86695f5f'),
      dataIndex: 'name',
      key: 'name',
      minWidth: 200
    },
    {
      align: 'center',
      title: t('TXT_CODE_74cb2b17'),
      dataIndex: 'type',
      key: 'type',
      customRender: (e: { text: number; record: { name: string } }) => {
        return e.text == 1 ? filterFileName(e.record.name) : t('TXT_CODE_15fbf98b');
      },
      condition: () => !isPhone.value,
      minWidth: 200
    },
    {
      align: 'center',
      title: t('TXT_CODE_9893ccd3'),
      dataIndex: 'size',
      key: 'size',
      customRender: (e: { text: number }) =>
        e.text == 0 ? '--' : convertFileSize(e.text.toString()),
      minWidth: 200,
      condition: () => !isPhone.value
    },
    {
      align: 'center',
      title: t('TXT_CODE_f50a4cc9'),
      dataIndex: 'time',
      key: 'time',
      customRender: (e: { text: string }) => {
        return dayjs(e.text).format('YYYY-MM-DD HH:mm:ss');
      },
      minWidth: 200,
      condition: () => !isPhone.value
    },
    {
      align: 'center',
      title: t('TXT_CODE_c0222feb'),
      dataIndex: 'mode',
      key: 'mode',
      minWidth: 200
    },
    {
      align: isPhone.value ? 'center' : 'right',
      title: t('TXT_CODE_608994aa'),
      dataIndex: 'action',
      key: 'action',
      minWidth: 200,
      condition: () => !isMultiple.value
    }
  ]);
});
//endregion

//region watch

//endregion

//region methods
const handleDragover = (e: DragEvent) => {
  e.preventDefault();
  opacity.value = true;
};

const handleDragleave = (e: DragEvent) => {
  e.preventDefault();
  opacity.value = false;
};

const handleDrop = (e: DragEvent) => {
  e.preventDefault();
  const files = e.dataTransfer?.files;
  opacity.value = false;
  if (!files) return;
  if (files.length === 0) return;
  if (files.length > 1) return message.error(t('TXT_CODE_dd31c8fa'));
  if (percentComplete.value > 0) return message.error(t('TXT_CODE_649d5bb1'));
  Modal.confirm({
    title: t('TXT_CODE_79ea06c8', { filename: files[0].name }),
    icon: h(ExclamationCircleOutlined),
    content: t('TXT_CODE_79d13f80'),
    onOk() {
      selectedFile(files[0]);
    }
  });
};

const editFile = (fileName: string) => {
  const path = breadcrumbs[breadcrumbs.length - 1].path + fileName;
  FileEditorDialog.value?.openDialog(path, fileName);
};

const handleClickFile = async (file: MscFileList) => {
  if (file.type === '0') return rowClickTable(file.name, file.type);
  const fileExtName = getFileExtName(file.name);
  if (isImage(fileExtName)) return showImage(file);
  return editFile(file.name);
};

const menuList = (record: MscFileList) =>
  arrayFilter<ItemType & { style?: CSSProperties }>([
    {
      label: t('TXT_CODE_a1187741'),
      key: 'new',
      icon: h(PlusOutlined),
      children: [
        {
          label: t('TXT_CODE_2c7407e8'),
          key: 'newFolder',
          icon: h(FolderOutlined),
          onClick: () => touchFile(true)
        },
        {
          label: t('TXT_CODE_2ce26742'),
          key: 'newFile',
          icon: h(FileOutlined),
          onClick: () => touchFile()
        }
      ],
      condition: () => !isMultiple.value
    },
    {
      label: t('TXT_CODE_f1ab9de1'),
      key: 'unzip',
      icon: h(FileZipOutlined),
      onClick: () => unzipFile(record.name),
      condition: () => record.type === '1' && isCompressFile(record.name)
    },
    {
      label: t('TXT_CODE_09e045a9'),
      key: 'edit',
      icon: h(EditOutlined),
      onClick: () => editFile(record.name),
      condition: () => !isMultiple.value && record.type === '1'
    },
    {
      label: t('TXT_CODE_BUTTON_DESC_DOWNLOAD'),
      key: 'download',
      icon: h(DownloadOutlined),
      onClick: () => downloadFile(record.name),
      condition: () => !isMultiple.value && record.type === '1'
    },
    {
      label: t('TXT_CODE_48ab9b61'),
      key: 'cut',
      icon: h(ScissorOutlined),
      onClick: () => setClipBoard('move')
    },
    {
      label: t('TXT_CODE_6edf7c5e'),
      key: 'copy',
      icon: h(CopyOutlined),
      onClick: () => setClipBoard('copy')
    },
    {
      label: t('TXT_CODE_68b085b2'),
      key: 'rename',
      icon: h(FormOutlined),
      onClick: () => resetName(record.name),
      condition: () => !isMultiple.value
    },
    {
      label: t('TXT_CODE_3e37664a'),
      key: 'changePermission',
      icon: h(KeyOutlined),
      onClick: () => changePermission(record.name, record.mode),
      condition: () => !isMultiple.value && fileStatus.value?.platform !== 'win32'
    },
    {
      label: t('TXT_CODE_9e17e170'),
      key: 'zip',
      icon: h(FileZipOutlined),
      onClick: () => zipFile()
    },
    {
      label: t('TXT_CODE_7502550b'),
      key: 'delete',
      icon: h(DeleteOutlined),
      style: {
        color: 'var(--color-red-5)'
      },
      onClick: () => deleteFile(record.name)
    }
  ]);

const handleRightClickRow = (e: MouseEvent, record: MscFileList) => {
  e.preventDefault();
  e.stopPropagation();
  oneSelected(record.name, record);
  openRightClickMenu(e.clientX, e.clientY, menuList(record));
  return false;
};
//endregion

//region otherMethods
task = setInterval(async () => {
  await getFileStatus();
}, 3000);

onMounted(async () => {
  await getFileStatus();
  dialog.value.loading = true;
  await getFileList();
  dialog.value.loading = false;
});

onUnmounted(() => {
  if (task) clearInterval(task);
  task = undefined;
});

defineOptions({ name: 'FileManage' });
//endregion

</script>

<template>
  <div style="height: 100%" class="container">
    <a-row :gutter="[24, 24]" style="height: 100%">
      <a-col :span="24">
        <between-menus>
          <template v-if="!isPhone" #left>
            <a-typography-title class="mb-0" :level="4">
              {{ route.meta.mainTitle }}
            </a-typography-title>
          </template>
          <template #center>
            <div class="search-input">
              <a-input
                v-model:value.trim.lazy="queryParams.filename"
                :placeholder="t('TXT_CODE_5787d578')"
                allow-clear
                @change="handleSearchChange()"
              >
                <template #suffix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
          </template>
          <template #right>
            <a-typography-text v-if="selectedRowKeys.length">
              {{ t('TXT_CODE_2df3061e', { num: String(selectedRowKeys.length) }) }}
            </a-typography-text>
            <a-upload
              :before-upload="beforeUpload"
              :max-count="1"
              :disabled="percentComplete > 0"
              :show-upload-list="false"
            >
              <a-button type="dashed" :loading="percentComplete > 0">
                <upload-outlined v-if="percentComplete === 0" />
                {{
                  percentComplete > 0
                    ? t('TXT_CODE_3ff7d88a', { num: percentComplete })
                    : t('TXT_CODE_aa7deb70')
                }}
              </a-button>
            </a-upload>
            <a-button
              v-if="clipboard?.value && clipboard.value.length > 0"
              type="dashed"
              danger
              @click="paste()"
            >
              {{ t('TXT_CODE_38f7cb5a') }}
            </a-button>
            <a-button v-else type="default" @click="reloadList()">
              {{ t('TXT_CODE_e4bfc6eb') }}
            </a-button>
            <a-dropdown v-if="isMultiple">
              <template #overlay>
                <a-menu
                  mode="vertical"
                  :items="
                    menuList({
                      absolutePath:'',
                      name: '',
                      type: '0',
                      size: 0,
                      time: '',
                      mode: 0
                    })
                  "
                >
                </a-menu>
              </template>
              <a-button type="primary">
                {{ t('TXT_CODE_f769ec55') }}
                <DownOutlined />
              </a-button>
            </a-dropdown>
            <a-dropdown v-else>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="newFile" @click="touchFile()">
                    {{ t('TXT_CODE_2ce26742') }}
                  </a-menu-item>
                  <a-menu-item key="newFolder" @click="touchFile(true)">
                    {{ t('TXT_CODE_2c7407e8') }}
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button type="primary">
                {{ t('TXT_CODE_a1187741') }}
                <DownOutlined />
              </a-button>
            </a-dropdown>
          </template>
        </between-menus>
      </a-col>
      <a-col :span="24">
        <card-panel
          style="height: 100%"
          :style="opacity && 'opacity: 0.4'"
          @dragover="handleDragover"
          @dragleave="handleDragleave"
          @drop="handleDrop"
        >
          <template #body>
            <a-progress
              v-if="percentComplete > 0"
              :stroke-color="{
                '0%': '#49b3ff',
                '100%': '#25f5b9'
              }"
              :percent="percentComplete"
              class="mb-20"
            />
            <div class="flex-wrap items-flex-start">
              <a-select
                v-if="isShowDiskList"
                v-model:value="currentDisk"
                :class="isPhone ? 'w-100 mb-10' : 'mr-10'"
                style="width: 125px"
                @change="toDisk(currentDisk)"
              >
                <a-select-option value="/">{{ t('TXT_CODE_2f642f11') }}</a-select-option>
                <a-select-option v-for="disk in fileStatus?.disks" :key="disk" :value="disk">
                  {{ disk }}
                </a-select-option>
              </a-select>
              <div class="file-breadcrumbs mb-20">
                <a-breadcrumb separator=">">
                  <a-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
                    <div class="file-breadcrumbs-item" @click="handleChangeDir(item.path)">
                      {{ item.name }}
                    </div>
                  </a-breadcrumb-item>
                </a-breadcrumb>
              </div>
            </div>
            <p
              v-if="fileStatus?.instanceFileTask && fileStatus.instanceFileTask > 0"
              style="color: #1677ff"
            >
              <a-spin />
              {{ t('TXT_CODE_c4e165ad', { num: fileStatus?.instanceFileTask }) }}
            </p>
            <a-spin :spinning="spinning">
              <a-table
                :row-selection="{
                selectedRowKeys: selectedRowKeys,
                onChange: selectChanged
              }"
                :row-key="(record: MscFileList) => record.name"
                :data-source="dataSource"
                :columns="columns"
                :scroll="{x: 'max-content'}"
                size="small"
                :pagination="{current: queryParams.page,pageSize: queryParams.limit,
                  total: total,hideOnSinglePage: false,showSizeChanger: true}"
                :custom-row="(record: MscFileList) => {
                    return {
                      onContextmenu: (e: MouseEvent) => handleRightClickRow(e, record as MscFileList)
                    };}"
                @change="(e: any) =>handleTableChange({ current: e.current || 0, pageSize: e.pageSize || 0 })"
                v-slot:bodyCell="{ column, record }: { column: any; record: MscFileList }"
              >
                <template v-if="column.key === 'name'">
                  <a-button
                    type="link"
                    class="file-name"
                    @click="handleClickFile(record as MscFileList)"
                  >
                      <span class="mr-4">
                        <component
                          :is="getFileIcon(record.name, record.type)"
                          style="font-size: 16px"
                        />
                      </span>
                    {{ record.name }}
                  </a-button>
                </template>
                <template v-if="column.key === 'action'">
                  <a-dropdown v-if="isPhone">
                    <template #overlay>
                      <a-menu mode="vertical" :items="menuList(record as MscFileList)"></a-menu>
                    </template>
                    <a-button size="middle">
                      {{ t('TXT_CODE_608994aa') }}
                      <DownOutlined />
                    </a-button>
                  </a-dropdown>
                  <a-space v-else>
                    <a-tooltip
                      v-for="(item, i) in (menuList(record as MscFileList) as any).filter(
                          (menu: any) => !menu.children
                        )"
                      :key="i"
                      :title="item.label"
                    >
                      <a-button
                        :icon="item.icon"
                        type="text"
                        size="small"
                        :style="item.style"
                        @click="
                            () => {
                              oneSelected(record.name, record as MscFileList);
                              item.onClick();
                            }
                          "
                      >
                      </a-button>
                    </a-tooltip>
                  </a-space>
                </template>
              </a-table>
            </a-spin>
          </template>
        </card-panel>
      </a-col>
    </a-row>
  </div>
  <a-modal
    v-model:open="dialog.show"
    :title="dialog.title"
    :confirm-loading="dialog.loading"
    :style="dialog.style"
    @ok="dialog.ok()"
    @cancel="dialog.cancel()"
  >
    <p>{{ dialog.info }}</p>

    <a-input
      v-if="dialog.mode == ''"
      :ref="dialog.ref"
      v-model:value="dialog.value"
      :placeholder="t('TXT_CODE_efaf2dad')"
    />

    <a-space v-if="dialog.mode == 'unzip'" direction="vertical" class="w-100">
      <a-typography-title :level="5">{{ t('TXT_CODE_5bd936d0') }}</a-typography-title>
      <a-radio-group v-model:value="dialog.unzipmode">
        <a-radio-button value="0">{{ t('TXT_CODE_012fe73c') }}</a-radio-button>
        <a-radio-button value="1">{{ t('TXT_CODE_1f3bf88b') }}</a-radio-button>
      </a-radio-group>

      <a-input
        v-if="dialog.unzipmode == '1'"
        v-model:value="dialog.value"
        :placeholder="t('TXT_CODE_6fe33ee3')"
      />
    </a-space>

    <a-space v-if="dialog.mode == 'zip'" direction="vertical" class="w-100">
      <a-input
        :ref="dialog.ref"
        v-model:value="dialog.value"
        :placeholder="t('TXT_CODE_86ac872f')"
        addon-after=". zip"
      />
    </a-space>

    <a-space v-if="dialog.mode == 'unzip'" direction="vertical" class="w-100 mt-16">
      <a-typography-title :level="5">{{ t('TXT_CODE_4db2747a') }}</a-typography-title>
      <a-typography-text type="secondary">
        {{ t('TXT_CODE_f59c59ae') }}
        <br />
        {{ t('TXT_CODE_0b63cd66') }}
        <br />
        {{ t('TXT_CODE_3d7f0186') }}
      </a-typography-text>
      <a-radio-group v-model:value="dialog.code">
        <a-radio-button value="utf-8">UTF-8</a-radio-button>
        <a-radio-button value="gbk">GBK</a-radio-button>
        <a-radio-button value="big5">BIG5</a-radio-button>
      </a-radio-group>
    </a-space>

    <a-space v-if="dialog.mode == 'zip'" direction="vertical" class="w-100 mt-16">
      <a-typography-text>
        {{ t('TXT_CODE_9ebb1950') }}
      </a-typography-text>
    </a-space>

    <a-space v-if="dialog.mode == 'permission'" direction="vertical" class="w-100 select-none">
      <a-spin :spinning="permission.loading">
        <div class="flex-between permission">
          <a-checkbox-group
            v-for="item in permission.item"
            :key="item.key"
            v-model:value="permission.data[item.role]"
          >
            <a-row class="direction-column son">
              <a-typography-text class="mb-10">
                <strong>{{ item.key }}</strong>
              </a-typography-text>
              <a-col class="mb-10 options">
                <a-checkbox value="4">{{ t('TXT_CODE_86d40a49') }}</a-checkbox>
              </a-col>
              <a-col class="mb-10 options">
                <a-checkbox value="2">{{ t('TXT_CODE_64d6d0f9') }}</a-checkbox>
              </a-col>
              <a-col class="mb-10 options">
                <a-checkbox value="1">{{ t('TXT_CODE_088257d4') }}</a-checkbox>
              </a-col>
            </a-row>
          </a-checkbox-group>
        </div>
        <a-checkbox v-model:checked="permission.deep" class="mt-15">
          {{ t('TXT_CODE_d745c975') }}
        </a-checkbox>
      </a-spin>
    </a-space>
  </a-modal>

  <file-editor
    v-if="daemonId && instanceId"
    ref="FileEditorDialog"
    :daemon-id="daemonId"
    :instance-id="instanceId"
    @save="getFileList"
  />
</template>

<style lang="scss" scoped>
.search-input {
  transition: all 0.4s;
  text-align: center;
  width: 50%;
}

.file-name {
  color: inherit;

  &:hover {
    color: #1677ff;
  }
}

.upload-tip {
  position: absolute;
  right: 0;
  left: 0;
  top: 0;
  bottom: 0;
}

@media (max-width: 992px) {
  .search-input {
    transition: all 0.4s;
    text-align: center;
    width: 100% !important;
  }
}

.search-input:hover {
  width: 100%;
}

.file-breadcrumbs {
  border: 1px solid var(--color-gray-5);
  border-radius: 6px;
  flex: 1;

  .file-breadcrumbs-item {
    padding: 8px;
    cursor: pointer;
    display: inline-block;
    transition: all 0.4s;
    min-width: 32px;
    text-align: center;
  }

  .file-breadcrumbs-item:hover {
    background-color: var(--color-gray-4);
  }
}

@media (max-width: 350px) {
  .permission {
    flex-direction: column;

    .son {
      width: 100%;
    }
  }
}
</style>
