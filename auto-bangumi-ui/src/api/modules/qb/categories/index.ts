import http from '@/api';
import { ADMIN_MODULE } from '@/api/helper/prefix';
import type { CategoriesListResponse } from '@/api/types/qb/categories';

/**
 * 查询所有分类
 */
export const findCategoriesList = () => {
  return http.get<CategoriesListResponse[]>(ADMIN_MODULE + 'v1/qb/categories/list');
};
