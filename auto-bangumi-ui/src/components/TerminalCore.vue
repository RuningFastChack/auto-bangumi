<script setup lang="ts">
import { useCommandHistory } from '@/hooks/useCommandHistory.ts';
import { encodeConsoleColor, type UseTerminalHook } from '@/hooks/useTerminal.ts';
import { getRandomId } from '@/utils/randId.ts';
import { onMounted, ref } from 'vue';
import { Terminal } from '@xterm/xterm';
import { message } from 'ant-design-vue';
import { getInstanceOutputLog } from '@/api/modules/mcs/protected/instance';
import { useUserStore } from '@/stores/modules/user.ts';
import { CodeOutlined, DeleteOutlined, LoadingOutlined } from '@ant-design/icons-vue';
//region type
const { userInfo } = useUserStore();
//endregion

//region props & emit

const props = defineProps<{
  height: string;
  useTerminalHook: UseTerminalHook;
}>();

const {
  focusHistoryList,
  selectLocation,
  history,
  commandInputValue,
  handleHistorySelect,
  clickHistoryItem
} = useCommandHistory();

const {
  state,
  events,
  isConnect,
  execute: setUpTerminal,
  initTerminalWindow,
  sendCommand,
  clearTerminal
} = props.useTerminalHook;

const terminalDomId = `terminal-window-${getRandomId()}`;

const socketError = ref<Error>();

let term: Terminal | undefined;

let inputRef = ref<HTMLElement | null>(null);


//endregion

//region refs & data

//endregion

//region computed

//endregion

//region watch

//endregion

//region methods

const handleSendCommand = () => {
  if (focusHistoryList.value) return;
  sendCommand(commandInputValue.value || '');
  commandInputValue.value = '';
};

const handleClickHistoryItem = (item: string) => {
  clickHistoryItem(item);
  inputRef.value?.focus();
};

const initTerminal = async () => {
  const dom = document.getElementById(terminalDomId);
  if (dom) {
    return initTerminalWindow(dom);
  }
  throw new Error('终端初始化失败，请刷新网页重试！');
};

events.on('opened', () => {
  message.success('实例已运行');
});

events.on('stopped', () => {
  message.success('实例已停止运行');
});

events.on('error', (error: Error) => {
  socketError.value = error;
});

events.once('detail', async () => {
  try {
    const { data } = await getInstanceOutputLog();

    if (data) {
      if (state.value?.config?.terminalOption?.haveColor) {
        term?.write(encodeConsoleColor(data));
      } else {
        term?.write(data);
      }
    }
  } catch (error: any) {
  }
});

const refreshPage = () => {
  window.location.reload();
};
//endregion

//region otherMethods
onMounted(async () => {
  try {
    if (userInfo.config?.mcsManageSetting.instanceId && userInfo.config?.mcsManageSetting.daemonId) {
      await setUpTerminal();
    }
    term = await initTerminal();
  } catch (error: any) {
    console.error(error);
    throw error;
  }
});
defineOptions({ name: 'TerminalCore' });
//endregion

</script>

<template>
  <div class="console-wrapper">
    <div v-if="!isConnect" class="terminal-loading">
      <LoadingOutlined style="font-size: 72px; color: white" />
    </div>
    <div class="terminal-button-group position-absolute-right position-absolute-top">
      <ul>
        <li @click="clearTerminal()">
          <a-tooltip placement="top">
            <template #title>
              <span>清空终端输出内容</span>
            </template>
            <delete-outlined />
          </a-tooltip>
        </li>
      </ul>
    </div>
    <div class="terminal-wrapper global-card-container-shadow position-relative">
      <div class="terminal-container">
        <div
          :id="terminalDomId"
          :style="{ height: props.height }"
        ></div>
      </div>
    </div>
    <div class="command-input">
      <div v-show="focusHistoryList" class="history">
        <li v-for="(item, key) in history" :key="item">
          <a-tag
            :color="key !== selectLocation ? 'blue' : '#108ee9'"
            @click="handleClickHistoryItem(item)"
          >
            {{ item.length > 14 ? item.slice(0, 14) + '...' : item }}
          </a-tag>
        </li>
      </div>
      <a-input
        ref="inputRef"
        v-model:value="commandInputValue"
        placeholder="输入命令按回车发送，使用上下键选择历史命令"
        autofocus
        :disabled="!isConnect"
        @press-enter="handleSendCommand"
        @keydown="handleHistorySelect"
      >
        <template #prefix>
          <CodeOutlined style="font-size: 18px" />
        </template>
      </a-input>
    </div>
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
