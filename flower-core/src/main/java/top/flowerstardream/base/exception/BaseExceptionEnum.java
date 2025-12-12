package top.flowerstardream.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 花海
 * @Date: 2025/10/29/00:41
 * @Description: 异常枚举
 */
@Getter
@AllArgsConstructor
public enum BaseExceptionEnum implements IExceptionEnum {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    THE_QUERY_PARAMETER_CANNOT_BE_EMPTY(400, "查询参数不能为空"),
    UNAUTHORIZED(401, "登录状态已失效，请重新登录"),
    THE_JWT_TOKEN_HAS_EXPIRED(401, "JWT令牌过期，请重新登录"),
    FORBIDDEN(403, "当前用户无权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    CONFLICT(409, "资源冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用");


    private final Integer code;
    private final String message;

    /**
     * 根据业务异常码获取异常枚举
     * @param code 业务异常码
     * @return 异常枚举
     */
    public static BaseExceptionEnum valueOf(Integer code) {
        for (BaseExceptionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
