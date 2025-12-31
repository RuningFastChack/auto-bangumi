import http from '@/api';
import { ADMIN_MODULE } from '@/api/helper/prefix';
import type { AnalysisResult } from '@/api/types';

export const analysisMikan = (rss: string,downImage:boolean) => {
  return http.get<AnalysisResult>(ADMIN_MODULE + 'v1/analysis/mikan', { rss: rss ,downImage:downImage});
};
