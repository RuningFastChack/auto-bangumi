import http from '@/api';
import type { IPage } from '@/api/types';
import { ADMIN_MODULE } from '@/api/helper/prefix';
import type {
  FileChangePermission,
  FileCompress,
  FileDownloadConfig,
  FileExtract,
  FileStatus,
  FileUploadConfig,
  MscFileList,
  MscFileManageListQuery,
  MscFileUpdate
} from '@/api/types/mcs/files';

/**
 * 文件列表
 * @param params
 */
export const findFileManagePage = (params: MscFileManageListQuery) => {
  return http.get<IPage<MscFileList>>(ADMIN_MODULE + 'v1/file/manage/page', params);
};

/**
 * 文件状态
 */
export const findFileStatus = () => {
  return http.get<FileStatus>(ADMIN_MODULE + 'v1/file/manage/status');
};

/**
 * 获取下载文件配置
 * @param params
 */
export const getDownloadConfig = (params: { filename: string }) => {
  return http.get<FileDownloadConfig>(ADMIN_MODULE + 'v1/file/manage/download', params);
};

/**
 * 获取上传文件配置
 * @param params
 */
export const getUploadConfig = (params: { uploadDir: string,filename: string }) => {
  return http.get<FileUploadConfig>(ADMIN_MODULE + 'v1/file/manage/upload', params);
};

/**
 * 获取文件内容
 * @param params
 */
export const getFileContent = (params: MscFileUpdate) => {
  return http.get<string>(ADMIN_MODULE + 'v1/file/manage/content', params);
};

/**
 * 更新文件内容
 * @param params
 */
export const updateFileContent = (params: MscFileUpdate) => {
  return http.put<boolean>(ADMIN_MODULE + 'v1/file/manage/content', params);
};

/**
 * 修改文件权限
 * @param params
 */
export const changeFilePermission = (params: FileChangePermission) => {
  return http.put<boolean>(ADMIN_MODULE + 'v1/file/manage/chmod', params);
};

/**
 * 复制文件
 * @param params
 */
export const copyFile = (params: { targets: string[][] }) => {
  return http.post<boolean>(ADMIN_MODULE + 'v1/file/manage/copy', params);
};

/**
 * 移动或重命名
 * @param params
 */
export const moveFile = (params: { targets: string[][] }) => {
  return http.put<boolean>(ADMIN_MODULE + 'v1/file/manage/move', params);
};

/**
 * 压缩文件
 * @param params
 */
export const compressFile = (params: FileCompress) => {
  return http.post<boolean>(ADMIN_MODULE + 'v1/file/manage/compress', params);
};

/**
 * 解压文件
 * @param params
 */
export const extractFile = (params: FileExtract) => {
  return http.put<boolean>(ADMIN_MODULE + 'v1/file/manage/compress', params);
};

/**
 * 创建文件
 * @param params
 */
export const createFileTouch = (params: { target: string }) => {
  return http.post<boolean>(ADMIN_MODULE + 'v1/file/manage/create', params);
};

/**
 * 创建文件夹
 * @param params
 */
export const createFileMkdir = (params: { target: string }) => {
  return http.put<boolean>(ADMIN_MODULE + 'v1/file/manage/create', params);
};

/**
 * 删除文件
 * @param params
 */
export const removeFile = (params: { targets: string[] }) => {
  return http.delete<boolean>(ADMIN_MODULE + 'v1/file/manage', params);
};

