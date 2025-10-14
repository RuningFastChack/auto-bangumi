package auto.bangumi.admin.model;

import auto.bangumi.common.enums.DownUtilEnum;
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

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GeneralSetting {

        private Integer rssTimeOut;

        private String savePathRule;

        private String episodeTitleRule;

        private Boolean enable;

        private Integer sendingTimeLimit;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class McsManageSetting {

        private String url;

        private String mcsManageKey;

        private String daemonId;

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
        private DownUtilEnum utilEnum;

        private String url;

        private String username;

        private String password;

        private String savePath;

        private Boolean ssl;
    }
}
