<script setup lang="ts">
import { useScroll } from '@vueuse/core';
import { computed, h, ref } from 'vue';
import {
  BgColorsOutlined, FontColorsOutlined,
  FontSizeOutlined,
  LogoutOutlined,
  MenuUnfoldOutlined,
  PlusCircleOutlined
} from '@ant-design/icons-vue';
import { Modal } from 'ant-design-vue';
import CardPanel from '@/components/CardPanel.vue';
import { type SizeType, THEME, useAppStore } from '@/stores/modules/app.ts';
import { useUserStore } from '@/stores/modules/user.ts';
import { useScreen } from '@/hooks/useScreen.ts';
import { useAppRouters } from '@/hooks/useAppRouters.ts';
import { useRoute } from 'vue-router';
import { logoutApi } from '@/api/modules/login.ts';
import { AutoBangumiRouter } from '@/router';
import { HOME_URL } from '@/config';
import RssManageForm from '@/views/rss/RssManageForm.vue';
import { SUPPORTED_LANGS, t } from '@/lang/i18n.ts';
//region type
const { y } = useScroll(document.body);

const { getRouteParamsUrl, toPage } = useAppRouters();

const useApp = useAppStore();

const useUser = useUserStore();

const { isPhone } = useScreen();

const route = useRoute();

//endregion

//region props & emit

//endregion

//region refs & data
const openPhoneMenu = ref<boolean>(false);
const rssManageFormRef = ref<InstanceType<typeof RssManageForm>>();
//endregion

//region computed
const isScroll = computed(() => {
  return y.value > 10;
});

const headerStyle = computed(() => {
  return {
    '--header-height': isScroll.value ? '60px' : '64px'
  };
});

const menus = computed(() => {
  return AutoBangumiRouter
    .filter((v) => {
      if (v.path === '/' || !v.meta.mainTitle) return false;
      return (v.meta.mainMenu === true && useUser.isLogged());
    })
    .map((r) => {
      return {
        name: r.meta.mainTitle,
        path: r.path,
        meta: r.meta
      };
    });
});

const appMenus = computed(() => {
  return [
    {
      title: t('TXT_CODE_2a15d8d5'),
      icon: FontSizeOutlined,
      click: (key: SizeType) => {
        useApp.setSize(key);
      },
      onlyPC: false,
      conditions: true,
      menus: [
        {
          title: t('TXT_CODE_4ffa7c8e'),
          value: 'middle'
        },
        {
          title: t('TXT_CODE_959c41e0'),
          value: 'small'
        },
        {
          title: t('TXT_CODE_788b4745'),
          value: 'large'
        }
      ]
    },
    {
      title: t('TXT_CODE_1a195559'),
      icon: FontColorsOutlined,
      click: (key: string) => {
        console.log(key);
        useApp.setLanguage(key);
        setTimeout(() => window.location.reload(), 600);
      },
      onlyPC: false,
      conditions: true,
      menus: SUPPORTED_LANGS.map(item => ({ title: item.label, value: item.value }))
    },
    {
      title: t('TXT_CODE_a4bf9492'),
      icon: BgColorsOutlined,
      click: (key: THEME) => {
        useApp.setTheme(key);
      },
      onlyPC: false,
      conditions: true,
      menus: [
        {
          title: t('TXT_CODE_17a80597'),
          value: THEME.LIGHT
        },
        {
          title: t('TXT_CODE_8b12366f'),
          value: THEME.DARK
        }
      ]
    },
    {
      title: t('TXT_CODE_6a905beb'),
      icon: PlusCircleOutlined,
      click: async () => {
        rssManageFormRef.value?.acceptParams('新增');
      },
      conditions: useUser.isLogged(),
      onlyPC: false
    },
    {
      title: t('TXT_CODE_45659f49'),
      icon: LogoutOutlined,
      click: async () => {
        Modal.confirm({
          title: t('TXT_CODE_845e9756'),
          async onOk() {
            await logoutApi();
            useUser.clear();
            setTimeout(() => (window.location.href = '/'), 400);
          }
        });
      },
      conditions: useUser.isLogged(),
      onlyPC: false
    }
  ];
});

