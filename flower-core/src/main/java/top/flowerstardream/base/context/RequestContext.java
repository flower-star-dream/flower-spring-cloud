package top.flowerstardream.base.context;

import lombok.Data;

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
}