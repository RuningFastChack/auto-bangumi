package auto.bangumi.mcs.model.Response;

import lombok.*;

import java.util.List;

/**
 * 获取文库列表
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileManageListResponse {

    private Integer page;

    private Integer pageSize;

    private Integer total;

    private List<Items> items;


    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Items {

        /**
         * 绝对路径
         */
        private String absolutePath;

        private String name;

        /**
         * byte
         */
        private Integer size;

        private String time;

        /**
         * Linux file permission
         */
        private Integer mode;

        /**
         * 0 = Folder, 1 = File
         */
        private String type;
    }
}
