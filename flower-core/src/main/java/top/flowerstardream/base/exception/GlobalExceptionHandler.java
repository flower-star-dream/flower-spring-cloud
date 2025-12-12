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
     */
    @ExceptionHandler
    public ResponseEntity<Result<Void>> handleBusinessException(Exception e) {
        BaseExceptionEnum baseExceptionEnum = BaseExceptionEnum.INTERNAL_SERVER_ERROR;
        Result<Void> result;
        if (e instanceof BizException be) {
            result = Result.errorResult(be.getExceptionEnum(), be.getMessage());
            if (BaseExceptionEnum.ERROR.equals(BaseExceptionEnum.valueOf(be.getExceptionEnum().getCode())) ||
                    BaseExceptionEnum.INTERNAL_SERVER_ERROR.equals(BaseExceptionEnum.valueOf(be.getExceptionEnum().getCode()))) {
                log.error("业务异常: {}", be.getMessage(), be);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            log.info("业务异常: {}", be.getMessage(), be);
            HttpStatus httpStatus = exceptionEnum2HttpStatus(be.getExceptionEnum());
            if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
                httpStatus = HttpStatus.OK;
            }
            return ResponseEntity.status(httpStatus).body(result);
        } else if (e instanceof ErrorResponseException errorResponseException){
            log.error("业务异常: {}", errorResponseException.getMessage(), errorResponseException);
            int errorCode = errorResponseException.getStatusCode().value();
            baseExceptionEnum = BaseExceptionEnum.valueOf(errorCode);
            if (baseExceptionEnum == null) {
                baseExceptionEnum = BaseExceptionEnum.INTERNAL_SERVER_ERROR;
            }
            HttpStatus httpStatus = exceptionEnum2HttpStatus(baseExceptionEnum);
            result = Result.errorResult(baseExceptionEnum, baseExceptionEnum.getMessage());
            return ResponseEntity.status(httpStatus).body(result);
        }
        log.error("系统异常: {}", e.getMessage(), e);
        HttpStatus httpStatus = exceptionEnum2HttpStatus(baseExceptionEnum);
        result = Result.errorResult(baseExceptionEnum, baseExceptionEnum.getMessage());
        return ResponseEntity.status(httpStatus).body(result);
    }

    private static HttpStatus exceptionEnum2HttpStatus(IExceptionEnum exceptionEnum) {
        if (exceptionEnum == null) {
            exceptionEnum = BaseExceptionEnum.INTERNAL_SERVER_ERROR;
        }
        HttpStatus httpStatus = null;
        try {
            httpStatus = HttpStatus.valueOf(exceptionEnum.getCode());
        } catch (Exception e) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

}