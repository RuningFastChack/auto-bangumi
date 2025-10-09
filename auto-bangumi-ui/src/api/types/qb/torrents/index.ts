export type TorrentsInfoListRequest = {
  /**
   * 过滤状态
   * all, downloading, seeding, completed, paused,
   * active, inactive, resumed, stalled,
   * stalled_uploading, stalled_downloading, errored
   */
  filter?: TorrentsStatusEnum;

  /**
   * 分类 (URL 编码后传输)
   * 空字符串: 无分类
   * null: 任意分类
   */
  category?: string | null;

  /**
   * 标签 (URL 编码后传输)
   * 空字符串: 无标签
   * null: 任意标签
   */
  tag?: string | null;

  /**
   * 排序字段
   * 可用字段：torrent JSON 返回中的任何 key
   */
  sort?: string;

  /**
   * 是否反向排序
   * 默认 false
   */
  reverse?: boolean;

  /**
   * 指定种子 Hash
   * 多个 hash 用 | 分隔
   */
  hashes?: string[];
}

export type TorrentsInfoAddRequest = {
  /**
   * 多行 URL 列表（每行一个 URL），用于通过 URL 下载 .torrent 文件。
   * 对应 API 参数名："urls"
   */
  urls: string[];

  /**
   * 保存路径（下载到本地的目标目录）。
   * 对应 API 参数名："savepath"
   */
  savePath?: string;

  /**
   * 请求下载 .torrent 时使用的 Cookie（如果服务器需要认证）。
   * 对应 API 参数名："cookie"
   */
  cookie?: string;

  /**
   * 种子分类（例如："movies"、"tv" 等）。
   * 对应 API 参数名："category"
   */
  category?: string;

  /**
   * 标签，多个标签以逗号分隔（例如："tag1,tag2"）。
   * 对应 API 参数名："tags"
   */
  tags?: string[];

  /**
   * 是否跳过哈希校验。默认 false。
   * 对应 API 参数名："skip_checking"
   */
  skipChecking?: boolean;

  /**
   * 是否以暂停状态添加。默认 false。
   * 对应 API 参数名："paused"
   */
  paused?: boolean;

  /**
   * 是否创建根文件夹。可选值："true"、"false" 或 "unset"（默认 unset）。
   * 对应 API 参数名："root_folder"
   */
  rootFolder?: 'true' | 'false' | 'unset';

  /**
   * 重命名种子（通常用于上传时修改显示名）。
   * 对应 API 参数名："rename"
   */
  rename?: string;

  /**
   * 上传速度限制（字节/秒）。
   * 对应 API 参数名："upLimit"
   */
  upLimit?: number;

  /**
   * 下载速度限制（字节/秒）。
   * 对应 API 参数名："dlLimit"
   */
  dlLimit?: number;

  /**
   * 设置种子下载时间限制，单位为分钟。
   */
  seedingTimeLimit?: number;

  /**
   * 分享率限制（自 2.8.1 起支持）。
   * 对应 API 参数名："ratioLimit"
   */
  ratioLimit?: number;

  /**
   * 是否应使用自动 Torrent 管理。
   */
  autoTMM?: boolean;

  /**
   * 启用顺序下载。
   */
  sequentialDownload?: boolean;

  /**
   * 优先下载第一个和最后一个片段。
   */
  firstLastPiecePrio?: boolean;
}

