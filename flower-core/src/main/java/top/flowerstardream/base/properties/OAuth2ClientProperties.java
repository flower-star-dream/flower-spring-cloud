package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.annotation.AutoConfigProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OAuth2 客户端配置
 * @Author: 花海
 * @Date: 2026/03/18/01:54
 * @Description: OAuth2 客户端配置
 */
@Data
@AutoConfigProperties(prefix = "oauth2")
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2ClientProperties {
    private List<ClientConfig> clients = new ArrayList<>();
    
    @Data
    public static class ClientConfig {
        private String clientId;
        private String clientSecret;  // 支持 {bcrypt} 前缀或明文
        private String tokenType; // 匹配jwt.tokens.xxx
        private List<String> grantTypes;
        private List<String> scopes;
        private List<String> redirectUris;
        // 可选字段，不填默认使用JWT设置
        private int accessTokenTtl;
        private int refreshTokenTtl;
    }
    
    public Optional<ClientConfig> getClient(String clientId) {
        return clients.stream()
            .filter(c -> c.getClientId().equals(clientId))
            .findFirst();
    }
}