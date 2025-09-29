<script setup lang="ts">
//region type

//endregion

//region props & emit

export interface CardPanelProps {
  fullHeight?: boolean,
  padding?: boolean
}


const props = withDefaults(defineProps<CardPanelProps>(), {
  fullHeight: true,
  padding: true
});

//endregion

//region refs & data

//endregion

//region computed

//endregion

//region watch

//endregion

//region methods

//endregion

//region otherMethods
defineOptions({name: 'CardPanel'});
//endregion

</script>

<template>
  <div
    :class="['card-panel','global-card-container-shadow',fullHeight ? 'h-100' : '',padding ? 'padding' : '']"
  >
    <div v-if="$slots.title" class="card-panel-title">
      <div>
        <a-typography-title :level="5" style="margin-bottom: 0">
          <slot name="title"></slot>
        </a-typography-title>
      </div>
      <div>
        <a-typography-text>
          <slot name="operator"></slot>
        </a-typography-text>
      </div>
    </div>
    <div class="card-panel-content">
      <slot name="body"></slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/assets/global.scss";

.padding {
  padding: 16px;
}

.card-panel {
  border: 1px solid var(--card-border-color);
  background-color: var(--background-color-white);
  display: flex;
  flex-direction: column;
  position: relative;

  .card-panel-title {
    font-weight: 600;
    color: var(--text-color);

    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: -4px; // For Ant-Design Font
    margin-bottom: 8px;
  }

  .card-panel-content {
    flex-grow: 1;
    color: var(--text-color);
    position: relative;
  }
}

.card-panel-content {
  overflow: hidden;
  zoom: 1;
  word-break: break-all;
}
</style>
