package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: 花海
 * @Date: 2026/02/08/16:42
 * @Description: OAuth2 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class OAuth2Properties {
    
    private Map<String, ClientConfig> clients = new HashMap<>();
    private TokenConfig token = new TokenConfig();
    
    @Data
    public static class ClientConfig {
        // 客户端ID
        private String clientId;
        // 客户端密钥
        private String clientSecret;
        // 重定向地址
        private String redirectUri;
        // 授权范围
        private String scopes;
        public List<String> getScopesList() {
            return Arrays.asList(scopes.split(","));
        }
    }
    
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