<script setup lang="ts">
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { sleep } from '@/utils/common.ts';
import type { LoginDTO } from '@/api/types';
import { loginApi } from '@/api/modules/login.ts';
import { useUserStore } from '@/stores/modules/user.ts';
import CardPanel from '@/components/CardPanel.vue';
import router from '@/router';
import {
  CheckCircleOutlined,
  LoadingOutlined,
  LockOutlined,
  UserOutlined
} from '@ant-design/icons-vue';
import { useRoute } from 'vue-router';
import { useAppRouters } from '@/hooks/useAppRouters.ts';
//region type
const userStore = useUserStore();
const appRouters = useAppRouters();
//endregion

//region props & emit

//endregion

//region refs & data

const formData = reactive<LoginDTO>({
  username: '',
  password: ''
});


const loginStep = ref<number>(0);
//endregion

//region computed

//endregion

//region watch

//endregion

//region methods

const handleLogin = async () => {
  if (!formData.username.trim() || !formData.password.trim()) {
    return message.error('请完善账号信息');
  }
  try {
    loginStep.value = 1;
    await sleep(1000);
    const { data } = await loginApi(formData);
    userStore.setToken(data.token);
    userStore.setExpireTime(data.expire);
    userStore.setUserInfo(data.user);
    loginStep.value = 2;
    await sleep(1000);
    loginSuccess();
  } catch (error: any) {
    loginStep.value = 0;
  }
};

const loginSuccess = () => {
  appRouters.toPage({ path: '/' });
};
//endregion

//region otherMethods
defineOptions({ name: 'LoginCard' });
//endregion

</script>

<template>
  <div
    :class="{
      logging: loginStep === 1,
      loginDone: loginStep === 3,
      'w-100': true,
      'h-100': true
    }"
  >
    <card-panel class="login-panel">
      <template #body>
        <div v-show="loginStep === 0" class="login-panel-body">
          <a-typography-title :level="3" class="mb-20 glitch-wrapper">
            <div
              class="glitch"
              :data-text="'用户验证'"
            >
              用户验证
            </div>
          </a-typography-title>
          <a-typography-paragraph class="mb-20">
            使用服务器的 AutoBangumi 账号进入面板
          </a-typography-paragraph>
          <div class="account-input-container">
            <form @submit.prevent>
              <div>
                <a-input
                  v-model:value="formData.username"
                  class="account"
                  size="large"
                  name="mcsm-name-input"
                  placeholder="账号"
                >
                  <template #suffix>
                    <UserOutlined style="color: rgba(0, 0, 0, 0.45)" />
                  </template>
                </a-input>
                <a-input
                  v-model:value="formData.password"
                  class="mt-20 account"
                  type="password"
                  placeholder="密码"
                  size="large"
                  name="mcsm-pw-input"
                  @press-enter="handleLogin"
                >
                  <template #suffix>
                    <LockOutlined style="color: rgba(0, 0, 0, 0.45)" />
                  </template>
                </a-input>
              </div>
            </form>

            <div class="mt-24 flex-between align-center">
              <div></div>
              <div class="justify-end" style="gap: 10px">
                <a-button size="large" type="primary" style="min-width: 95px" @click="handleLogin">
                  登录
                </a-button>
              </div>
            </div>
          </div>
        </div>
        <div v-show="loginStep === 1" class="login-panel-body flex-center">
          <div style="text-align: center">
            <LoadingOutlined class="logging-icon"
                             :style="{ fontSize: '62px', fontWeight: 800 }" />
          </div>
        </div>
        <div v-show="loginStep >= 2" class="login-panel-body flex-center">
          <div style="text-align: center">
            <CheckCircleOutlined
              class="login-success-icon"
              :style="{
                fontSize: '62px',
                color: 'var(--color-green-6)'
              }"
            />
          </div>
        </div>
      </template>
    </card-panel>
  </div>
</template>

