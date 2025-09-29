package auto.bangumi.mcs.service.imp;

import auto.bangumi.common.enums.McsResponseEnum;
import auto.bangumi.common.exception.common.BusinessException;
import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.common.utils.PageUtils;
import auto.bangumi.mcs.constant.McsManageConstant;
import auto.bangumi.mcs.constant.McsManagePathConstant;
import auto.bangumi.mcs.model.McsManageResponse;
import auto.bangumi.mcs.model.Request.*;
import auto.bangumi.mcs.model.Response.FileDownloadResponse;
import auto.bangumi.mcs.model.Response.FileManageListResponse;
import auto.bangumi.mcs.model.Response.FileStatusResponse;
import auto.bangumi.mcs.model.Response.FileUploadResponse;
import auto.bangumi.mcs.service.IMcsFileService;
import auto.bangumi.mcs.utils.McsHttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理
 *
 * @author 查查
 * @since 2025/9/23
 */
@Slf4j
@Service
public class McsFileServiceImpl implements IMcsFileService {
    /**
     * 获取文件列表
     *
     * @param request
     */
    @Override
    public PageResult<FileManageListResponse.Items> findFilePage(FileManageListRequest request) {

        Map<String, Object> query = request.toMap();

        String result = McsHttpUtil.sendGet(McsManagePathConstant.FILES_LIST, query);

        if (StringUtils.isBlank(result)) {
            return new PageResult<>();
        }

        McsManageResponse<FileManageListResponse> response = JSON.parseObject(result, new TypeReference<>() {
        });

        McsResponseEnum.MCS_REQUEST_ERROR.assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));



        FileManageListResponse fileManageListResponse = response.getData();

        return PageUtils.getPageResult(fileManageListResponse.getPage(),
                fileManageListResponse.getPageSize(),
                fileManageListResponse.getItems(),
                fileManageListResponse.getTotal());
    }

    /**
     * 获取文件内容
     *
     * @param request
     */
    @Override
    public String getFileContent(FileUpdateRequest request) {
        try {
            Map<String, Object> body = request.toMap();
            HttpResponse httpResponse = McsHttpUtil.sendJSONPut(McsManagePathConstant.FILES, new HashMap<>(), body);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.MCS_REQUEST_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return response.getData();
        } catch (Exception e) {
            log.error("获取文件内容异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.MCS_REQUEST_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 更新文件内容
     *
     * @param request
     */
    @Override
    public Boolean updateFileContent(FileUpdateRequest request) {
        try {
            Map<String, Object> body = request.toMap();
            HttpResponse httpResponse = McsHttpUtil.sendJSONPut(McsManagePathConstant.FILES, new HashMap<>(), body);

            McsResponseEnum.FILES_UPDATE_CONTENT_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_UPDATE_CONTENT_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("更新文件内容异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_UPDATE_CONTENT_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param request
     */
    @Override
    public FileDownloadResponse downloadFile(FileDownloadRequest request) {
        try {
            Map<String, Object> query = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendJSONPost(McsManagePathConstant.FILES_DOWNLOAD, query, new HashMap<>());

            McsResponseEnum.FILES_OPERATE_DOWNLOAD_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_DOWNLOAD_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return JSON.parseObject(result, new TypeReference<McsManageResponse<FileDownloadResponse>>() {
            }).getData();
        } catch (Exception e) {
            log.error("获取下载文件配置异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_DOWNLOAD_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 上传文件
     *
     * @param request
     */
    @Override
    public FileUploadResponse uploadFile(FileUploadRequest request) {
        try {
            Map<String, Object> query = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendJSONPost(McsManagePathConstant.FILES_UPLOAD, query, new HashMap<>());

            McsResponseEnum.FILES_OPERATE_UPLOADED_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_UPLOADED_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return JSON.parseObject(result, new TypeReference<McsManageResponse<FileUploadResponse>>() {
            }).getData();
        } catch (Exception e) {
            log.error("获取上传文件配置异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_UPLOADED_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 获取文件状态
     *
     * @return
     */
    @Override
    public FileStatusResponse findFileStatus() {
        String result = McsHttpUtil.sendGet(McsManagePathConstant.FILES_STATUS, new HashMap<>());

        if (StringUtils.isBlank(result)) {
            return new FileStatusResponse();
        }

        McsManageResponse<FileStatusResponse> response = JSON.parseObject(result, new TypeReference<>() {
        });

        McsResponseEnum.MCS_REQUEST_ERROR.assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

        return response.getData();
    }

    /**
     * 修改文件权限
     *
     * @param request
     */
    @Override
    public Boolean changeFilePermission(FileChangePermissionRequest request) {
        try {
            Map<String, Object> body = request.toMap();
            HttpResponse httpResponse = McsHttpUtil.sendJSONPut(McsManagePathConstant.FILES_CHANGE_CHMOD, new HashMap<>(), body);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_CHANGE_CHMOD_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("修改文件权限异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_CHANGE_CHMOD_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 复制文件
     *
     * @param request
     */
    @Override
    public Boolean copyFile(FileCopyRequest request) {
        try {
            Map<String, Object> body = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendJSONPost(McsManagePathConstant.FILES_COPY, new HashMap<>(), body);

            McsResponseEnum.FILES_OPERATE_COPY_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_COPY_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("复制文件异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_COPY_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 移动或重命名
     *
     * @param request
     */
    @Override
    public Boolean moveFile(FileMoveRequest request) {
        try {
            Map<String, Object> body = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendJSONPut(McsManagePathConstant.FILES_MOVE, new HashMap<>(), body);

            McsResponseEnum.FILES_OPERATE_MOVE_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_MOVE_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("移动或重命名异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_MOVE_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 压缩文件
     *
     * @param request
     */
    @Override
    public Boolean compressFile(FileCompressRequest request) {
        try {
            Map<String, Object> body = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendJSONPost(McsManagePathConstant.FILES_COMPRESS, new HashMap<>(), body);

            McsResponseEnum.FILES_OPERATE_COMPRESS_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_COMPRESS_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("压缩文件异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_COMPRESS_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 解压文件
     *
     * @param request
     */
    @Override
    public Boolean extractFile(FileExtractRequest request) {
        try {
            Map<String, Object> body = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendJSONPost(McsManagePathConstant.FILES_COMPRESS, new HashMap<>(), body);

            McsResponseEnum.FILES_OPERATE_EXTRACT_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_EXTRACT_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("解压文件异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_EXTRACT_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param request
     */
    @Override
    public Boolean deleteFile(FileDeleteRequest request) {
        try {
            Map<String, Object> body = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendDelete(McsManagePathConstant.FILES, new HashMap<>(), body);

            McsResponseEnum.FILES_OPERATE_DELETE_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_DELETE_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("删除文件异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_DELETE_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 新建文件
     *
     * @param request
     */
    @Override
    public Boolean touchFile(FileTouchMkdirRequest request) {
        try {
            Map<String, Object> body = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendJSONPost(McsManagePathConstant.FILES_TOUCH, new HashMap<>(), body);

            McsResponseEnum.FILES_OPERATE_TOUCH_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_TOUCH_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("新建文件异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_TOUCH_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 新建文件夹
     *
     * @param request
     */
    @Override
    public Boolean touchMkdirFile(FileTouchMkdirRequest request) {
        try {
            Map<String, Object> body = request.toMap();

            HttpResponse httpResponse = McsHttpUtil.sendJSONPost(McsManagePathConstant.FILES_MKDIR, new HashMap<>(), body);

            McsResponseEnum.FILES_OPERATE_MKDIR_ERROR.assertNull(httpResponse);

            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<String> response = JSON.parseObject(result, new TypeReference<>() {
            });

            McsResponseEnum.FILES_OPERATE_MKDIR_ERROR.message(response.getData()).assertFalse(McsManageConstant.SUCCESS.equals(response.getStatus()));

            return Boolean.valueOf(response.getData());
        } catch (Exception e) {
            log.error("新建文件夹异常：{}", e.getMessage());
            throw new BusinessException(McsResponseEnum.FILES_OPERATE_MKDIR_ERROR, new Object[]{request}, e.getMessage());
        }
    }
}
