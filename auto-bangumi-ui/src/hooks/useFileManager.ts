import { computed, createVNode, reactive, ref, type VNodeRef } from 'vue';
import type {
  Breadcrumb,
  FileExtract,
  FileStatus,
  MscFileList,
  MscFileManageListQuery,
  Permission
} from '@/api/types/mcs/files';
import { reportErrorMsg } from '@/utils/validator.ts';
import {
  changeFilePermission,
  compressFile,
  copyFile,
  createFileMkdir,
  createFileTouch,
  extractFile,
  findFileManagePage,
  findFileStatus,
  getDownloadConfig,
  getUploadConfig,
  moveFile,
  removeFile
} from '@/api/modules/mcs/files';
import type { IPage } from '@/api/types';
import { message, Modal, type UploadProps } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { openLoadingDialog, useImageViewerDialog } from '@/components/fc';
import { parseForwardAddress } from '@/utils/protocol.ts';
import { useUserStore } from '@/stores/modules/user.ts';
import { number2permission, permission2number } from '@/utils/permission.ts';
import axios from 'axios';

export const useFileManager = () => {
  const fileStatus = ref<FileStatus>();
  const selectedRowKeys = ref<string[]>([]);
  const selectionData = ref<MscFileList[]>();
  const queryParams = ref<MscFileManageListQuery>({
    page: 1,
    limit: 10,
    filename: ''
  });
  const SearchData = ref<IPage<MscFileList>>({
    current: 0,
    total: 0,
    limit: 100,
    rows: []
  });
  const spinning = ref(false);
  const percentComplete = ref(0);
  const clipboard = ref<{
    type: 'copy' | 'move';
    value: string[];
  }>();
  const dialog = ref({
    show: false,
    title: 'Dialog',
    loading: false,
    value: '',
    info: '',
    mode: '',
    unzipmode: '0',
    code: 'utf-8',
    ref: ref<VNodeRef>(),
    ok: () => {
    },
    cancel: () => {
      dialog.value.value = '';
    },
    style: {}
  });
  const { userInfo } = useUserStore();

  const dataSource = computed<MscFileList[]>(() => SearchData.value.rows || []);

  const total = computed(() => SearchData.value.total || 0);

  const isMultiple = computed(() => selectionData.value && selectionData.value.length > 1);

  const breadcrumbs = reactive<Breadcrumb[]>([]);

  breadcrumbs.push({
    path: '/',
    name: '/',
    disabled: false
  });

  const openDialog = (
    title: string,
    info: string,
    defaultValue?: string,
    mode?: string,
    style?: object
  ): Promise<string> => {
    dialog.value.style = style || {};
    dialog.value.value = defaultValue || '';
    dialog.value.mode = mode || '';

    dialog.value.title = title;
    dialog.value.info = info;
    dialog.value.show = true;

    (dialog.value?.ref as any)?.focus();

    return new Promise((resolve) => {
      dialog.value.ok = () => {
        if (
          dialog.value.value == '' &&
          dialog.value.mode != 'unzip' &&
          dialog.value.mode != 'permission'
        ) {
          return reportErrorMsg('请输入内容');
        }
        resolve(dialog.value.value);
        dialog.value.show = false;
        dialog.value.value = '';
        dialog.value.info = '';
        dialog.value.mode = '';
        dialog.value.style = {};
        dialog.value.ok = () => {
        };
      };
    });
  };

  const getFileList = async () => {
    try {
      clearSelected();
      const { data } = await findFileManagePage({
        target: breadcrumbs[breadcrumbs.length - 1].path,
        filename: queryParams.value.filename,
        page: queryParams.value.page! - 1,
        limit: queryParams.value.limit
      });
      SearchData.value = data;
    } catch (error: any) {
    }
  };

  const reloadList = async () => {
    await getFileList();
    return message.success('刷新文件列表成功');
  };

  const touchFile = async (dir?: boolean) => {
    try {
      clearSelected();
      const dirname = dir
        ? await openDialog('新建目录', '请输入目录名称')
        : await openDialog('新建文件', '请输入文件名');
      const { data } = dir ? await createFileMkdir({ target: breadcrumbs[breadcrumbs.length - 1].path + dirname }) :
        await createFileTouch({ target: breadcrumbs[breadcrumbs.length - 1].path + dirname });
      await getFileList();
      data ? message.success('创建成功') : message.error('创建失败');
    } catch (error: any) {
    }
  };

  const setClipBoard = (type: 'copy' | 'move', file?: string) => {
    if (file) {
      clipboard.value = {
        type,
        value: [breadcrumbs[breadcrumbs.length - 1].path + file]
      };
    } else {
      if (!selectionData.value || selectionData.value.length === 0)
        return reportErrorMsg('请先选择文件');
      clipboard.value = {
        type,
        value: selectionData.value?.map((e) => breadcrumbs[breadcrumbs.length - 1].path + e.name)
      };
    }
    message.success('已保存至剪切板');
    clearSelected();
  };

  const paste = async () => {
    if (!clipboard?.value?.type || !clipboard.value.value)
      return reportErrorMsg('请先选择文件');
    try {
      let targets = clipboard.value.value.map((e) => [
        e,
        breadcrumbs[breadcrumbs.length - 1].path + e.split('/')[e.split('/').length - 1]
      ]);
      clipboard.value.type == 'copy' ? await copyFile({ targets: targets }) : await moveFile({ targets: targets });
      await getFileList();
      message.success('文件操作任务开始，如果文件数量过多，则需要一定时间');
      clearSelected();
      clipboard.value.value = [];
    } catch (error: any) {
    }
  };

  const resetName = async (file: string) => {
    const newname = await openDialog('重命名', '请输入新名称', file);
    try {
      await moveFile({
        targets: [
          [
            breadcrumbs[breadcrumbs.length - 1].path + file,
            breadcrumbs[breadcrumbs.length - 1].path + newname
          ]
        ]
      });
      message.success('重命名成功');
      await getFileList();
    } catch (error: any) {

    }
  };

  const deleteFile = async (file?: string) => {
    const useDeleteFileApi = async (files: string[]) => {
      try {
        await removeFile({ targets: files });
        await getFileList();
        message.success('文件删除任务开始，如果文件数量过多，则需要一定时间');
        if (dataSource?.value?.length === 0 && queryParams.value.page && queryParams.value.page > 1) {
          queryParams.value.page -= 1;
          await getFileList();
        }
      } catch (error: any) {
      }
    };

    Modal.confirm({
      title: '你确定要删除吗?',
      icon: createVNode(ExclamationCircleOutlined),
      content: createVNode('div', { style: 'color:red;' }, '删除后将无法恢复！'),
      async onOk() {
        if (!isMultiple.value) {
          // one file
          await useDeleteFileApi([breadcrumbs[breadcrumbs.length - 1].path + file]);
        } else {
          // more file
          if (!selectionData.value) return reportErrorMsg('请选择要删除的内容');
          await useDeleteFileApi(
            selectionData.value.map((e) => breadcrumbs[breadcrumbs.length - 1].path + e.name)
          );
        }
      },
      okType: 'danger',
      okText: '确定',
      class: 'test'
    });
    return;
  };

  const zipFile = async () => {
    if (!selectionData.value || selectionData.value.length === 0)
      return reportErrorMsg('请先选择文件');
    const filename = await openDialog('压缩文件', '请输入压缩后的文件名', '', 'zip');
    const loadingDialog = await openLoadingDialog(
      '处理中..',
      '正在压缩文件，请耐心等待...',
      '我们正在全力处理文件中！'
    );
    try {
      await compressFile({
        code: 'utf-8',
        source: breadcrumbs[breadcrumbs.length - 1].path + filename + '.zip',
        targets: selectionData.value.map((e) => breadcrumbs[breadcrumbs.length - 1].path + e.name)
      });
      message.success('任务执行完毕');
      await getFileList();
    } catch (error: any) {
      message.error('压缩任务执行失败');
    } finally {
      await loadingDialog.cancel();
    }
  };

  const unzipFile = async (name: string) => {
    const dirname = await openDialog('解压文件', '', '', 'unzip');
    const loadingDialog = await openLoadingDialog(
      '处理中..',
      '正在解压文件，请耐心等待...',
      '我们正在全力处理文件'
    );
    try {
      await extractFile(<FileExtract>{
        code: dialog.value.code,
        source: breadcrumbs[breadcrumbs.length - 1].path + name,
        targets:
          dialog.value.unzipmode == '0'
            ? breadcrumbs[breadcrumbs.length - 1].path
            : breadcrumbs[breadcrumbs.length - 1].path + dirname
      });
      message.success('任务执行完毕！');
      await getFileList();
    } catch (error: any) {
      message.error('解压任务执行失败！');
    } finally {
      await loadingDialog.cancel();
    }
  };

  const selectedFile = async (file: File) => {
    try {
      percentComplete.value = 1;
      const uploadDir = breadcrumbs[breadcrumbs.length - 1].path;
      const { data } = await getUploadConfig({
        uploadDir: uploadDir,
        filename: file.name
      });
      if (!data) {
        percentComplete.value = 0;
        throw new Error('获取上传地址失败');
      }

      let shouldOverwrite = true;
      if (dataSource.value?.find((dataType) => dataType.name === file.name)) {
        const confirmPromise = new Promise<boolean>((onComplete) => {
          Modal.confirm({
            title: '覆盖文件',
            icon: createVNode(ExclamationCircleOutlined),
            content: ['您上传的文件', file.name, '已经在目录中存在, 是否覆盖原文件?'].join(' '),
            onOk() {
              onComplete(true);
            },
            onCancel() {
              onComplete(false);
              percentComplete.value = 0;
            }
          });
        });
        if (!(await confirmPromise)) {
          shouldOverwrite = false;
          //return reportErrorMsg(t("TXT_CODE_8b14426e"));
        }
      }

      const uploadFormData = new FormData();
      uploadFormData.append('file', file);

      await axios.post(
        `${parseForwardAddress(data.addr, 'http')}/upload/${data.password}`,
        uploadFormData,
        {
          timeout: Number.MAX_VALUE,
          onUploadProgress: (progressEvent: any) => {
            const p = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            if (p >= 1) percentComplete.value = p;
          },
          params: {
            overwrite: shouldOverwrite
          }
        });

      await getFileList();
      percentComplete.value = 0;
      return message.success('上传成功');
    } catch (error: any) {
      percentComplete.value = 0;
      return reportErrorMsg(error.response?.data || error.message);
    }
  };
  const beforeUpload: UploadProps['beforeUpload'] = async (file) => {
    await selectedFile(file);
    return false;
  };

  const selectChanged = (_selectedRowKeys: string[], selectedRows: MscFileList[]) => {
    selectionData.value = selectedRows;
    selectedRowKeys.value = _selectedRowKeys;
  };

  const pushSelected = (key: string, row: MscFileList) => {
    const index = selectedRowKeys.value.indexOf(key);
    if (index > -1) return;
    selectionData.value?.push(row);
    selectedRowKeys.value.push(key);
  };

  const oneSelected = (key: string, row: MscFileList) => {
    const index = selectedRowKeys.value.indexOf(key);
    if (index > -1) return;
    selectionData.value = [row];
    selectedRowKeys.value = [key];
  };

  const clearSelected = () => {
    selectionData.value = [];
    selectedRowKeys.value = [];
  };

  const rowClickTable = async (item: string, type: string) => {
    if (type === '1') return;
    try {
      spinning.value = true;
      breadcrumbs.push({
        path: `${breadcrumbs[breadcrumbs.length - 1].path}${item}/`,
        name: item,
        disabled: false
      });
      queryParams.value.filename = '';
      queryParams.value.page = 1;
      await getFileList();
    } catch (error: any) {
      breadcrumbs.splice(breadcrumbs.length - 1, 1);
      return reportErrorMsg(error.message);
    } finally {
      spinning.value = false;
    }
  };

  const getFileLink = async (fileName: string, frontDir?: string) => {
    frontDir = frontDir || breadcrumbs[breadcrumbs.length - 1].path;
    try {
      const { data } = await getDownloadConfig({ filename: frontDir + fileName });

      if (!data) return null;
      return `${parseForwardAddress(data.addr, 'http')}/download/${
        data.password
      }/${fileName}`;
    } catch (err: any) {
      console.error(err);
      return reportErrorMsg(err.message);
    }
  };

  const downloadFile = async (fileName: string) => {
    const link = await getFileLink(fileName);
    if (!link) throw new Error('获取下载地址失败');
    window.open(link);
  };

  const handleChangeDir = async (dir: string) => {
    if (breadcrumbs.findIndex((e) => e.path === dir) === -1)
      return reportErrorMsg('找不到路径');
    spinning.value = true;
    breadcrumbs.splice(breadcrumbs.findIndex((e) => e.path === dir) + 1);
    queryParams.value.filename = '';
    queryParams.value.page = 1;
    await getFileList();
    spinning.value = false;
  };

  const handleTableChange = async (e: { current: number; pageSize: number }) => {
    selectedRowKeys.value = [];
    selectionData.value = [];
    queryParams.value.filename = '';
    queryParams.value.page = e.current;
    queryParams.value.limit = e.pageSize;
    await getFileList();
  };

  const handleSearchChange = async () => {
    queryParams.value.page = 1;
    await getFileList();
  };

  const getFileStatus = async () => {
    try {
      const { data } = await findFileStatus();
      if (data) {
        fileStatus.value = data;
      }
    } catch (err: any) {
      console.error(err);
    }
  };

  const permission = reactive<Permission>({
    data: {
      owner: [],
      usergroup: [],
      everyone: []
    },
    deep: false,
    loading: false,
    item: [
      {
        key: '所有者',
        role: 'owner'
      },
      {
        key: '用户组',
        role: 'usergroup'
      },
      {
        key: '任何人',
        role: 'everyone'
      }
    ]
  });

  const changePermission = async (name: string, mode: number) => {
    permission.loading = true;
    permission.data = number2permission(mode);
    permission.loading = false;
    await openDialog('更改权限', '', '', 'permission', {
      maxWidth: '400px'
    });
    try {
      await changeFilePermission({
        chmod: permission2number(
          permission.data.owner,
          permission.data.usergroup,
          permission.data.everyone
        ),
        deep: permission.deep,
        target: breadcrumbs[breadcrumbs.length - 1].path + name
      });
      message.success('更改权限成功');
      await getFileList();
    } catch (err: any) {
    }
    permission.deep = false;
  };

  const currentDisk = ref('程序根目录');

  const toDisk = async (disk: string) => {
    spinning.value = true;
    try {
      breadcrumbs.splice(0, breadcrumbs.length);
      breadcrumbs.push({
        path: disk === '/' ? disk : disk + ':\\',
        name: '/',
        disabled: false
      });
      queryParams.value.filename = '';
      queryParams.value.page = 1;
      await getFileList();
    } finally {
      spinning.value = false;
    }
  };

  const isImage = (extName: string) => {
    if (!extName) return;
    return ['JPG', 'JPEG', 'PNG', 'GIF', 'BMP', 'WEBP', 'ICO'].includes(extName.toUpperCase());
  };

  const showImage = async (file: MscFileList) => {
    const frontDir = breadcrumbs[breadcrumbs.length - 1].path;
    await useImageViewerDialog(userInfo.config?.mcsManageSetting.instanceId || '',
      userInfo.config?.mcsManageSetting.daemonId || '', file.name, frontDir);
  };

  return {
    fileStatus,
    dialog,
    percentComplete,
    spinning,
    queryParams,
    SearchData,
    total,
    dataSource,
    breadcrumbs,
    permission,
    clipboard,
    selectedRowKeys,
    currentDisk,
    selectionData,
    beforeUpload,
    selectChanged,
    isMultiple,
    openDialog,
    getFileList,
    touchFile,
    reloadList,
    setClipBoard,
    paste,
    resetName,
    deleteFile,
    zipFile,
    unzipFile,
    selectedFile,
    rowClickTable,
    downloadFile,
    getFileLink,
    handleChangeDir,
    handleTableChange,
    handleSearchChange,
    getFileStatus,
    changePermission,
    toDisk,
    pushSelected,
    oneSelected,
    isImage,
    showImage
  };
};
