package top.flowerstardream.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 花海
 * @Date: 2025/11/11/22:08
 * @Description: 自定义异常接口
 */
public interface IExceptionEnum{
    // 创建一个自定义异常
    @Getter
    @AllArgsConstructor
    class SimpleExceptionEnum implements IExceptionEnum {
        private Integer code;
        private String message;
    }

    /**
     * 创建一个自定义异常
     * @param code
     * @param message
     * @return
     */
    static IExceptionEnum of(Integer code, String message) {
        return new SimpleExceptionEnum(code, message);
    }

    /**
     * 获取异常码
     * @return 异常码
     */
    Integer getCode();

    /**
     * 获取异常信息
     * @return 异常信息
     */
    String getMessage();

    // 调用方法抛异常
    default BizException toException() {
        return new BizException(this);
    }
    default BizException toException(Throwable cause) {
        return new BizException(this, cause);
    }
}
