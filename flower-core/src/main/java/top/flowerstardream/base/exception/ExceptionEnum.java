package top.flowerstardream.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 花海
 * @Date: 2025/12/16/17:17
 * @Description:
 */
@Getter
@AllArgsConstructor
public enum ExceptionEnum implements IExceptionEnum{
    BASE_EXCEPTION(10000, "基础设施异常"),
    THE_CONTENT_OF_THE_FILE_IS_EMPTY(10001, "文件内容为空"),
    UNKNOWN_STATUS(10002, "未知状态"),
    EVENT_NOT_EXIST(10003, "事件不存在"),
    EVENT_NOT_IMPLEMENTED(10004, "事件未实现"),
    EVENT_ROUTER_ERROR(10005, "事件路由错误"),
    EMPTY_STATE(10006, "空状态"),
    ILLEGAL_STATE_TRANSITION(10007, "不允许的状态转换"),
    EMPTY_PARAMETER(10008, "空参数"),
    MODIFICATION_FAILED(10009, "修改失败");


    private final Integer code;
    private final String message;

    /**
     * 根据业务异常码获取异常枚举
     * @param code 业务异常码
     * @return 异常枚举
     */
    public static ExceptionEnum valueOf(Integer code) {
        for (ExceptionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
