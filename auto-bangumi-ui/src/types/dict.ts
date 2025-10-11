import { t } from '@/config/lang/i18n.ts';

export type DictOptions = {
  text: string,
  color: string,
  icon?: string
}

export const YES_NO_MAP: Record<string, DictOptions> = {
  '1': { text: t('TXT_CODE_DICT_YES'), color: 'green', icon: 'CheckCircleOutlined' },
  '0': { text: t('TXT_CODE_DICT_NO'), color: 'red', icon: 'CloseCircleOutlined' }
};
export const WEEK_MAP: Record<number, DictOptions> = {
  1: { text: t('TXT_CODE_DICT_MONDAY'), color: 'red' },
  2: { text: t('TXT_CODE_DICT_TUESDAY'), color: 'pink' },
  3: { text: t('TXT_CODE_DICT_WEDNESDAY'), color: 'orange' },
  4: { text: t('TXT_CODE_DICT_THURSDAY'), color: 'green' },
  5: { text: t('TXT_CODE_DICT_FRIDAY'), color: 'cyan' },
  6: { text: t('TXT_CODE_DICT_SATURDAY'), color: 'blue' },
  7: { text: t('TXT_CODE_DICT_SUNDAY'), color: 'purple' }
};

export const RSS_TYPE_MAP: Record<string, DictOptions> = {
  'Mikan': { text: 'Mikan', color: 'blue' }
};

export const DOWN_UTIL_MAP: Record<string, DictOptions> = {
  'QB': { text: 'QB', color: 'blue' }
};

export const QB_STATUS_MAP: Record<string, DictOptions> = {
  'all': { text: t('TXT_CODE_DICT_QB_ALL'), color: '' },
  'downloading': { text: t('TXT_CODE_DICT_QB_DOWNLOADING'), color: '' },
  'seeding': { text: t('TXT_CODE_DICT_QB_SEEDING'), color: '' },
  'completed': { text: t('TXT_CODE_DICT_QB_COMPLETED'), color: '' },
  'paused': { text: t('TXT_CODE_DICT_QB_PAUSED'), color: '' },
  'active': { text: t('TXT_CODE_DICT_QB_ACTIVE'), color: '' },
  'inactive': { text: t('TXT_CODE_DICT_QB_INACTIVE'), color: '' },
  'resumed': { text: t('TXT_CODE_DICT_QB_RESUMED'), color: '' },
  'stalled': { text: t('TXT_CODE_DICT_QB_STALLED'), color: '' },
  'stalled_uploading': { text: t('TXT_CODE_DICT_QB_STALLED_UPLOADING'), color: '' },
  'stalled_downloading': { text: t('TXT_CODE_DICT_QB_STALLED_DOWNLOADING'), color: '' },
  'errored': { text: t('TXT_CODE_DICT_QB_ERRORED'), color: '' }
};

export const QB_TORRENTS_STATUS_MAP: Record<string, DictOptions> = {
  'error': { text: t('TXT_CODE_DICT_QB_ERROR'), color: '' },
  'missingFiles': { text: t('TXT_CODE_DICT_QB_MISSING_FILES'), color: '' },
  'uploading': { text: t('TXT_CODE_DICT_QB_UPLOADING'), color: '' },
  'pausedUP': { text: t('TXT_CODE_DICT_QB_PAUSED_UP'), color: '' },
  'queuedUP': { text: t('TXT_CODE_DICT_QB_QUEUED_UP'), color: '' },
  'stalledUP': { text: t('TXT_CODE_DICT_QB_STALLED_UP'), color: '' },
  'checkingUP': { text: t('TXT_CODE_DICT_QB_CHECKING_UP'), color: '' },
  'forcedUP': { text: t('TXT_CODE_DICT_QB_FORCED_UP'), color: '' },
  'allocating': { text: t('TXT_CODE_DICT_QB_ALLOCATING'), color: '' },
  'downloading': { text: t('TXT_CODE_DICT_QB_DOWNLOADING'), color: '' },
  'metaDL': { text: t('TXT_CODE_DICT_QB_META_DL'), color: '' },
  'pausedDL': { text: t('TXT_CODE_DICT_QB_PAUSED_DL'), color: '' },
  'queuedDL': { text: t('TXT_CODE_DICT_QB_QUEUED_DL'), color: '' },
  'stalledDL': { text: t('TXT_CODE_DICT_QB_STALLED_DL'), color: '' },
  'checkingDL': { text: t('TXT_CODE_DICT_QB_CHECKING_DL'), color: '' },
  'forcedDL': { text: t('TXT_CODE_DICT_QB_FORCED_DL'), color: '' },
  'checkingResumeData': { text: t('TXT_CODE_DICT_QB_CHECKING_RESUME_DATA'), color: '' },
  'moving': { text: t('TXT_CODE_DICT_QB_MOVING'), color: '' },
  'unknown': { text: t('TXT_CODE_DICT_QB_UNKNOWN'), color: '' }
};
