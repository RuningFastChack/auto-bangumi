import http from '@/api';
import { ADMIN_MODULE } from '@/api/helper/prefix.ts';
import type {
  MissionPassportResponse,
  ProtectedInstance
} from '@/api/types/mcs/protected/instance';

/**
 * 启动实例
 */
export const openInstance = () => {
  return http.get<ProtectedInstance>(ADMIN_MODULE + 'v1/protected/instance/open');
};

/**
 * 停止实例
 */
export const stopInstance = () => {
  return http.get<ProtectedInstance>(ADMIN_MODULE + 'v1/protected/instance/stop');
};

/**
 * 重启实例
 */
export const restartInstance = () => {
  return http.get<ProtectedInstance>(ADMIN_MODULE + 'v1/protected/instance/restart');
};

/**
 * 强制结束实例进程
 */
export const killInstance = () => {
  return http.get<ProtectedInstance>(ADMIN_MODULE + 'v1/protected/instance/kill');
};

export const setUpTerminalStreamChannel = () => {
  return http.post<MissionPassportResponse>(ADMIN_MODULE + 'v1/protected/instance/stream/channel');
};

/**
 * 获取实例输出日志
 */
export const getInstanceOutputLog = (size?: number | string) => {
  return http.get<string>(ADMIN_MODULE + 'v1/protected/instance/log', { size: size });
};
