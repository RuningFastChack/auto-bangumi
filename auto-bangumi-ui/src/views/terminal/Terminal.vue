<script setup lang="ts">
import BetweenMenus from '@/components/BetweenMenus.vue';
import {
  CloseOutlined,
  CodeOutlined,
  DownOutlined,
  PauseCircleOutlined,
  RedoOutlined
} from '@ant-design/icons-vue';
import {useScreen} from '@/hooks/useScreen.ts';
import {useRoute} from 'vue-router';
import {computed} from 'vue';
import {
  killInstance,
  restartInstance,
  stopInstance
} from '@/api/modules/mcs/protected/instance/index.ts';
import TerminalCore from '@/components/TerminalCore.vue';
import {message} from 'ant-design-vue';
import {useTerminal, type UseTerminalHook} from '@/hooks/useTerminal.ts';
import {t} from '@/lang/i18n.ts';
//region type
const { isPhone } = useScreen();
const terminalHook: UseTerminalHook = useTerminal();

interface OperationsType {
  title?: string;
  type?: string;
  props?: Record<string, any>;
  click?: () => void | Promise<void>;
  icon?: any;
  noConfirm?: boolean;
  class?: string;
}

//endregion

//region props & emit
const route = useRoute();

//endregion

//region refs & data
const operations = computed<OperationsType[]>(() => [
  {
    title: t('TXT_CODE_BUTTON_DESC_CLOSE'),
    icon: PauseCircleOutlined,
    type: 'default',
    click: async () => {
      await stopInstance();
      message.success(t('TXT_CODE_10cc2794'));
    },
    props: { danger: true }
  },
  {
    title: t('TXT_CODE_BUTTON_DESC_RESTART'),
    icon: RedoOutlined,
    type: 'default',
    noConfirm: false,
    click: async () => {
      await restartInstance();
      message.success(t('TXT_CODE_8ede6095'));
    }
  },
  {
    title: t('TXT_CODE_BUTTON_DESC_TERMINATE'),
    icon: CloseOutlined,
    type: 'danger',
    class: 'color-warning',
    click: async () => {
      await killInstance();
      message.success(t('TXT_CODE_10cc2794'));
    }
  }
]);//endregion

//region computed

//endregion

//region watch

//endregion

//region methods

//endregion

//region otherMethods

defineOptions({ name: 'Terminal' });
//endregion

</script>

<template>
  <div>
    <div class="mb-24">
      <between-menus>
        <template #left>
          <div class="align-center">
            <a-typography-title class="mb-0" :level="4">
              <CodeOutlined />
              {{ route.meta.mainTitle }}
            </a-typography-title>
          </div>
        </template>
        <template #right>
          <div v-if="!isPhone">
            <template v-for="item in operations" :key="item.title">
              <a-button
                v-if="item.noConfirm"
                class="ml-8"
                :class="item.class ? item.class : ''"
                :danger="item.type === 'danger'"
                @click="item.click"
              >
                <component :is="item.icon" />
                {{ item.title }}
              </a-button>
              <a-popconfirm v-else :key="item.title"
                            :title="t('TXT_CODE_1d745be0')"
                            @confirm="item.click"
              >
                <a-button
                  class="ml-8"
                  :danger="item.type === 'danger'"
                  :class="item.class ? item.class : ''"
                >
                  <component :is="item.icon" />
                  {{ item.title }}
                </a-button>
              </a-popconfirm>
            </template>
          </div>

          <a-dropdown v-else>
            <template #overlay>
              <a-menu>
                <a-menu-item
                  v-for="item in operations"
                  :key="item.title"
                  @click="item.click"
                >
                  <component :is="item.icon" />
                  {{ item.title }}
                </a-menu-item>
              </a-menu>
            </template>
            <a-button type="primary">
              {{ t('TXT_CODE_608994aa') }}
              <DownOutlined />
            </a-button>
          </a-dropdown>
        </template>
      </between-menus>
    </div>
    <terminal-core
      :use-terminal-hook="terminalHook"
      :height="route?.meta?.viewConfig?.height||'unset'" />
  </div>
</template>

<style scoped lang="scss">
.console-wrapper {
  position: relative;

  .terminal-loading {
    z-index: 12;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  .terminal-button-group {
    z-index: 11;
    margin-right: 20px;
    padding-bottom: 50px;
    padding-left: 50px;
    border-radius: 6px;
    color: #fff;

    &:hover {
      ul {
        transition: all 1s;
        opacity: 0.8;
      }
    }

    ul {
      display: flex;
      opacity: 0;

      li {
        cursor: pointer;
        list-style: none;
        padding: 5px;
        margin-left: 5px;
        border-radius: 6px;
        font-size: 20px;

        &:hover {
          background-color: #3e3e3e;
        }
      }
    }
  }

  .terminal-wrapper {
    border: 1px solid var(--card-border-color);
    position: relative;
    overflow: hidden;
    height: 100%;
    background-color: #1e1e1e;
    padding: 8px;
    border-radius: 6px;
    display: flex;
    flex-direction: column;

    .terminal-container {
      // min-width: 1200px;
      height: 100%;
    }

    margin-bottom: 12px;
  }

  .command-input {
    position: relative;

    .history {
      display: flex;
      max-width: 100%;
      overflow: scroll;
      z-index: 10;
      position: absolute;
      top: -35px;
      left: 0;

      li {
        list-style: none;

        span {
          padding: 3px 20px;
          max-width: 300px;
          overflow: hidden;
          text-overflow: ellipsis;
          cursor: pointer;
        }
      }

      &::-webkit-scrollbar {
        width: 0 !important;
        height: 0 !important;
      }
    }
  }

  .terminal-design-tip {
    color: rgba(255, 255, 255, 0.584);
  }
}
</style>
