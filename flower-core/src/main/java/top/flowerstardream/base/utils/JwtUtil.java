package top.flowerstardream.base.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT与JWK融合工具类
 * @Author: 花海
 * @Date: 2025/11/09/15:26
 * @Description: JWT与JWK融合工具类
 */
@Slf4j
@UtilityClass
public class JwtUtil {

    private final Map<String, JwkKey> keyStore = new ConcurrentHashMap<>();

    @Getter
    private volatile String defaultKeyId;

    /**
     * 生成JWT令牌
     * @param secretKey 密钥，用于签名JWT令牌, 也可传 keyId
     * @param timeout 令牌有效期，单位为秒
     * @param claims 令牌中包含的声明信息
     * @return 生成的JWT令牌字符串
     */
    public String getToken(String secretKey, long timeout, Map<String, Object> claims) {
        // 判断是 keyId 还是原密钥
        JwkKey key = resolveKey(secretKey);

        if (key == null) {
            throw new IllegalArgumentException("JWK not found: " + secretKey);
        }

        long currentTime = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date(currentTime))
            .subject("system")
            .issuer("huahai")
            .expiration(new Date(currentTime + timeout * 1000))
            .claims(claims)
            .header()
            .add("kid", key.keyId())
            .and();

        // 根据密钥类型签名
        if (key.rsa()) {
            builder.signWith(key.privateKey());
        } else {
            builder.signWith(key.secretKey());
        }

