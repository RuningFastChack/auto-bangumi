import type {IPageQuery} from '@/api/types';

export type RssManage = {
  id?: number;
  officialTitle: string;
  season: string;
  status: '0' | '1';
  filter?: string[];
  posterLink?: string;
  savePath?: string;
  complete: '0' | '1';
  updateWeek: number;
  sendDate: string;
  rssList: RSS[];
};

export type RssManageList = {
  id: number;
  lastEpisodeNum: string;
  officialTitle: string;
  season: string;
  status: '0' | '1';
  savePath: string;
  complete: '0' | '1';
  updateWeek: number;
  sendDate: string;
};

export type RssManageCalendar = {
  id: number;
  lastEpisodeNum: string;
  officialTitle: string;
  season: string;
  updateWeek: number;
  sendDate: string;
  posterLink: string;
};

export type RssManageQuery = IPageQuery & {
  officialTitle?: string;
  season?: string;
  status?: '0' | '1';
  complete?: '0' | '1';
  updateWeek?: number;
  sendDateForm?: string;
  sendDateTo?: string;
  sendDate?:[]
};

export type RSS = {
  id?: string;
  rss: string;
  translationGroup: string;
  sort: number;
  status: '0' | '1';
  type: 'Mikan';
  subGroupId?: string;
};