export type TorrentsInfoListResponse = {
  /**
   * 种子添加到客户端的时间（Unix 时间戳）
   */
  addedOn: number;

  /**
   * 剩余下载的数据量（字节）
   */
  amountLeft: number;

  /**
   * 是否由自动种子管理管理
   */
  autoTmm: boolean;

  /**
   * 文件片段的可用百分比
   */
  availability: number;

  /**
   * 种子分类
   */
  category: string;

  /**
   * 已完成传输的数据量（字节）
   */
  completed: number;

  /**
   * 种子完成时间（Unix 时间戳）
   */
  completionOn: number;

  /**
   * 种子内容绝对路径（多文件为根路径，单文件为文件路径）
   */
  contentPath: string;

  /**
   * 种子下载速度限制（字节/秒），-1 表示无限制
   */
  dlLimit: number;

  /**
   * 种子当前下载速度（字节/秒）
   */
  dlSpeed: number;

  /**
   * 已下载的数据量
   */
  downloaded: number;

  /**
   * 本次会话已下载的数据量
   */
  downloadedSession: number;

  /**
   * 剩余时间（秒）
   */
  eta: number;

  /**
   * 是否优先下载首尾片段
   */
  firstLastPiecePrio: boolean;

  /**
   * 是否启用强制启动
   */
  forceStart: boolean;

  /**
   * 种子哈希
   */
  hash: string;

  /**
   * 是否来自私有 tracker（5.0.0 新增）
   */
  isPrivate: boolean;

  /**
   * 最后下载/上传块的时间（Unix 时间戳）
   */
  lastActivity: number;

  /**
   * 种子对应的 Magnet URI
   */
  magnetUri: string;

  /**
   * 最大分享率，达到后停止做种/上传
   */
  maxRatio: number;

  /**
   * 最大做种时间（秒），达到后停止做种
   */
  maxSeedingTime: number;

  /**
   * 种子名称
   */
  name: string;

  /**
   * swarm 中的种子数量
   */
  numComplete: number;

  /**
   * swarm 中的下载者数量
   */
  numIncomplete: number;

  /**
   * 当前连接的下载者数量
   */
  numLeechs: number;

  /**
   * 当前连接的种子数量
   */
  numSeeds: number;

  /**
   * 种子优先级，-1 表示排队禁用或种子模式
   */
  priority: number;

  /**
   * 种子进度（百分比/100）
   */
  progress: number;

  /**
   * 种子分享率，最大值 9999
   */
  ratio: number;

  /**
   * TODO（与 maxRatio 的区别）
   */
  ratioLimit: number;

  /**
   * 种子数据存储路径
   */
  savePath: string;

  /**
   * 种子完成后的活跃做种时间（秒）
   */
  seedingTime: number;

  /**
   * TODO（与 maxSeedingTime 的区别，单种子设置）
   */
  seedingTimeLimit: number;

  /**
   * 种子最后一次完成时间（Unix 时间戳）
   */
  seenComplete: number;

  /**
   * 是否启用顺序下载
   */
  sequentialDownload: boolean;

  /**
   * 已选文件总大小（字节）
   */
  size: number;

  /**
   * 种子状态
   */
  state: string;

  /**
   * 是否启用超级做种
   */
  superSeeding: boolean;

  /**
   * 种子标签列表（逗号分隔）
   */
  tags: string;

  /**
   * 种子总活跃时间（秒）
   */
  timeActive: number;

  /**
   * 种子所有文件总大小（包括未选择的）
   */
  totalSize: number;

  /**
   * 第一个工作状态正常的 tracker，若无返回空
   */
  tracker: string;

  /**
   * 种子上传速度限制（字节/秒），-1 表示无限制
   */
  upLimit: number;

  /**
   * 已上传的数据量
   */
  uploaded: number;

  /**
   * 本次会话已上传的数据量
   */
  uploadedSession: number;

  /**
   * 种子当前上传速度（字节/秒）
   */
  upSpeed: number;
}

export type TorrentsInfoDeleteRequest = {
  hashes: string[],
  deleteFiles: boolean
}

export enum TorrentsStatusEnum {
  all = 'all',
  downloading = 'downloading',
  seeding = 'seeding',
  completed = 'completed',
  paused = 'paused',
  active = 'active',
  inactive = 'inactive',
  resumed = 'resumed',
  stalled = 'stalled',
  stalled_uploading = 'stalled_uploading',
  stalled_downloading = 'stalled_downloading',
  errored = 'errored',
}