<style lang="scss">
.account-input-container {
  input:-webkit-autofill {
    -webkit-text-fill-color: var(--color-gray-8) !important;
    -webkit-box-shadow: 0 0 0 1000px transparent inset !important;
    background-color: transparent !important;
    background-image: none;
    transition: background-color 99999s ease-in-out 0s;
  }

  input {
    background-color: transparent;
    caret-color: #fff;
  }
}
</style>

<style lang="scss" scoped>
.logging {
  .login-panel {
    transform: scale(0.94);
    border: 2px solid var(--color-blue-5);
    box-shadow: 0 0 20px rgba(28, 120, 207, 0.3);
  }
}

.login-panel {
  margin: 0 auto;
  transition: all 0.4s;
  width: 100%;
  // backdrop-filter: saturate(120%) blur(12px);
  background-color: var(--login-panel-bg);

  .login-panel-body {
    padding: 28px 24px;
    min-height: 322px;
  }
}

.mcsmanager-link {
  font-size: var(--font-body);
  text-align: right;
  color: var(--color-gray-7);

  a {
    color: var(--color-gray-7) !important;
    text-decoration: underline;
  }
}

.logging-icon {
  animation: opacityAnimation 0.4s;
}

.login-success-icon {
  animation: scaleAnimation 0.4s;
}

@keyframes opacityAnimation {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes scaleAnimation {
  0% {
    transform: scale(0);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes moveAnimation {
  0% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(0, 8px);
  }
  50% {
    transform: translate(8px, 8px);
  }
  75% {
    transform: translate(8px, 0);
  }
  100% {
    transform: translate(0, 0);
  }
}

@keyframes moveAnimation2 {
  0% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(0, 2px);
  }
  50% {
    transform: translate(2px, 2px);
  }
  75% {
    transform: translate(2px, 0);
  }
  100% {
    transform: translate(0, 0);
  }
}

.glitch-wrapper {
  position: relative;
  overflow: hidden;
}

.glitch {
  position: relative;
  font-weight: 600;
  animation: glitch-trigger 4s infinite;

  &::before,
  &::after {
    content: attr(data-text);
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: transparent;
    overflow: hidden;
    opacity: 0;
    pointer-events: none;
  }

  &::before {
    color: #ff0040;
    animation: glitch-anim-1 4s infinite;
  }

  &::after {
    color: #00ffff;
    animation: glitch-anim-2 4s infinite;
  }
}

@keyframes glitch-trigger {
  0%,
  96% {
    transform: translate(0);
  }
  97%,
  100% {
    transform: translate(-1px, 1px);
  }
}

@keyframes glitch-anim-1 {
  0%,
  96% {
    transform: translate(0);
    opacity: 0;
    clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  }
  97% {
    transform: translate(-2px, -2px);
    opacity: 0.7;
    clip-path: polygon(0 0, 100% 0, 100% 35%, 0 35%);
  }
  98% {
    transform: translate(2px, 1px);
    opacity: 0.8;
    clip-path: polygon(0 35%, 100% 35%, 100% 70%, 0 70%);
  }
  99% {
    transform: translate(-1px, 2px);
    opacity: 0.9;
    clip-path: polygon(0 70%, 100% 70%, 100% 100%, 0 100%);
  }
  100% {
    transform: translate(0);
    opacity: 0;
    clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  }
}

@keyframes glitch-anim-2 {
  0%,
  96% {
    transform: translate(0);
    opacity: 0;
    clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  }
  97% {
    transform: translate(2px, 1px);
    opacity: 0.6;
    clip-path: polygon(0 0, 100% 0, 100% 25%, 0 25%);
  }
  98% {
    transform: translate(-2px, -1px);
    opacity: 0.7;
    clip-path: polygon(0 25%, 100% 25%, 100% 75%, 0 75%);
  }
  99% {
    transform: translate(1px, -2px);
    opacity: 0.8;
    clip-path: polygon(0 75%, 100% 75%, 100% 100%, 0 100%);
  }
  100% {
    transform: translate(0);
    opacity: 0;
    clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  }
}
</style>
