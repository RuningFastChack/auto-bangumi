// 请求响应参数（不包含data）
import type {User} from "@/api/types/user.ts";

export type IResult = {
  code: string;
  message: string;
};

// 请求响应参数（包含data）
export type IResultData<T = any> = IResult & {
  data: T;
};

export type IPage<T = any> = {
  current: number;
  limit: number;
  total: number;
  rows: T[];
  param?: { [key: string]: any } | string;
};

export type IPageQuery = {
  page?: number;
  limit?: number;
};

export type AnalysisResult = {
  title: string;
  titleEn: string;
  titleJp: string;
  season: string;
  savePath: string;
  translationGroup: string;
  subGroupId: string;
  sendData: string;
  updateWeek: number;
  posterLink: string;
  config: RssManageConfig;
};

export type RssManageConfig = {
  latestEpisode: string,
  totalEpisode: string,
}

export type LoginInfo = {
  token: string;
  expire: number;
  user: User;
};

export type LoginDTO = {
  username: string;
  password: string;
};

export interface MountComponent<T = any> {
  destroyComponent?(delay?: number): void;
  emitResult?(data?: T): void;
}
