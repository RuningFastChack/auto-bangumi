import http from '@/api';
import { ADMIN_MODULE } from '@/api/helper/prefix';
import type { LoginDTO, LoginInfo } from '@/api/types';

export const loginApi = (params: LoginDTO) => {
  return http.post<LoginInfo>(ADMIN_MODULE + 'v1/auth/login', params);
};

export const logoutApi = () => {
  return http.post(ADMIN_MODULE + 'v1/auth/logout');
};
