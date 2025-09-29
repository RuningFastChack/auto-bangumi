package auto.bangumi.common.enums;

/**
 * McsResponseEnum
 *
 * @author 查查
 * @since 2025/9/23
 */
public enum McsResponseEnum implements ResponseEnumTemplate<McsResponseEnum> {
    VALID_ERROR(4000, "参数缺失"),

    MCS_REQUEST_ERROR(4001, "请求失败"),

    FILES_FIND_LIST_ERROR(4100, "获取文件列表异常"),

    FILES_FIND_CONTENT_ERROR(4101, "获取文件内容异常"),

    FILES_FIND_STATUS_ERROR(4102, "获取文件状态"),

    FILES_UPDATE_CONTENT_ERROR(4200, "更改文件内容异常"),


    FILES_OPERATE_COPY_ERROR(4301, "复制异常"),

    FILES_OPERATE_MOVE_ERROR(4302, "移动或重命名异常"),

    FILES_OPERATE_COMPRESS_ERROR(4303, "压缩文件异常"),

    FILES_OPERATE_EXTRACT_ERROR(4304, "解压文件异常"),

    FILES_OPERATE_DELETE_ERROR(4305, "删除文件异常"),

    FILES_OPERATE_TOUCH_ERROR(4306, "创建文件异常"),

    FILES_OPERATE_MKDIR_ERROR(4306, "创建文件夹异常"),

    FILES_OPERATE_DOWNLOAD_ERROR(4307, "下载文件异常"),

    FILES_OPERATE_UPLOADED_ERROR(4308, "上传文件异常"),

    FILES_OPERATE_CHANGE_CHMOD_ERROR(4309, "修改文件权限"),

    ;

    /**
     * 返回码
     */
    private final int code;

    /**
     * 返回消息
     */
    private final String message;

    McsResponseEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public ErrorPrefixEnum getCodePrefixEnum() {
        return ErrorPrefixEnum.MCS;
    }
}
