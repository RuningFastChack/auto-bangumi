<script lang="ts" setup>
import {useRoute} from "vue-router";
//region type

const route = useRoute();

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

//region otherMethods
defineOptions({name: 'LayoutRouter'});
//endregion

</script>

<template>
  <router-view :key="route.fullPath"/>
</template>

<style lang="scss" scoped>

</style>
