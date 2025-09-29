package auto.bangumi.mcs.service;

import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.mcs.model.Request.*;
import auto.bangumi.mcs.model.Response.FileDownloadResponse;
import auto.bangumi.mcs.model.Response.FileManageListResponse;
import auto.bangumi.mcs.model.Response.FileStatusResponse;
import auto.bangumi.mcs.model.Response.FileUploadResponse;

/**
 * 文件管理
 *
 * @author 查查
 * @since 2025/9/23
 */
public interface IMcsFileService {

    /**
     * 获取文件列表
     */
    PageResult<FileManageListResponse.Items> findFilePage(FileManageListRequest request);

    /**
     * 获取文件内容
     */
    String getFileContent(FileUpdateRequest request);

    /**
     * 更新文件内容
     */
    Boolean updateFileContent(FileUpdateRequest request);

    /**
     * 下载文件
     */
    FileDownloadResponse downloadFile(FileDownloadRequest request);

    /**
     * 上传文件
     */
    FileUploadResponse uploadFile(FileUploadRequest request);

    /**
     * 获取文件状态
     *
     * @return
     */
    FileStatusResponse findFileStatus();

    /**
     * 修改文件权限
     *
     * @param request
     */
    Boolean changeFilePermission(FileChangePermissionRequest request);

    /**
     * 复制文件
     *
     * @param request
     */
    Boolean copyFile(FileCopyRequest request);

    /**
     * 移动或重命名
     *
     * @param request
     */
    Boolean moveFile(FileMoveRequest request);

    /**
     * 压缩文件
     *
     * @param request
     */
    Boolean compressFile(FileCompressRequest request);

    /**
     * 解压文件
     *
     * @param request
     */
    Boolean extractFile(FileExtractRequest request);

    /**
     * 删除文件
     *
     * @param request
     */
    Boolean deleteFile(FileDeleteRequest request);

    /**
     * 新建文件
     *
     * @param request
     */
    Boolean touchFile(FileTouchMkdirRequest request);

    /**
     * 新建文件夹
     *
     * @param request
     */
    Boolean touchMkdirFile(FileTouchMkdirRequest request);
}

