export type User = {
  id?: number;
  username?: string;
  password?: string;
  config?: UserConfig;
};

export type UserConfig = {
  generalSetting: GeneralSetting;
  filterSetting: FilterSetting;
  downLoadSetting: DownLoadSetting;
  mcsManageSetting: McsManageSetting;
};

export type McsManageSetting = {
  url: string;
  mcsManageKey: string;
  daemonId: string;
  instanceId: string;
};

export type GeneralSetting = {
  rssTimeOut: number;
  enable: boolean;
  savePathRule: string;
  episodeTitleRule: string;
};

export type FilterSetting = {
  enable: boolean;
  filterReg: string[];
};

export type DownLoadSetting = {
  utilEnum: 'QB';
  url: string;
  username: string;
  password: string;
  savePath: string;
  ssl: boolean;
};
