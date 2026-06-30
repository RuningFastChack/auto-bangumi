import http from '@/api';
import { ADMIN_MODULE } from '@/api/helper/prefix';
import type { LoginDTO } from '@/api/types';
import type { UserConfig } from '@/api/types/user.ts';

export const updateConfig = (params: UserConfig) => {
  return http.put(ADMIN_MODULE + 'v1/user', params);
};

export const updateLoginInfo = (params: LoginDTO) => {
  return http.post(ADMIN_MODULE + 'v1/user/update/login/info', params);
};

export const findUserConfig = () => {
  return http.get<UserConfig>(ADMIN_MODULE + 'v1/user/config');
};
