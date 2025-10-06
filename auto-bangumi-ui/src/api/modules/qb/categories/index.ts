import http from '@/api';
import {ADMIN_MODULE} from '@/api/helper/prefix';
import type {CategoriesListResponse, CategoriesRequest} from '@/api/types/qb/categories';

/**
 * 查询所有分类
 */
export const findCategoriesList = () => {
  return http.get<CategoriesListResponse[]>(ADMIN_MODULE + 'v1/qb/categories/list');
};

/**
 * 新增分类
 */
export const addCategories = (data: CategoriesRequest) => {
  return http.post<boolean>(ADMIN_MODULE + 'v1/qb/categories', data);
};

/**
 * 修改分类
 */
export const updateCategories = (data: CategoriesRequest) => {
  return http.put<boolean>(ADMIN_MODULE + 'v1/qb/categories', data);
};

/**
 * 删除分类
 */
export const removeCategories = (categories: string) => {
  return http.delete<boolean>(ADMIN_MODULE + `v1/qb/categories/${categories}`);
};
