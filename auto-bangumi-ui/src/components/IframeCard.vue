<script setup lang="ts">
import { ref } from 'vue';
import CardPanel from '@/components/CardPanel.vue';
import { Empty } from 'ant-design-vue';
//region type

//endregion

//region props & emit

export interface FrameCardProps {
  fullCard?: boolean,
  height?: string,
  title?: string,
  path: string
}

withDefaults(defineProps<FrameCardProps>(), {
  fullCard: false,
  height: '',
  title: '',
  path: ''
});

//endregion

//region refs & data

const myIframe = ref<HTMLIFrameElement | null>(null);

const myIframeLoading = ref(false);
//endregion

//region computed

//endregion

//region watch

//endregion

//region methods
//endregion

//region otherMethods
defineOptions({ name: 'IframeCard' });
//endregion

</script>

<template>
  <div style="width: 100%; height: 100%; position: relative">
    <card-panel v-if="path !== ''" style="backdrop-filter: blur()">
      <template #title>
        {{ title }}
      </template>
      <template #body>
        <a-skeleton
          v-show="myIframeLoading"
          active
        />
        <iframe
          v-show="!myIframeLoading"
          ref="myIframe"
          :src="path"
          :style="{
            height: height,
            width: '100%',
            'z-index':-1
          }"
          :class="{ 'full-card-iframe': fullCard }"
          frameborder="0"
          marginwidth="0"
          marginheight="0"
        ></iframe>
      </template>
    </card-panel>
    <card-panel v-else style="height: 100%">
      <template #body>
        <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE">
          <template #description>
            <span>暂无网页</span>
          </template>
        </a-empty>
      </template>
    </card-panel>
  </div>

</template>

<style scoped lang="scss">
.full-card-iframe {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  right: 0;
  border-radius: 6px;
}
</style>
