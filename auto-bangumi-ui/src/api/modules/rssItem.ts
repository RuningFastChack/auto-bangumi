import http from '@/api';
import type { AnalysisResult, IPage } from '@/api/types';
import { ADMIN_MODULE } from '@/api/helper/prefix';
import type { RssItem, RssItemList, RssItemQuery } from '@/api/types/rss/rssItem';

export const updateRssItemById = (params: RssItem) => {
  return http.put<AnalysisResult>(ADMIN_MODULE + 'v1/rss/item', params);
};

export const findRssItemPage = (params: RssItemQuery) => {
  return http.get<IPage<RssItemList>>(ADMIN_MODULE + 'v1/rss/item/page', params);
};

/**
 * 修改列表信息
 * @param params
 */
export const updateRssItemToList = (params: { id: number; downloaded?: string; pushed?: string; name?: string }) => {
  return http.put(ADMIN_MODULE + 'v1/rss/item/update', params);
};

/**
 * 推送指定订阅
 * @param torrentCodes
 */
export const pushRssItemToDownLoad = (torrentCodes: string[]) => {
  return http.put(ADMIN_MODULE + 'v1/rss/item/push', torrentCodes);
};

/**
 * 手动触发可推送番剧
 */
export const triggerPushLastRssItem = () => {
  return http.post(ADMIN_MODULE + 'v1/rss/item/refresh/last', {});
};
