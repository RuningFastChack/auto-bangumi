import type { RouteRecordRaw } from 'vue-router';
import { createRouter, createWebHashHistory } from 'vue-router';
import { useUserStore } from '@/stores/modules/user';
import { HOME_URL, IS_PREVIEW, LOGIN_URL, ROUTER_ERROR_LIST, ROUTER_WHITE_LIST } from '@/config';


export const AutoBangumiRouter: Menu.MenuOptions[] = [
  {
    path: '/',
    redirect: HOME_URL,
    meta: {
      viewConfig: {
        height: 'unset',
        width: 12,
        margin: 'auto'
      }
    }
  },
  {
    name: 'Login',
    path: '/login',
    component: () => import('@/views/LoginCard.vue'),
    meta: {
      title: '登录页',
      viewConfig: {
        height: 'unset',
        width: 4,
        margin: '10vh 0 0 0'
      }
    }
  },
  {
    name: 'Calendar',
    path: '/calendar',
    component: () => import('@/views/Calendar.vue'),
    meta: {
      title: '日历',
      mainTitle: '日历',
      mainMenu: true,
      viewConfig: {
        height: 'unset',
        width: 12,
        margin: 'auto'
      }
    }
  },
  {
    path: '/rss/manage',
    name: 'RssManage',
    component: () => import('@/views/rss/RssManage.vue'),
    meta: {
      title: 'RSS订阅',
      mainTitle: 'RSS订阅',
      mainMenu: true,
      viewConfig: {
        height: 'unset',
        width: 12,
        margin: 'auto'
      }
    }
  },
  {
    name: 'Terminal',
    path: '/terminal',
    component: () => import('@/views/terminal/Terminal.vue'),
    meta: {
      title: '终端',
      mainTitle: '终端',
      mainMenu: !IS_PREVIEW,
      viewConfig: {
        height: '600px',
        width: 12,
        margin: 'auto'
      }
    }
  },
  {
    name: 'FileManage',
    path: '/fileManage',
    component: () => import('@/views/files/FileManage.vue'),
    meta: {
      title: '文件管理',
      mainTitle: '文件管理',
      mainMenu: true,
      viewConfig: {
        height: 'unset',
        width: 12,
        margin: 'auto'
      }
    }
  },
  {
    name: 'Settings',
    path: '/setting',
    component: () => import('@/views/Settings.vue'),
    meta: {
      title: '系统设置',
      mainTitle: '设置',
      mainMenu: true,
      viewConfig: {
        height: '800px',
        width: 8,
        margin: 'auto'
      }
    }
  },
  {
    name: '403',
    path: '/403',
    component: () => import('@/components/error/403.vue'),
    meta: {
      title: '系统错误',
      mainTitle: '错误',
      viewConfig: {
        height: '100px',
        width: 6,
        margin: 'auto'
      }
    }
  }
];

function routersConfigOptimize(
  config: Menu.MenuOptions[],
  list: Array<{ name: string; path: string }> = []
) {
  for (const r of config) {
    r.meta.breadcrumbs = list;
    if (r.children) {
      const newList = JSON.parse(JSON.stringify(list));
      newList.push({
        name: r.meta.title,
        path: r.path,
        mainMenu: r.meta.mainMenu
      });
      routersConfigOptimize(r.children, newList);
    }
  }
  return config;
}

const router = createRouter({
  history: createWebHashHistory(),
  routes: routersConfigOptimize(AutoBangumiRouter) as RouteRecordRaw[]
});


/**
 * @description 路由拦截 beforeEach
 * */
router.beforeEach(async (to, from, next) => {
  const { isLogged } = useUserStore();

  const title = import.meta.env.VITE_APP_TITLE;

  document.title = to.meta.title ? `${to.meta.title} - ${title}` : title;

  const toRoutePath = to.path.trim();

  if (toRoutePath === LOGIN_URL) {
    if (isLogged()) {
      if (ROUTER_ERROR_LIST.includes(from.fullPath)) {
        return next(HOME_URL);
      }
      return next(from.fullPath);
    }
    return next();
  }

  if ([...ROUTER_WHITE_LIST, ...ROUTER_ERROR_LIST].includes(toRoutePath)) {
    return next();
  }

  if (!isLogged()) {
    return next({ path: LOGIN_URL, replace: true });
  }

  if (!to.name) return next('/403');

  next();
});

/**
 * @description 路由跳转错误
 * */
router.onError(error => {
  console.warn('路由错误', error.message);
});

export default router;
