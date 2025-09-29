package auto.bangumi.mcs.service;

import auto.bangumi.mcs.model.Request.ProtectedInstanceRequest;
import auto.bangumi.mcs.model.Response.StreamChannelResponse;

/**
 * 实例管理
 *
 * @author 查查
 * @since 2025/9/21
 */
public interface IProtectedInstanceService {

    /**
     * 启动实例
     */
    void open();

    /**
     * 停止实例
     */
    void stop();

    /**
     * 重启实例
     */
    void restart();

    /**
     * 强制结束实例进程
     */
    void kill();

    /**
     * 获取输出
     *
     * @return log
     */
    String outputLog(ProtectedInstanceRequest request);

    /**
     * 设置终端流通道
     */
    StreamChannelResponse streamChannel();
}
