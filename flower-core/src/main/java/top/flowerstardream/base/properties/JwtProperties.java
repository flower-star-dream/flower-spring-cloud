package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 花海
 * @Date: 2026/02/08/16:42
 * @Description: Jwt 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private Map<String, TokenConfig> tokens = new HashMap<>();
    
    @Data
    public static class TokenConfig {
        // 密钥
        private String secretKey;
        // 过期时间
        private long ttl;
        // 令牌名称
        private String tokenName;
        // 刷新时间
        private long refreshTime;
    }
}