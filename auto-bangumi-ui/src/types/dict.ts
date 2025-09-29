export type DictOptions = {
  text: string,
  color: string,
  icon?: string
}

export const YES_NO_MAP: Record<string, DictOptions> = {
  '1': { text: '是', color: 'green', icon: 'CheckCircleOutlined' },
  '0': { text: '否', color: 'red', icon: 'CloseCircleOutlined' }
};
export const WEEK_MAP: Record<number, DictOptions> = {
  1: { text: '星期一', color: 'red' },
  2: { text: '星期二', color: 'pink' },
  3: { text: '星期三', color: 'orange' },
  4: { text: '星期四', color: 'green' },
  5: { text: '星期五', color: 'cyan' },
  6: { text: '星期六', color: 'blue' },
  7: { text: '星期日', color: 'purple' }
};

export const RSS_TYPE_MAP: Record<string, DictOptions> = {
  'Mikan': { text: 'Mikan', color: 'blue' }
};

export const DOWN_UTIL_MAP: Record<string, DictOptions> = {
  'QB': { text: 'QB', color: 'blue' }
};
