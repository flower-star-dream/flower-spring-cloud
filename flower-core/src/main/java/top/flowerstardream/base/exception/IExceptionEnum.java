package top.flowerstardream.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 花海
 * @Date: 2025/11/11/22:08
 * @Description: 自定义异常接口
 */
public interface IExceptionEnum{
    // 创建一个自定义异常
    @Getter
    @AllArgsConstructor
    class CustomExceptionEnum implements IExceptionEnum {
        private Integer code;
        private String message;
    }

    /**
     * 创建一个自定义异常
     * @param code 错误码
     * @param message 错误信息
     * @return 自定义异常
     */
    static IExceptionEnum of(Integer code, String message) {
        return new CustomExceptionEnum(code, message);
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
    default BizException toException(String replenishmentMsg) {
        String newMessage = this.getMessage() + ":" + replenishmentMsg;
        return new BizException(IExceptionEnum.of(this.getCode(), newMessage));
    }
}
