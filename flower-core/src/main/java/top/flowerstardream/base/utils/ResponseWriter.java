package top.flowerstardream.base.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @Author: 花海
 * @Date: 2025/11/04/17:21
 * @Description: 网关响应写入工具类
 */
public final class ResponseWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Mono<Void> write(ServerWebExchange exchange, HttpStatus status, Object body) {
        ServerHttpResponse resp = exchange.getResponse();
        resp.setStatusCode(status);
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = MAPPER.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = "{\"msg\":\"json error\"}".getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = resp.bufferFactory().wrap(bytes);
        return resp.writeWith(Mono.just(buffer));
    }
}
