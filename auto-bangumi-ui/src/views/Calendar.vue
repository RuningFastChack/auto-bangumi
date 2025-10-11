<script setup lang="ts">
import CardPanel from '@/components/CardPanel.vue';
import { onMounted, ref } from 'vue';
import type { RssManageCalendar } from '@/api/types/rss/rssManage.ts';
import { findRssManageCalendar } from '@/api/modules/rssManage.ts';
import { isEmpty, isLocalEnv } from '@/utils';
import { EditOutlined, OrderedListOutlined } from '@ant-design/icons-vue';
import { sleep } from '@/utils/common.ts';
import BetweenMenus from '@/components/BetweenMenus.vue';
import { useScreen } from '@/hooks/useScreen.ts';
import { useRoute } from 'vue-router';
import { WEEK_MAP } from '../types/dict.ts';
import RssManageForm from '@/views/rss/RssManageForm.vue';
import RssItems from '@/views/rss/RssItems.vue';
import { t } from '@/lang/i18n.ts';
//region type

//endregion

//region props & emit
const route = useRoute();

const rssManageFormRef = ref<InstanceType<typeof RssManageForm>>();

const rssItemRef = ref<InstanceType<typeof RssItems>>();

const { isPhone } = useScreen();

const loading = ref<boolean>(false);

const paramsProps = ref<Record<number, RssManageCalendar[]>>({});
//endregion

//region refs & data

//endregion

//region computed
//endregion

//region watch

//endregion

//region methods

const init = async () => {
  loading.value = true;
  try {
    const { data } = await findRssManageCalendar();
    await sleep(1000);
    paramsProps.value = data;
  } finally {
    loading.value = false;
  }
};

const handlerEdit = (obj: RssManageCalendar) => {
  rssManageFormRef.value?.acceptParams(t('TXT_CODE_09e045a9'), obj.id);
};
const handlerRssItem = (obj: RssManageCalendar) => {
  rssItemRef.value?.acceptParams(obj.id, obj.officialTitle);
};
//endregion

//region otherMethods

defineOptions({ name: 'Calendar' });

onMounted(() => init());
//endregion

</script>

<template>
  <div style="height: 100%" class="container">
    <a-row :gutter="[24, 24]" style="height: 100%">
      <a-col :span="24">
        <between-menus>
          <template v-if="!isPhone" #left>
            <a-typography-title class="mb-0" :level="4">{{ route.meta.mainTitle }}
            </a-typography-title>
          </template>
        </between-menus>
      </a-col>
      <a-col :span="24">
        <a-spin tip="Loading..." :spinning="loading">
          <card-panel class="calendar-container">
            <template #body v-if="isEmpty(paramsProps) && !loading">
              <a-empty :description="t('TXT_CODE_e7d9ffb0')" />
            </template>
            <template #body v-else>
              <div class="week-container"
                   v-for="([key, item], index) in Object.entries(paramsProps)"
                   :key="index">
                <h2 class="week-header" :title="t('TXT_CODE_eefeb8c4')">
                  {{ WEEK_MAP[Number(key)].text }}</h2>
                <a-flex class="week-item-container"
                        :style="isPhone?{width:'100%',overflowX: 'auto',padding:'5px 0'}:{}"
                        :wrap="isPhone?'nowrap':'wrap'"
                        gap="large" justify="flex-start"
                        align="center">
                  <div v-if="!isEmpty(item)" v-for="(_item, _index) in item"
                       :key="_index">
                    <a-tooltip :title="_item.officialTitle">
                      <a-card hoverable :title="_item.officialTitle">
                        <template #cover>
                          <div class="rss-item-container">
                            <div class="rss-item-card">
                              <div class="rss-item-image">
                                <a-image class="rss-item-image"
                                         :width="100"
                                         fallback="data:image/png;base64,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"
                                         :src="`${isLocalEnv()?'':''}${_item.posterLink?.startsWith('/images/') ? _item.posterLink : '/images/' + _item.posterLink}`" />
                              </div>
                              <div class="rss-item-description-container">
                                <div class="rss-item-description">
                                  <div>
                                    <span class="rss-item-label">{{ t('TXT_CODE_b1ffe778') }}：</span>
                                    <span class="rss-item-label-value">{{ _item.sendDate }}</span>
                                  </div>
                                  <div>
                                    <span class="rss-item-label">{{ t('TXT_CODE_70a588d7') }}：</span>
                                    <a-tag color="orange">
                                      {{ t('TXT_CODE_10e36bd4', { num: _item.season }) }}
                                    </a-tag>
                                  </div>
                                  <div>
                                    <span class="rss-item-label">{{ t('TXT_CODE_7df39b03') }}：</span>
                                    <span class="rss-item-label-value">
                                      {{ _item.config.totalEpisode }}
                                    </span>
                                  </div>
                                  <div>
                                    <span class="rss-item-label">{{ t('TXT_CODE_18ad2c30') }}：</span>
                                    <span class="rss-item-label-value">
                                      {{ _item.config.latestEpisode }}
                                    </span>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </template>
                        <template #actions>
                          <edit-outlined @click="handlerEdit(_item)" />
                          <ordered-list-outlined @click="handlerRssItem(_item)" />
                        </template>
                      </a-card>
                    </a-tooltip>
                  </div>
                  <a-empty v-else :description="t('TXT_CODE_e7d9ffb0')"></a-empty>
                </a-flex>
              </div>
            </template>
          </card-panel>
        </a-spin>
      </a-col>
    </a-row>
    <rss-manage-form ref="rssManageFormRef" @success="init()" />
    <rss-items ref="rssItemRef" />
  </div>
</template>

<style scoped lang="scss">
.container {
  :deep(.calendar-container) {
    min-height: 50vh;

    .week-container {
      margin-bottom: 10px;
      padding: 5px;

      .week-item-container {
        min-height: 20vh;

        &::-webkit-scrollbar {
          display: none;
        }
      }

      .week-header {
        font-weight: bold;
        padding: 5px;
        border-left: 2px solid var(--color-blue-6);
      }

      .ant-card {
        width: 300px;

        .rss-item-container {

          .rss-item-card {
            padding: 10px;
            display: flex;
            gap: 5px;

            .rss-item-image {
              border-radius: 5px;
            }

            .rss-item-description-container {
              flex: 1;

              .rss-item-description {
                display: flex;
                flex-direction: column;
                gap: 17px;

                .rss-item-label {
                  font-weight: bold;
                  width: 50px;
                }

                .rss-item-label-value {
                  font-weight: normal;
                  color: var(--color-gray-8)
                }
              }
            }
          }
        }
      }


    }
  }
}
</style>
