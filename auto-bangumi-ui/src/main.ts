import { createApp } from 'vue';
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
import router from './router';
import errorHandler from '@/utils/errorHandler.ts';
import { isLocalEnv } from '@/utils';
import dayjs from 'dayjs';
import duration from "dayjs/plugin/duration";

const app = createApp(App);

dayjs.extend(duration);
dayjs.locale("zh-cn");

if (!isLocalEnv()) {
  app.config.errorHandler = errorHandler;
}


app.use(Antd);
app.use(pinia);
app.use(router);

app.mount('#app');
