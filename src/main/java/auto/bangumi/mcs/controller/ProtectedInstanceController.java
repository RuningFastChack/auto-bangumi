package auto.bangumi.mcs.controller;

import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.mcs.model.Request.ProtectedInstanceRequest;
import auto.bangumi.mcs.model.Response.StreamChannelResponse;
import auto.bangumi.mcs.service.IProtectedInstanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/protected/instance")
public class ProtectedInstanceController {
    @Resource
    private IProtectedInstanceService iProtectedInstanceService;

    /**
     * 启动实例
     */
    @GetMapping("open")
    public ApiResult<Void> open() {
        iProtectedInstanceService.open();
        return ApiResult.success();
    }

    /**
     * 停止实例
     */
    @GetMapping("stop")
    public ApiResult<Void> stop() {
        iProtectedInstanceService.stop();
        return ApiResult.success();
    }

    /**
     * 重启实例
     */
    @GetMapping("restart")
    public ApiResult<Void> restart() {
        iProtectedInstanceService.restart();
        return ApiResult.success();
    }

    /**
     * 强制结束实例进程
     */
    @GetMapping("kill")
    public ApiResult<Void> kill() {
        iProtectedInstanceService.kill();
        return ApiResult.success();
    }

    /**
     * 获取实例输出日志
     */
    @GetMapping("log")
    public ApiResult<String> outputLog(ProtectedInstanceRequest request) {
        return ApiResult.success(iProtectedInstanceService.outputLog(request));
    }

    /**
     * 获取实例输出日志
     */
    @PostMapping("stream/channel")
    public ApiResult<StreamChannelResponse> streamChannel() {
        return ApiResult.success(iProtectedInstanceService.streamChannel());
    }
}