        return builder.compact();
    }

    /**
     * 从JWT令牌中提取载荷(Payload)信息
     *
     * @param secretKey 密钥，参数兼容：可传 keyId，也可传 null（自动从 token 提取）
     * @param token JWT令牌
     * @return 声明信息，如果令牌无效或过期则返回null
     */
    public Claims getClaimsBody(String secretKey, String token) {
        try {
            // 无密钥参数或密钥不匹配时，从 token 提取 kid
            String keyId = (secretKey == null || secretKey.length() < 20)
                ? extractKid(token)
                : resolveKeyId(secretKey);

            JwkKey key = (keyId != null) ? keyStore.get(keyId) : keyStore.get(defaultKeyId);
            if (key == null) {
                return null;
            }

            JwtParserBuilder parser = Jwts.parser();

            if (key.rsa()) {
                parser.verifyWith(key.publicKey());
            } else {
                parser.verifyWith(key.secretKey());
            }

            return parser.build().parseSignedClaims(token).getPayload();

        } catch (ExpiredJwtException e) {
            return null;
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT令牌中提取头部(Header)信息
     *
     * @param token JWT令牌
     * @return 头部信息
     */
    public JwsHeader getHeaderBody(String secretKey, String token) {
        try {
            String keyId = extractKid(token);
            JwkKey key = (keyId != null) ? keyStore.get(keyId) : keyStore.get(defaultKeyId);

            if (key == null) {
                return null;
            }

            JwtParserBuilder parser = Jwts.parser();
            if (key.rsa()) {
                parser.verifyWith(key.publicKey());
            } else {
                parser.verifyWith(key.secretKey());
            }

            return parser.build().parseSignedClaims(token).getHeader();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 Token 状态
     * @param claims 声明
     * @param refreshTime 刷新阈值（秒）
     * @return -1: 有效无需刷新, 0: 有效建议刷新, 1: 已过期, 2: 验证异常
     */
    public int verifyToken(Claims claims, long refreshTime) {
        if (claims == null) {
            return 1;
        }
        try {
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return 1;
            }
            if ((expiration.getTime() - System.currentTimeMillis()) > refreshTime * 1000) {
                return -1;
            } else {
                return 0;
            }
        } catch (ExpiredJwtException ex) {
            return 1;
        } catch (Exception e) {
            return 2;
        }
    }

    /**
     * 根据字符串生成加密密钥
     *
     * @return SecretKey 加密密钥对象
     */
    public void generalKey(String keyId, String secretKey) {
        // Base64 编码处理
        byte[] encodedKey = Base64.getEncoder().encode(secretKey.getBytes());
        SecretKey key = Keys.hmacShaKeyFor(encodedKey);

        JwkKey wrapper = new JwkKey(keyId, key, null, null, false);
        keyStore.put(keyId, wrapper);

        if (defaultKeyId == null) {
            defaultKeyId = keyId;
        }

        log.info("初始化对称密钥: {}", keyId);
    }

    /**
     * 生成 RSA 密钥对（JWK 模式特有）
     * @return 公钥 PEM 字符串（需保存）
     */
    public String generateRsaKey(String keyId) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            JwkKey wrapper = new JwkKey(keyId, null, privateKey, publicKey, true);
            keyStore.put(keyId, wrapper);
            defaultKeyId = keyId;

            // 导出公钥 PEM
            String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";

        } catch (Exception e) {
            throw new RuntimeException("RSA 密钥生成失败", e);
        }
    }

    /**
     * 加载已有 RSA 密钥
     */
    public void loadRsaKey(String keyId, String privateKeyBase64, String publicKeyBase64) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            PrivateKey privateKey = kf.generatePrivate(
                new PKCS8EncodedKeySpec(
                    Base64.getDecoder().decode(privateKeyBase64)
                )
            );

            PublicKey publicKey = kf.generatePublic(
                new X509EncodedKeySpec(
                    Base64.getDecoder().decode(publicKeyBase64)
                )
            );

            JwkKey wrapper = new JwkKey(keyId, null, privateKey, publicKey, true);
            keyStore.put(keyId, wrapper);

            if (defaultKeyId == null) {
                defaultKeyId = keyId;
            }

        } catch (Exception e) {
            throw new RuntimeException("RSA 密钥加载失败", e);
        }
    }

    /**
     * 导出公钥 JWK Set
     */
    public Map<String, Object> exportJwks() {
        List<Map<String, Object>> keys = new ArrayList<>();

        for (JwkKey key : keyStore.values()) {
            if (!key.rsa()) {
                continue;
            }

            Map<String, Object> jwk = new HashMap<>();
            jwk.put("kty", "RSA");
            jwk.put("kid", key.keyId());
            jwk.put("use", "sig");
            jwk.put("alg", "RS256");

            // Base64URL 编码公钥参数
            RSAPublicKey rsaPub = (RSAPublicKey) key.publicKey();

            jwk.put("n", base64UrlEncode(rsaPub.getModulus().toByteArray()));
            jwk.put("e", base64UrlEncode(rsaPub.getPublicExponent().toByteArray()));

            keys.add(jwk);
        }

        return Map.of("keys", keys);
    }

    // ==================== 内部辅助 ====================

    private JwkKey resolveKey(String secretKey) {
        // 先尝试作为 keyId 查找
        JwkKey key = keyStore.get(secretKey);
        if (key != null) {
            return key;
        }

        // 未找到，使用默认密钥
        return keyStore.get(defaultKeyId);
    }

    private String resolveKeyId(String secretKey) {
        // 检查是否是已注册的 keyId
        if (keyStore.containsKey(secretKey)) {
            return secretKey;
        }
        return null;
    }

    private String extractKid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String header = new String(Decoders.BASE64URL.decode(parts[0]));

            // 简单 JSON 提取
            if (header.contains("\"kid\"")) {
                int start = header.indexOf("\"kid\"") + 6;
                int quote1 = header.indexOf("\"", start);
                int quote2 = header.indexOf("\"", quote1 + 1);
                return header.substring(quote1 + 1, quote2);
            }
        } catch (Exception e) {
            log.debug("提取kid失败: {}", e.getMessage());
        }
        return null;
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private record JwkKey(String keyId, SecretKey secretKey, PrivateKey privateKey, PublicKey publicKey, boolean rsa) {
    }

}
