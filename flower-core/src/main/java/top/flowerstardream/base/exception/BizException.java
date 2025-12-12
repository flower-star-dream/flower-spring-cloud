package top.flowerstardream.base.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * 业务异常类
 *
 * @author 花海
 * @date 2025-10-14
 */
@Getter
public class BizException extends RuntimeException {

    private final IExceptionEnum exceptionEnum;

    public BizException(IExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.exceptionEnum = exceptionEnum;
    }
    public BizException(Integer code, String message) {
        super(message);
        this.exceptionEnum = IExceptionEnum.of(code, message);
    }
    public BizException(IExceptionEnum template, Object... params) {
        super(StrUtil.format(template.getMessage(), params));
        this.exceptionEnum = template;
    }

    public BizException(IExceptionEnum template, Throwable cause, Object... params) {
        super(StrUtil.format(template.getMessage(), params), cause);
        this.exceptionEnum = template;
    }

}