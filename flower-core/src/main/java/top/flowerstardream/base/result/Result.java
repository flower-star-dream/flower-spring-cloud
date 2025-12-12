package top.flowerstardream.base.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.flowerstardream.base.exception.BaseExceptionEnum;
import top.flowerstardream.base.exception.IExceptionEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 花海
 * @date 2025-10-14
 * @description: 统一响应结果
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "响应码")
    private Integer code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    public Result() {
        this.code = BaseExceptionEnum.SUCCESS.getCode();
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> successResult(Integer code, String message) {
        Result<T> result = new Result<>();
        return result.success(code, message, null);
    }
    public static <T> Result<T> successResult() {
        Result<T> result = new Result<>(BaseExceptionEnum.SUCCESS.getCode(), BaseExceptionEnum.SUCCESS.getMessage());
        return result.success();
    }

    public static <T> Result<T> successResult(T data) {
        Result<T> result = new Result<>(BaseExceptionEnum.SUCCESS.getCode(), BaseExceptionEnum.SUCCESS.getMessage());
        if (data != null) {
            result.setData(data);
        }
        return result;
    }

    public static <T> Result<T> errorResult(IExceptionEnum exceptionEnum) {
        Result<T> result = new Result<>(exceptionEnum.getCode(), exceptionEnum.getMessage());
        return result.error(exceptionEnum.getCode(), exceptionEnum.getMessage());
    }

    public static <T> Result<T> errorResult(IExceptionEnum exceptionEnum, String message) {
        Result<T> result = new Result<>(exceptionEnum.getCode(), message);
        return result.error(exceptionEnum.getCode(), message);
    }

    /**
     * 成功响应
     */
    public Result<T> success() {
        this.code = BaseExceptionEnum.SUCCESS.getCode();
        this.message = BaseExceptionEnum.SUCCESS.getMessage();
        return this;
    }

    public Result<T> success(T data) {
        this.code = BaseExceptionEnum.SUCCESS.getCode();
        this.message = BaseExceptionEnum.SUCCESS.getMessage();
        this.data = data;
        return this;
    }

    public Result<T> success(Integer code, T data) {
        this.code = code;
        this.data = data;
        return this;
    }

    public Result<T> success(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        return this;
    }

    /**
     * 失败响应
     */
    public Result<T> error() {
        this.code = BaseExceptionEnum.ERROR.getCode();
        this.message = BaseExceptionEnum.ERROR.getMessage();
        return this;
    }

    public Result<T> error(String message) {
        this.code = BaseExceptionEnum.ERROR.getCode();
        this.message = message;
        return this;
    }

    public Result<T> error(Integer code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    /**
     * 自定义响应
     */
    public static <T> Result<T> custom(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return BaseExceptionEnum.SUCCESS.getCode().equals(this.code);
    }
}