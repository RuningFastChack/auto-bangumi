package auto.bangumi.mcs.constant;

/**
 * Mcs 请求路径
 *
 * @author 查查
 * @since 2025/9/27
 */
public interface McsManagePathConstant {

    /**
     * 启动实例
     */
    String PROTECTED_INSTANCE_OPEN = "/api/protected_instance/open";

    /**
     * 停止实例
     */
    String PROTECTED_INSTANCE_STOP = "/api/protected_instance/stop";

    /**
     * 重启实例
     */
    String PROTECTED_INSTANCE_RESTART = "/api/protected_instance/restart";

    /**
     * 终止实例
     */
    String PROTECTED_INSTANCE_KILL = "/api/protected_instance/kill";

    /**
     * 获取输出
     */
    String PROTECTED_INSTANCE_OUTPUT_LOG = "/api/protected_instance/outputlog";

    /**
     * 获取远程节点信息
     */
    String PROTECTED_INSTANCE_STREAM_CHANNEL = "/api/protected_instance/stream_channel";

    /**
     * 编辑文件
     */
    String FILES = "/api/files";

    /**
     * 获取文件列表
     */
    String FILES_LIST = "/api/files/list";

    /**
     * 下载文件（获得下载配置）
     */
    String FILES_DOWNLOAD = "/api/files/download";

    /**
     * 上传文件（获得上传配置）
     */
    String FILES_UPLOAD = "/api/files/upload";

    /**
     * 复制文件
     */
    String FILES_COPY = "/api/files/copy";

    /**
     * 移动或重命名
     */
    String FILES_MOVE = "/api/files/move";

    /**
     * 文件解压缩
     */
    String FILES_COMPRESS = "/api/files/compress";

    /**
     * 新建文件
     */
    String FILES_TOUCH = "/api/files/touch";

    /**
     * 新建文件夹
     */
    String FILES_MKDIR = "/api/files/mkdir";

    /**
     * 获取文件状态
     */
    String FILES_STATUS = "/api/files/status";

    /**
     * 修改文件权限
     */
    String FILES_CHANGE_CHMOD = "/api/files/chmod";
}
