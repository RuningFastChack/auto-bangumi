// 全局默认配置项

// 首页地址（默认）

export const HOME_URL: string = '/calendar';

// 登录页地址（默认）
export const LOGIN_URL: string = '/login';


// 路由白名单地址（必须是本地存在的路由 staticRouter.ts 中）
export const ROUTER_WHITE_LIST: string[] = ['/login'];

export const ROUTER_ERROR_LIST: string[] = ['/403'];

// 是否是预览环境
export const IS_PREVIEW: boolean = import.meta.env.VITE_PREVIEW === 'true';

export enum LayoutCardHeight {
  MINI = "100px",
  SMALL = "200px",
  MEDIUM = "400px",
  BIG = "600px",
  LARGE = "800px",
  AUTO = "unset"
}


