<script setup lang="ts">
//region type
import {useScreen} from '@/hooks/useScreen.ts';
import {useRoute} from 'vue-router';
import LayoutRouter from "@/Layout/LayoutRouter.vue";
//region otherMethods

const { isPhone } = useScreen();

//endregion

//region props & emit

//endregion

//region refs & data
//endregion

//region computed

//endregion

//region watch

//endregion

//region methods

//endregion

const route = useRoute();
defineOptions({ name: 'LayoutMain' });
//endregion

</script>

<template>
  <main class="main-layout-container">
    <div :class="{ 'main-flex-center': !isPhone }"
         :style="!isPhone ? { margin: route?.meta?.viewConfig?.margin } : {}"
    >
      <a-row :gutter="[0, 0]" :align="'center' as any" style="width: 100%">
        <a-col
          :span="24"
          :md="24"
          :lg="route?.meta?.viewConfig?.width * 2||24"
          class="layout-card-col transition-all-6">
          <div
            :style="{minHeight: route?.meta?.viewConfig?.height||'unset'}"
            class="layout-card-container transition-all-6 global-drag-animation">
            <LayoutRouter/>
          </div>
        </a-col>
      </a-row>
    </div>
  </main>
</template>

<style lang="scss">
@use "@/assets/variables.scss";
</style>

<style scoped lang="scss">
@use "@/assets/global.scss";

.layout-card-col {
  position: relative;
}

.main-layout-container {
  position: relative;
  animation: scaleAnimation 0.6s ease-in-out;
  min-height: 100%;
  margin: auto;
  padding-bottom: 20px;
}

.main-flex-center {
  height: 100%;
  width: 100%;
}

.layout-card-container {
  width: 100%;
  height: 100%;
}

@keyframes scaleAnimation {
  0% {
    opacity: 0.02;
    transform: scale(0.98);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
