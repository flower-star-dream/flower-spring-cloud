package top.flowerstardream.base.utils;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;

/**
 * 微信小程序数据解密
 * @author 花海
 * @date 2026/03/19/03:04
 * @description 微信小程序数据解密
 */
public class WechatDecryptUtil {
    
    static {
        // 注册 BouncyCastle 提供方（支持 AES/CBC/PKCS7Padding）
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 解密微信加密数据
     * @param encryptedData 前端传来的 encryptedData（Base64）
     * @param sessionKey    通过 code2Session 获取的 session_key（Base64）
     * @param iv            前端传来的 iv（Base64）
     * @return 解密后的 JSON 字符串
     */
    public static String decrypt(String encryptedData, String sessionKey, String iv) throws Exception {
        // 1. Base64 解码
        byte[] dataBytes = Base64.decodeBase64(encryptedData);
        byte[] keyBytes = Base64.decodeBase64(sessionKey);
        byte[] ivBytes = Base64.decodeBase64(iv);
        
        // 2. 初始化密钥（AES-128，key 必须是 16 字节）
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(ivBytes));
        
        // 3. 创建 Cipher 实例（PKCS7Padding 即 PKCS5Padding 的扩展）
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, params);
        
        // 4. 解密
        byte[] decrypted = cipher.doFinal(dataBytes);
        
        // 5. 去除填充（PKCS7 会自动处理，但如果手动处理）
        // 实际上 BouncyCastle 的 PKCS7Padding 已经自动去除填充
        
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    /**
     * 专门解析手机号
     */
    public static String getPhoneNumber(String encryptedData, String sessionKey, String iv) {
        try {
            String json = decrypt(encryptedData, sessionKey, iv);
            // 微信返回格式：{"phoneNumber":"13800138000","purePhoneNumber":"13800138000","countryCode":"86","watermark":{"appid":"wx...","timestamp":1234567890}}
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.get("phoneNumber").getAsString();
        } catch (Exception e) {
            throw new RuntimeException("解密手机号失败，可能 session_key 不匹配或数据过期", e);
        }
    }
}