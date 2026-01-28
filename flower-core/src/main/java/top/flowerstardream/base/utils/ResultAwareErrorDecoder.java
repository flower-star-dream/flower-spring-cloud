package top.flowerstardream.base.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.flowerstardream.base.exception.BizException;
import top.flowerstardream.base.exception.IExceptionEnum;
import top.flowerstardream.base.result.Result;
import top.flowerstardream.base.state.IBaseEvent;

import java.io.IOException;

/**
 * @Author: 花海
 * @Date: 2025/11/13/16:51
 * @Description: OpenFeign错误解码器
 */
@Slf4j
public class ResultAwareErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                // 把响应体读成 Result
                Result<?> result = objectMapper.readValue(
                        response.body().asInputStream(),
                        new TypeReference<>() {
                        }
                );
                if (!result.isSuccess()) {
                    // 预订座位失败，抛出异常
                    log.error("【服务调用】调用服务异常，错误状态码: {}, 异常信息: {}", result.getCode(), result.getMessage());
                    throw IExceptionEnum.of(result.getCode(), result.getMessage()).toException();
                }
                // 不抛异常！返回一个包装了 Result 的“伪异常”
                // 但 Feign 会把它当成异常处理，所以返回一个特殊的 RuntimeException，里面带 Result
                return new ResultWrappedException(result);
            }
        } catch (IOException e) {
            // 解析失败，返回默认异常
        }
        return FeignException.errorStatus(methodKey, response);
    }

    // 自定义异常，专门用来“传值”
    @Getter
    public static class ResultWrappedException extends RuntimeException {
        private final Result<?> result;

        public ResultWrappedException(Result<?> result) {
            this.result = result;
        }

    }
}
