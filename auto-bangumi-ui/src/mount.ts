import {createApp} from 'vue';
import pinia from '@/stores';

import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import '@/assets/base.scss';
import '@/assets/bg-extend-theme.scss';
import '@/assets/global.scss';
import '@/assets/tools.scss';
import '@/assets/variables.scss';
import '@/assets/variables-dark.scss';

import App from './App.vue';
import errorHandler from '@/utils/errorHandler.ts';
import {getI18nInstance} from '@/lang/i18n';
import {isLocalEnv} from '@/utils';
import router from './router';

export async function mountApp() {

  const app = createApp(App);

  app.use(pinia);

  app.use(router);

  app.use(Antd);

  app.use(getI18nInstance());

  if (!isLocalEnv()) app.config.errorHandler = errorHandler;

  app.mount('#app');
}
