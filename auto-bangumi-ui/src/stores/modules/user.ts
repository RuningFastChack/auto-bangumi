import { defineStore } from 'pinia';
import { ref } from 'vue';
import piniaPersistConfig from '@/stores/helper/persist';
import type { User } from '@/api/types/user.ts';

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string>('');
    const expireTime = ref<number>(-1);
    const userInfo = ref<User>({
      username: ''
    });

    function setToken(tokenStr: string) {
      token.value = tokenStr;
    }

    // Set setUserInfo
    function setUserInfo(info: User) {
      userInfo.value = info;
    }

    function clear() {
      token.value = '';
      expireTime.value = -1;
      userInfo.value = {
        username: ''
      };
    }

    function isLogged(): boolean {
      if (expireTime.value === -1) return false; // 未登录
      const remaining = expireTime.value - Date.now();
      return remaining > 10 * 1000; // 剩余时间大于 10 秒才算有效
    }

    /**
     * 设置过期时间
     * @param value 秒
     */
    function setExpireTime(value: number) {
      if (value <= 0) {
        expireTime.value = -1;
        return;
      }
      // 计算绝对过期时间戳（毫秒）
      expireTime.value = Date.now() + value * 1000;
    }

    function updateUserInfo(partial: Partial<User>) {
      userInfo.value = {
        ...userInfo.value,
        ...partial
      };
    }

    return {
      token,
      userInfo,
      expireTime,
      isLogged,
      setToken,
      setExpireTime,
      setUserInfo,
      updateUserInfo,
      clear
    };
  },
  {
    persist: piniaPersistConfig('user')
  }
);
