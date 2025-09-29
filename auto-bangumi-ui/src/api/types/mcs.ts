export type IResult = {
  status: number;
  message: string;
};

// 请求响应参数（包含data）
export type IResultData<T = any> = IResult & {
  data: T;
};
