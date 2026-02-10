package top.flowerstardream.base.context;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 花海
 * @Date: 2025/11/06/22:14
 * @Description: 请求上下文
 */
@Data
public class RequestContext {
    // 链路追踪ID
    private String traceId;
    // 操作者ID
    private Long operatorId;
    // 操作者名称
    private String operatorName;
    // JWT令牌
    private String token;
    // 任意需要存储的数据
    private Map<String, Object> extraData = new HashMap<>();

    // 类型安全地获取扩展数据
    public <T> T getExtra(String key, Class<T> type) {
        Object value = extraData.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public void putExtra(String key, Object value) {
        extraData.put(key, value);
    }
}