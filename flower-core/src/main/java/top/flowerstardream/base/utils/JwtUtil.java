package top.flowerstardream.base.utils;

import io.jsonwebtoken.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * JWT工具类
 * @Author: 花海
 * @Date: 2025/11/09/15:26
 * @Description: JWT工具类
 */
public class JwtUtil {
    /**
     * 生成JWT令牌
     * @param secretKey 密钥，用于签名JWT令牌
     * @param timeout 令牌有效期，单位为秒
     * @param claims 令牌中包含的声明信息
     * @return 生成的JWT令牌字符串
     */
    public static String getToken(String secretKey, long timeout, Map<String, Object> claims){
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                // 设置签发时间
                .issuedAt(new Date(currentTime))
                // 设置主题
                .subject("system")
                // 设置签发者
                .issuer("huahai")
                // 使用HS512算法签名
                .signWith(generalKey(secretKey))
                // 设置过期时间
                .expiration(new Date(currentTime + timeout * 1000))
                // 添加声明信息
                .claims(claims)
                .compact();
    }

    /**
     * 解析JWT令牌并获取其中的声明信息
     * 
     * @param token JWT令牌
     * @return 包含声明信息的Jws对象
     */
    private static Jws<Claims> getJws(String secretKey, String token) {
            JwtParser parser = Jwts.parser()
                .verifyWith(generalKey(secretKey))
                .build();
            return parser.parseSignedClaims(token);
    }

    /**
     * 从JWT令牌中提取载荷(Payload)信息
     * 
     * @param secretKey 密钥
     * @param token JWT令牌
     * @return 声明信息，如果令牌无效或过期则返回null
     */
    public static Claims getClaimsBody(String secretKey, String token) {
        try {
            return getJws(secretKey, token).getPayload();
        }catch (Exception e){
            // 捕获所有JWT相关异常，包括过期、签名错误、格式错误等
            return null;
        }
    }

    /**
     * 从JWT令牌中提取头部(Header)信息
     * 
     * @param token JWT令牌
     * @return 头部信息
     */
    public static JwsHeader getHeaderBody(String secretKey, String token) {
        return getJws(secretKey, token).getHeader();
    }

    /**
     * 验证JWT令牌是否过期
     * 
     * @param claims 声明信息
     * @return -1: 有效且无需刷新, 0: 有效但需要刷新, 1: 已过期, 2: 验证异常
     */
    public static int verifyToken(Claims claims, long refreshTime) {
        if(claims==null){
            return 1;
        }
        try {
            Date expiration = claims.getExpiration();
            // 判断是否已过期
            if (expiration.before(new Date())) {
                return 1;
            }
            // 判断是否需要自动刷新TOKEN
            if((expiration.getTime()-System.currentTimeMillis())> refreshTime*1000){
                return -1;
            }else {
                return 0;
            }
        } catch (ExpiredJwtException ex) {
            return 1;
        }catch (Exception e){
            return 2;
        }
    }

    /**
     * 根据字符串生成加密密钥
     * 
     * @return SecretKey 加密密钥对象
     */
    public static SecretKey generalKey(String secretKey) {
        byte[] encodedKey = Base64.getEncoder().encode(secretKey.getBytes());
        return new SecretKeySpec(encodedKey, "HmacSHA512");
    }

}