const breadcrumbs = computed(() => {
  const arr = [
    {
      title: t('TXT_CODE_a9bf94d0'),
      disabled: false,
      href: `.`
    }
  ];

  const queryUrl = getRouteParamsUrl();

  if (route.meta.breadcrumbs instanceof Array) {
    const meta = route.meta as Menu.MetaProps;
    meta.breadcrumbs?.forEach((v) => {
      const params = queryUrl && !v.mainMenu ? `?${queryUrl}` : '';
      arr.push({
        title: v.name,
        disabled: false,
        href: `./#${v.path}${params}`
      });
    });
  }
  arr.push({
    title: String(route.meta.title),
    disabled: true,
    href: `./#${route.fullPath}`
  });
  return arr;
});
//endregion

//region watch
//endregion

//region methods
const handleToPage = (url: string) => {
  openPhoneMenu.value = false;
  toPage({ path: url });
};
//endregion

//region otherMethods
defineOptions({ name: 'AppHeader' });
//endregion

</script>

<template>
  <header class="app-header-wrapper" :style="headerStyle">
    <div v-if="!isPhone" class="app-header-content">
      <nav class="btns">
        <a href="." style="margin-right: 12px">
          <div class="logo">
            <img alt="" src="@/assets/logo.svg" style="height: 18px" />
          </div>
        </a>
        <div
          v-for="item in menus"
          :key="item.path"
          class="nav-button"
          @click="handleToPage(item.path||HOME_URL)"
        >
          <span>{{ item.name }}</span>
        </div>
      </nav>
      <div class="btns">
        <div v-for="(item, index) in appMenus" :key="index">
          <a-dropdown v-if="item.menus && item.conditions" placement="bottom">
            <div class="nav-button right-nav-button flex-center" @click.prevent>
              <component :is="item.icon" v-if="item.icon"></component>
            </div>
            <template #overlay>
              <a-menu @click="(e: any) => item.click(e.key)">
                <a-menu-item v-for="m in item.menus" :key="m.value">
                  {{ m.title }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-tooltip v-else-if="item.conditions" placement="bottom">
            <template #title>
              <span>{{ item.title }}</span>
            </template>
            <div class="nav-button right-nav-button flex-center" type="text"
                 @click="(e: any) => item.click(e.key)">
              <component :is="item.icon" v-if="item.icon"></component>
            </div>
          </a-tooltip>
        </div>
      </div>
    </div>
  </header>
  <div v-if="!isPhone" style="height: 64px"></div>
  <!-- Menus for phone -->
  <header v-if="isPhone" class="app-header-content-for-phone">
    <card-panel class="card-panel">
      <template #body>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div style="width: 100px" class="flex">
            <a-button
              type="text"
              :icon="h(MenuUnfoldOutlined)"
              size="small"
              @click="()=>openPhoneMenu = true"
            />
            <div v-for="(item, index) in appMenus" :key="index">
              <a-dropdown v-if="item.menus && item.conditions" class="phone-nav-button"
                          placement="bottom">
                <a-button type="text" :icon="h(item.icon)" size="small" @click.prevent></a-button>
                <template #overlay>
                  <a-menu @click="(e: any) => item.click(e.key)">
                    <a-menu-item v-for="m in item.menus" :key="m.value">
                      {{ m.title }}
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </div>
          <div>
            <img alt="" src="@/assets/logo.svg" style="height: 18px" />
          </div>
          <div style="width: 100px" class="justify-end">
            <div v-for="(item, index) in appMenus" :key="index">
              <a-button
                v-if="!item.menus && item.conditions && !item.onlyPC"
                class="phone-nav-button"
                type="text"
                :icon="h(item.icon)"
                size="small"
                @click="item.click"
              />
            </div>
          </div>
        </div>
      </template>
    </card-panel>
  </header>
  <a-drawer
    :width="500"
    title="MENU"
    placement="top"
    :open="openPhoneMenu"
    @close="() => (openPhoneMenu = false)"
  >
    <div class="phone-menu">
      <div
        v-for="item in menus"
        :key="item.path"
        class="phone-menu-btn"
        @click="handleToPage(item.path||HOME_URL)"
      >
        {{ item.meta.mainTitle }}
      </div>
    </div>
  </a-drawer>
  <div class="breadcrumbs">
    <a-breadcrumb>
      <a-breadcrumb-item v-for="item in breadcrumbs" :key="item.title">
        <a v-if="!item.disabled" :href="item.href">{{ item.title }}</a>
        <span v-else>{{ item.title }}</span>
      </a-breadcrumb-item>
    </a-breadcrumb>
  </div>
  <rss-manage-form ref="rssManageFormRef" />
</template>

<style lang="scss" scoped>
@use "@/assets/global.scss";

.nav-button-warning:hover {
  background-color: rgba(255, 193, 7, 0.34) !important;
}

.nav-button-success:hover {
  background-color: rgba(64, 156, 216, 0.12) !important;
}

.nav-button-danger:hover {
  background-color: #ff19116f !important;
}

.nav-button-primary:hover {
  background-color: rgba(255, 255, 255, 0.25) !important;
}

.nav-button-success:hover {
  background-color: #48e6635a !important;
}

.phone-menu {
  .phone-menu-btn {
    padding: 16px 8px;
    border-bottom: 1px solid var(--color-gray-4);
    color: var(--color-gray-12);
  }
}

.app-header-content-for-phone {
  height: 60px;
  width: 100%;

  // display: flex;
  // justify-content: space-between;
  // align-items: center;
  // margin: 0;
  .card-panel {
    background-color: var(--app-header-bg);
    margin-top: 8px;

    button {
      color: var(--color-always-white) !important;
    }
  }

  .phone-nav-button {
    margin: 0 8px;
  }

  .phone-nav-button * {
    margin: 0;
  }
}

.breadcrumbs {
  font-size: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.app-header-wrapper {
  box-shadow: 0 2px 4px 0 var(--card-shadow-color);

  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--app-header-bg);
  backdrop-filter: saturate(180%) blur(20px);
  color: var(--app-header-text-color);

  position: fixed;
  top: 0;
  left: 0;
  right: 0;

  z-index: 20;

  // 添加平滑过渡效果
  transition: height 0.4s ease-in-out;

  .app-header-content {
    @extend .global-app-container;

    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    height: var(--header-height);

    // 添加平滑过渡效果
    transition: height 0.4s ease-in-out;

    .btns {
      display: flex;
      align-items: center;
    }
  }

  .nav-button {
    margin: 0 4px;
    font-size: 14px;
    transition: all 0.4s;
    color: var(--app-header-text-color) !important;
    text-align: center;
    padding: 8px 12px;
    min-width: 40px;
    cursor: pointer;
    border-radius: 6px;
    user-select: none;
  }

  .right-nav-button {
    margin: 0 2px;
    font-size: 14px;
    padding: 8px 8px;
  }

  .icon-button {
    font-size: 16px !important;
  }

  .nav-button:hover {
    background-color: rgba(215, 215, 215, 0.261);
  }

  .logo {
    cursor: pointer;
  }

  .pro-mode-order-container {
    @extend .nav-button;
    @extend .nav-button-success;
  }

  // Sync margin
  @media (max-width: 1470px) {
    .app-header-content,
    .app-header-content-for-phone {
      margin: 0 25px;
    }
  }

  @media (max-width: 992px) {
    .app-header-content {
      margin: 0 8px;
    }
  }
}
</style>
