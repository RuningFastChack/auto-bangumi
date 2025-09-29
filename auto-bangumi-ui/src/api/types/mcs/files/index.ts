import type { IPageQuery } from '@/api/types';

export type MscFileManageListQuery = IPageQuery & {
  target?: string,
  filename?: string
}

export type MscFileList = {
  absolutePath: string,
  name: string,
  size: number,
  time: string,
  mode: number,
  type: '0' | '1'
}

export type MscFileUpdate = {
  target: string,
  text?: string
}

export type FileCompress = {
  code: 'utf-8' | 'gbk' | 'big5',
  source: string,
  targets: string[]
}

export type FileExtract = FileCompress & {
  targets: string
}

export type McsManageSecurityResponse = {
  addr: string,
  password: string
}

export type FileChangePermission = {
  chmod: number,
  deep?: boolean,
  target?: string
}

export type FileDownloadConfig = McsManageSecurityResponse & {}

export type FileUploadConfig = McsManageSecurityResponse & {}

export interface Permission {
  data: {
    owner: string[];
    usergroup: string[];
    everyone: string[];
  };
  deep: boolean;
  loading: boolean;
  item: {
    key: string;
    role: 'owner' | 'usergroup' | 'everyone';
  }[];
}

export interface Breadcrumb {
  path: string;
  name: string;
  disabled: boolean;
}

export interface FileStatus {
  instanceFileTask: number;
  globalFileTask: number;
  platform: string;
  isGlobalInstance: boolean;
  disks: string[];
}
