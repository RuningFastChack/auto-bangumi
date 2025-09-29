import type { IPageQuery } from '@/api/types';

export type RssItem = {
  id?: number;
  torrentCode?: string;
  episodeNum?: string;
  rssManageId?: number;
  translationGroup?: string;
  savePath?: string;
  subGroupId?: string;
  torrentName?: string;
  name?: string;
  url?: string;
  homepage?: string;
  downloaded?: '0' | '1';
  pushed?: '0' | '1';
};

export type RssItemQuery = IPageQuery & {
  rssManageId?: number;
  translationGroup?: string;
  torrentName?: string;
  name?: string;
  downloaded?: '0' | '1';
  pushed?: '0' | '1';
};

export type RssItemList = {
  id: number;
  torrentCode: string;
  episodeNum: string;
  rssManageId: number;
  translationGroup: string;
  savePath: string;
  torrentName: string;
  name: string;
  downloaded: '0' | '1';
  pushed: '0' | '1';
};
