import http from '@/api';
import {ADMIN_MODULE} from '@/api/helper/prefix';
import type {
  TorrentsInfoAddRequest,
  TorrentsInfoListRequest,
  TorrentsInfoListResponse,
} from '@/api/types/qb/torrents';

/**
 * 分页查询种子列表
 */
export const findTorrentsPage = (params?: TorrentsInfoListRequest) => {
  return http.get<TorrentsInfoListResponse[]>(ADMIN_MODULE + 'v1/qb/torrents/list', params);
};

/**
 * 新增种子
 */
export const addTorrent = (data: TorrentsInfoAddRequest) => {
  return http.post<boolean>(ADMIN_MODULE + 'v1/qb/torrents', data);
};

/**
 * 暂停种子
 */
export const pauseTorrent = (torrents: string[]) => {
  return http.put<boolean>(ADMIN_MODULE + 'v1/qb/torrents/pause', torrents);
};

/**
 * 恢复种子
 */
export const resumeTorrent = (torrents: string[]) => {
  return http.put<boolean>(ADMIN_MODULE + 'v1/qb/torrents/resume', torrents);
};

/**
 * 删除种子
 */
export const deleteTorrent = (torrents: string[]) => {
  return http.delete<boolean>(ADMIN_MODULE + 'v1/qb/torrents', torrents);
};
