import router from '@/router';

import { type RouteLocationPathRaw, useRoute } from 'vue-router';

export function useAppRouters() {
  const route = useRoute();

  const getRouteParamsUrl = () => {
    return route.fullPath.split('?')[1] || '';
  };


  const getUrlParams = () => {
    const params: Record<string, string> = {};
    const queryString = route.fullPath.split('?')[1];
    if (queryString) {
      const searchParams = new URLSearchParams(queryString);
      searchParams.forEach((value, key) => {
        params[key] = value;
      });
    }
    return params;
  };

  const toPage = async (params: RouteLocationPathRaw) => {
    const tmp = {
      ...params
    };
    tmp.query = {
      ...(route.query || {}),
      ...(params.query || {})
    };
    await router.push(tmp);
  };

  return {
    getRouteParamsUrl,
    getUrlParams,
    toPage
  };
}
