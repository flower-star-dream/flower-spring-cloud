package top.flowerstardream.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.flowerstardream.base.result.Result;

/**
 * 全局异常处理器
 *
 * @author 花海
 * @date 2025-10-14
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理全局异常
     * 根据异常类型返回相应的 HTTP 状态码和错误信息
     *
     * @param e 捕获的异常对象
     * @return 包含错误信息的 ResponseEntity
     */
    @ExceptionHandler
    public ResponseEntity<Result<Void>> handleBusinessException(Exception e) {
        // 默认异常枚举为内部服务器错误
        BaseExceptionEnum baseExceptionEnum = BaseExceptionEnum.INTERNAL_SERVER_ERROR;
        Result<Void> result;
        
        // 处理业务异常 (BizException)
        if (e instanceof BizException be) {
            // 构建错误结果
            result = Result.errorResult(be.getExceptionEnum(), be.getMessage());
            
            // 判断是否为严重错误 (ERROR 或 INTERNAL_SERVER_ERROR)，若是则记录错误日志并返回 500
            if (BaseExceptionEnum.ERROR.equals(BaseExceptionEnum.valueOf(be.getExceptionEnum().getCode())) ||
                    BaseExceptionEnum.INTERNAL_SERVER_ERROR.equals(BaseExceptionEnum.valueOf(be.getExceptionEnum().getCode()))) {
                log.error("业务异常：{}", be.getMessage(), be);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            
            // 记录普通业务异常日志
            log.info("业务异常：{}", be.getMessage(), be);
            
            // 获取对应的 HTTP 状态码
            HttpStatus httpStatus = exceptionEnum2HttpStatus(be.getExceptionEnum());
            // 如果转换后仍为 500，则降级为 200 OK，避免前端误判为系统故障
            if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
                httpStatus = HttpStatus.OK;
            }
            return ResponseEntity.status(httpStatus).body(result);
            
        } 
        // 处理 Spring ErrorResponseException
        else if (e instanceof ErrorResponseException errorResponseException) {
            log.error("业务异常：{}", errorResponseException.getMessage(), errorResponseException);
            
            // 从异常中获取状态码并转换为对应的异常枚举
            int errorCode = errorResponseException.getStatusCode().value();
            baseExceptionEnum = BaseExceptionEnum.valueOf(errorCode);
            
            // 如果未找到对应的枚举，默认为内部服务器错误
            if (baseExceptionEnum == null) {
                baseExceptionEnum = BaseExceptionEnum.INTERNAL_SERVER_ERROR;
            }
            
            // 获取 HTTP 状态码并构建结果
            HttpStatus httpStatus = exceptionEnum2HttpStatus(baseExceptionEnum);
            result = Result.errorResult(baseExceptionEnum, baseExceptionEnum.getMessage());
            return ResponseEntity.status(httpStatus).body(result);
        }
        
        // 处理其他未知系统异常
        log.error("系统异常：{}", e.getMessage(), e);
        HttpStatus httpStatus = exceptionEnum2HttpStatus(baseExceptionEnum);
        result = Result.errorResult(baseExceptionEnum, baseExceptionEnum.getMessage());
        return ResponseEntity.status(httpStatus).body(result);
    }

    /**
     * 将异常枚举转换为 HTTP 状态码
     *
     * @param exceptionEnum 异常枚举对象
     * @return 对应的 HttpStatus，若转换失败则返回 INTERNAL_SERVER_ERROR
     */
    private static HttpStatus exceptionEnum2HttpStatus(IExceptionEnum exceptionEnum) {
        // 如果传入枚举为空，默认使用内部服务器错误
        if (exceptionEnum == null) {
            exceptionEnum = BaseExceptionEnum.INTERNAL_SERVER_ERROR;
        }
        
        HttpStatus httpStatus = null;
        try {
            // 尝试通过枚举中的 code 值构建 HttpStatus
            httpStatus = HttpStatus.valueOf(exceptionEnum.getCode());
        } catch (Exception e) {
            // 转换失败时兜底返回 500
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

}