export interface InstanceDetail {
  instanceUuid: string;
  started: number;
  status: INSTANCE_STATUS_CODE;
  info: InstanceRuntimeInfo;
  config: IGlobalInstanceConfig;
  watcher?: number;
}

export interface InstanceRuntimeInfo {
  mcPingOnline: boolean;
  currentPlayers: number;
  maxPlayers: number;
  version: string;
  fileLock: number;
  playersChart: { value: string }[];
  openFrpStatus: boolean;
  latency: number;
  cpuUsage?: number;
  memoryUsagePercent?: number;
  rxBytes?: number;
  txBytes?: number;
  readBytes?: number;
  writeBytes?: number;
  memoryUsage?: number;
  memoryLimit?: number;
  allocatedPorts?: {
    host: number;
    container: number;
    protocol: string;
  }[];
}

interface IGlobalInstanceConfig {
  nickname: string;
  startCommand: string;
  stopCommand: string;
  cwd: string;
  ie: string;
  oe: string;
  createDatetime: number;
  lastDatetime: number;
  type: string;
  tag: string[];
  endTime: number;
  fileCode: string;
  processType: string;
  updateCommand: string;
  runAs: string;
  actionCommandList: any[];
  crlf: number;
  category: number;

  // Steam RCON
  enableRcon?: boolean;
  rconPassword?: string;
  rconPort?: number;
  rconIp?: string;

  // Old fields
  terminalOption: {
    haveColor: boolean;
    pty: boolean;
  };
  eventTask: {
    autoStart: boolean;
    autoRestart: boolean;
    ignore: boolean;
  };
  docker: IGlobalInstanceDockerConfig;
  pingConfig: {
    ip?: string;
    port?: number;
    type?: number;
  };
  extraServiceConfig: {
    openFrpTunnelId?: string;
    openFrpToken?: string;
  };
}

interface IGlobalInstanceDockerConfig {
  containerName?: string;
  image?: string;
  memory?: number;
  ports?: string[];
  extraVolumes?: string[];
  maxSpace?: number;
  network?: number;
  io?: number;
  networkMode?: string;
  networkAliases?: string[];
  cpusetCpus?: string;
  cpuUsage?: number;
  workingDir?: string;
  env?: string[];
  changeWorkdir?: boolean;
  memorySwap?: number;
  memorySwappiness?: number;
}

export enum INSTANCE_STATUS_CODE {
  BUSY = -1,
  STOPPED = 0,
  STOPPING = 1,
  STARTING = 2,
  RUNNING = 3
}

export const GLOBAL_INSTANCE_NAME = "__MCSM_GLOBAL_INSTANCE__";
