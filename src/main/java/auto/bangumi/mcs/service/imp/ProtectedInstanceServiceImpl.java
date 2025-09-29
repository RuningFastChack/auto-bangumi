package auto.bangumi.mcs.service.imp;

import auto.bangumi.mcs.constant.McsManagePathConstant;
import auto.bangumi.mcs.model.McsManageResponse;
import auto.bangumi.mcs.model.Request.ProtectedInstanceRequest;
import auto.bangumi.mcs.model.Response.StreamChannelResponse;
import auto.bangumi.mcs.service.IProtectedInstanceService;
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
import java.util.Objects;

/**
 * 实例管理
 *
 * @author 查查
 * @since 2025/9/21
 */
@Slf4j
@Service
public class ProtectedInstanceServiceImpl implements IProtectedInstanceService {

    /**
     * 启动实例
     */
    @Override
    public void open() {
        McsHttpUtil.sendGet(McsManagePathConstant.PROTECTED_INSTANCE_OPEN, new HashMap<>());
    }

    /**
     * 停止实例
     */
    @Override
    public void stop() {
        McsHttpUtil.sendGet(McsManagePathConstant.PROTECTED_INSTANCE_STOP, new HashMap<>());
    }

    /**
     * 重启实例
     */
    @Override
    public void restart() {
        McsHttpUtil.sendGet(McsManagePathConstant.PROTECTED_INSTANCE_RESTART, new HashMap<>());
    }

    /**
     * 强制结束实例进程
     */
    @Override
    public void kill() {
        McsHttpUtil.sendGet(McsManagePathConstant.PROTECTED_INSTANCE_KILL, new HashMap<>());
    }

    /**
     * 获取输出
     *
     * @return log
     */
    @Override
    public String outputLog(ProtectedInstanceRequest request) {

        Map<String, Object> query = request.toMap();

        String responseJSON = McsHttpUtil.sendGet(McsManagePathConstant.PROTECTED_INSTANCE_OUTPUT_LOG, query);
        if (StringUtils.isBlank(responseJSON)) {
            return "";
        }
        McsManageResponse<String> response = JSON.parseObject(responseJSON, new TypeReference<>() {
        });
        return response.getData();
    }

    /**
     * 设置终端流通道
     */
    @Override
    public StreamChannelResponse streamChannel() {
        HttpResponse httpResponse = McsHttpUtil.sendJSONPost(McsManagePathConstant.PROTECTED_INSTANCE_STREAM_CHANNEL, new HashMap<>(), new HashMap<>());
        if (Objects.isNull(httpResponse)) {
            return new StreamChannelResponse();
        }
        try {
            String result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            McsManageResponse<StreamChannelResponse> response = JSON.parseObject(result, new TypeReference<>() {
            });
            return response.getData();
        } catch (Exception e) {
            log.error("设置终端流通道异常：{}", e.getMessage());
        }
        return StreamChannelResponse.builder().build();
    }
}
