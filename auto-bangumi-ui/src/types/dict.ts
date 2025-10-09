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

export const QB_STATUS_MAP: Record<string, DictOptions> = {
  'all': {text: '全部', color: ''},
  'downloading': {text: '下载', color: ''},
  'seeding': {text: '做种', color: ''},
  'completed': {text: '完成', color: ''},
  'paused': {text: '暂停', color: ''},
  'active': {text: '活跃', color: ''},
  'inactive': {text: '空闲', color: ''},
  'resumed': {text: '恢复', color: ''},
  'stalled': {text: '正在检查', color: ''},
  'stalled_uploading': {text: '上传已暂停', color: ''},
  'stalled_downloading': {text: '下载已暂停', color: ''},
  'errored': {text: '错误', color: ''},
}

export const QB_TORRENTS_STATUS_MAP: Record<string, DictOptions> = {
  'error': { text: '错误', color: '' },
  'missingFiles': { text: '文件缺失', color: '' },
  'uploading': { text: '上传中', color: '' },
  'pausedUP': { text: '已完成暂停', color: '' },
  'queuedUP': { text: '排队上传', color: '' },
  'stalledUP': { text: '上传停滞', color: '' },
  'checkingUP': { text: '校验中(已完成)', color: '' },
  'forcedUP': { text: '强制上传', color: '' },
  'allocating': { text: '分配空间', color: '' },
  'downloading': { text: '下载中', color: '' },
  'metaDL': { text: '获取元数据', color: '' },
  'pausedDL': { text: '下载暂停', color: '' },
  'queuedDL': { text: '排队下载', color: '' },
  'stalledDL': { text: '等待', color: '' },
  'checkingDL': { text: '校验中(未完成)', color: '' },
  'forcedDL': { text: '强制下载', color: '' },
  'checkingResumeData': { text: '检查恢复数据', color: '' },
  'moving': { text: '移动中', color: '' },
  'unknown': { text: '未知状态', color: '' }
}
