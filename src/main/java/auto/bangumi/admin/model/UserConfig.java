package auto.bangumi.admin.model;

import auto.bangumi.common.enums.DownUtilEnum;
import auto.bangumi.common.enums.PlayerEnums;
import lombok.*;

import java.util.List;

/**
 * UserConfig
 *
 * @author sakura
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserConfig {
    private GeneralSetting generalSetting;

    private FilterSetting filterSetting;

    private DownLoadSetting downLoadSetting;

    private McsManageSetting mcsManageSetting;
    
    private PlayerSetting playerSetting;

    private SystemInfo systemInfo;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GeneralSetting {

        /**
         * 轮询间隔时间
         */
        private Integer rssTimeOut;

        /**
         * 保存路径规则
         */
        private String savePathRule;

        /**
         * 剧集标题规则
         */
        private String episodeTitleRule;

        /**
         * 启用
         */
        private Boolean enable;

        /**
         * 做种时间
         */
        private Integer sendingTimeLimit;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class McsManageSetting {

        /**
         * MCS管理地址
         */
        private String url;

        /**
         * MCS管理Key
         */
        private String mcsManageKey;

        /**
         * MCS守护进程ID
         */
        private String daemonId;

        /**
         * MCS实例ID
         */
        private String instanceId;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilterSetting {
        /**
         * 启用
         */
        private Boolean enable;
        /**
         * 排除
         */
        private List<String> filterReg;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DownLoadSetting {
        /**
         * 下载工具枚举
         */
        private DownUtilEnum utilEnum;

        /**
         * 下载地址
         */
        private String url;

        /**
         * 下载用户名
         */
        private String username;

        /**
         * 下载密码
         */
        private String password;

        /**
         * 保存路径
         */
        private String savePath;

        /**
         * 是否启用SSL
         */
        private Boolean ssl;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerSetting {
        /**
         * 播放器枚举
         */
        private PlayerEnums playerEnums;

        /**
         * 用户ID
         */
        private String userId;

        /**
         * 应用Key
         */
        private String appKey;

        /**
         * 基础路径
         */
        private String basePath;

        /**
         * 播放地址
         */
        private String url;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemInfo {
        /**
         * 系统名称
         */
        private String version;
        /**
         * 构建时间
         */
        private String buildTime;
    }
}
