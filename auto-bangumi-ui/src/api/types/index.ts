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
  subGroupId: string;
  translationGroup: string;
  season: string;
  title: string;
  sendData: string;
  posterLink: string;
  updateWeek: number;
};

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
