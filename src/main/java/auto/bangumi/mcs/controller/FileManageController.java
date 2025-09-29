package auto.bangumi.mcs.controller;

import auto.bangumi.common.model.dto.ApiPageResult;
import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.common.valid.Add;
import auto.bangumi.common.valid.Delete;
import auto.bangumi.common.valid.Query;
import auto.bangumi.common.valid.Update;
import auto.bangumi.mcs.model.Request.*;
import auto.bangumi.mcs.model.Response.FileDownloadResponse;
import auto.bangumi.mcs.model.Response.FileManageListResponse;
import auto.bangumi.mcs.model.Response.FileStatusResponse;
import auto.bangumi.mcs.model.Response.FileUploadResponse;
import auto.bangumi.mcs.service.IMcsFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/file/manage")
public class FileManageController {

    @Resource
    private IMcsFileService iMcsFileService;

    /**
     * 获取文件列表
     */
    @GetMapping("page")
    public ApiPageResult<PageResult<FileManageListResponse.Items>> findFilePage(FileManageListRequest request) {
        return ApiPageResult.success(iMcsFileService.findFilePage(request));
    }

    /**
     * 获取文件状态
     */
    @GetMapping("status")
    public ApiResult<FileStatusResponse> findFileStatus() {
        return ApiResult.success(iMcsFileService.findFileStatus());
    }

    /**
     * 下载文件
     */
    @GetMapping("download")
    public ApiResult<FileDownloadResponse> downloadFile(@Validated(Query.class) FileDownloadRequest request) {
        return ApiResult.success(iMcsFileService.downloadFile(request));
    }

    /**
     * 上传文件
     */
    @GetMapping("upload")
    public ApiResult<FileUploadResponse> uploadFile(@Validated(Query.class) FileUploadRequest request) {
        return ApiResult.success(iMcsFileService.uploadFile(request));
    }

    /**
     * 获取文件内容
     */
    @GetMapping("content")
    public ApiResult<String> getFileContent(@Validated(Query.class) FileUpdateRequest request) {
        return ApiResult.success(iMcsFileService.getFileContent(request));
    }

    /**
     * 更新文件内容
     */
    @PutMapping("content")
    public ApiResult<Boolean> updateFileContent(@RequestBody @Validated(Update.class) FileUpdateRequest request) {
        return ApiResult.success(iMcsFileService.updateFileContent(request));
    }

    /**
     * 修改文件权限
     */
    @PutMapping("chmod")
    public ApiResult<Boolean> changeFilePermission(@RequestBody @Validated(Update.class) FileChangePermissionRequest request) {
        return ApiResult.success(iMcsFileService.changeFilePermission(request));
    }

    /**
     * 复制文件
     */
    @PostMapping("copy")
    public ApiResult<Boolean> copyFile(@RequestBody @Validated(Update.class) FileCopyRequest request) {
        return ApiResult.success(iMcsFileService.copyFile(request));
    }

    /**
     * 移动或重命名
     */
    @PutMapping("move")
    public ApiResult<Boolean> moveFile(@RequestBody @Validated(Update.class) FileMoveRequest request) {
        return ApiResult.success(iMcsFileService.moveFile(request));
    }

    /**
     * 压缩文件
     */
    @PostMapping("compress")
    public ApiResult<Boolean> compressFile(@RequestBody @Validated(Add.class) FileCompressRequest request) {
        request.setType("1");
        return ApiResult.success(iMcsFileService.compressFile(request));
    }

    /**
     * 解压文件
     */
    @PutMapping("compress")
    public ApiResult<Boolean> extractFile(@RequestBody @Validated(Update.class) FileExtractRequest request) {
        request.setType("2");
        return ApiResult.success(iMcsFileService.extractFile(request));
    }

    /**
     * 新建文件
     */
    @PostMapping("create")
    public ApiResult<Boolean> touchFile(@RequestBody @Validated(Add.class) FileTouchMkdirRequest request) {
        return ApiResult.success(iMcsFileService.touchFile(request));
    }

    /**
     * 新建文件夹
     */
    @PutMapping("create")
    public ApiResult<Boolean> touchMkdirFile(@RequestBody @Validated(Add.class) FileTouchMkdirRequest request) {
        return ApiResult.success(iMcsFileService.touchMkdirFile(request));
    }

    /**
     * 删除文件
     */
    @DeleteMapping
    public ApiResult<Boolean> deleteFile(@RequestBody @Validated(Delete.class) FileDeleteRequest request) {
        return ApiResult.success(iMcsFileService.deleteFile(request));
    }
}
