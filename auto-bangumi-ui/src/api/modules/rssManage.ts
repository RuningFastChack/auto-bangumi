import http from '@/api';
import type { IPage } from '@/api/types';
import { ADMIN_MODULE } from '@/api/helper/prefix';
import type {
  RssManage,
  RssManageCalendar,
  RssManageList,
  RssManageQuery
} from '@/api/types/rss/rssManage';

/**
 * 分页
 * @param params
 */
export const findRssManagePage = (params: RssManageQuery) => {
  return http.get<IPage<RssManageList>>(ADMIN_MODULE + 'v1/rss/manage/page', params);
};

/**
 * 日历
 */
export const findRssManageCalendar = () => {
  return http.get<Record<number, RssManageCalendar[]>>(ADMIN_MODULE + 'v1/rss/manage/calendar');
};

/**
 * 详情
 * @param id
 */
export const findRssManageDetail = (id: number) => {
  return http.get<RssManage>(ADMIN_MODULE + 'v1/rss/manage', { id: id });
};

/**
 * 创建
 * @param params
 */
export const createRssManage = (params: RssManage) => {
  return http.post<RssManage>(ADMIN_MODULE + 'v1/rss/manage', params);
};

/**
 * 编辑
 * @param params
 */
export const updateRssManage = (params: RssManage) => {
  return http.put<RssManage>(ADMIN_MODULE + 'v1/rss/manage', params);
};

/**
 * 修改状态
 * @param params
 */
export const changeRssManageStatusAndComplete = (params: { id: number; status?: string; complete?: string }) => {
  return http.put(ADMIN_MODULE + 'v1/rss/manage/change', params);
};

/**
 * 删除
 * @param id
 */
export const removeRssManage = (id: number) => {
  return http.delete(ADMIN_MODULE + 'v1/rss/manage', { id: id });
};

/**
 * 刷新海报
 * @param ids
 */
export const refreshPosterApi = (ids?: number[]) => {
  return http.put<RssManage>(ADMIN_MODULE + 'v1/rss/manage/refresh/poster', ids);
};

/**
 * 刷新订阅
 * @param ids
 */
export const refreshRssManageByIds = (ids: number[]) => {
  return http.put<RssManage>(ADMIN_MODULE + 'v1/rss/manage/refresh/manage', ids);
};
