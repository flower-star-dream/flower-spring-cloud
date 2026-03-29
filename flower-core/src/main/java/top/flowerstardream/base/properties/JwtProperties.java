package top.flowerstardream.base.properties;

import jakarta.websocket.EndpointConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.annotation.AutoConfigProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 花海
 * @Date: 2026/02/08/16:42
 * @Description: Jwt 配置
 */
@Data
@AutoConfigProperties(prefix = "jwt")
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private Map<String, TokenConfig> tokens = new HashMap<>();

    private Map<String, EndpointConfig> endpoints = new HashMap<>();
    
    @Data
    public static class TokenConfig {
        // 密钥
        private String secretKey;
        // 过期时间
        private long accessTokenTtl;
        private long refreshTokenTtl;
        // 令牌名称
        private String tokenName = "Authorization";
        // 刷新时间
        private long refreshTime;
    }

    @Data
    public static class EndpointConfig {
        private String defaultTokenType;  // 该端默认使用的token配置名
        private String[] permitPaths;     // 该端白名单路径
    }
}