package top.flowerstardream.base.autoconfigure;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.flowerstardream.base.properties.JwtProperties;
import top.flowerstardream.base.utils.JwtUtil;

/**
 * @Author: 花海
 * @Date: 2026/03/18/00:31
 * @Description: JWT 配置类
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties({JwtProperties.class})
public class JwtConfig {

    @Resource
    private JwtProperties jwtProperties;

    @Bean
    public CommandLineRunner initJwkKeys() {
        return args -> {
            for (var entry : jwtProperties.getEndpoints().entrySet()) {
                String endpoint = entry.getKey();
                String tokenType = entry.getValue().getDefaultTokenType();
                var config = jwtProperties.getTokens().get(tokenType);

                if (config == null) {
                    continue;
                }

                // 简化：endpoint 作为 keyId
                String keyId = endpoint;
                String secret = config.getSecretKey();

                if (secret.startsWith("RSA:")) {
                    if ("RSA:GENERATE".equals(secret)) {
                        // 生成新 RSA 密钥
                        String publicPem = JwtUtil.generateRsaKey(keyId);
                        log.warn("【重要】生成 RSA 密钥对 [{}]，请保存公钥到配置：\n{}", keyId, publicPem);
                    } else {
                        // 加载已有 RSA:公钥|私钥
                        String[] parts = secret.substring(4).split("\\|");
                        JwtUtil.loadRsaKey(keyId, parts[0], parts[1]);
                    }
                } else {
                    // 对称密钥：兼容原有 secretKey
                    JwtUtil.generalKey(keyId, secret);
                }

                log.info("JWK 初始化: {} -> {}", endpoint, keyId);
            }
        };
    }
}
