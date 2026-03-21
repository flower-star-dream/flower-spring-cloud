package top.flowerstardream.base.utils;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.properties.AesProperties;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 加密工具类
 * @Author: 花海
 * @Date: 2026/03/11/15:58
 * @Description: AES 加密工具类
 */
public class AesEncryptUtil {

    @Resource
    private AesProperties aesProperties;
    
    /**
     * 加密
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[aesProperties.getGcmIvLength()];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(aesProperties.getAlgorithm());
            GCMParameterSpec gcmSpec = new GCMParameterSpec(aesProperties.getGcmTagLength(), iv);
            SecretKeySpec keySpec = new SecretKeySpec(aesProperties.getKey().getBytes(StandardCharsets.UTF_8), "AES");
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // IV + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);
            
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
    
    /**
     * 解密
     */
    public String decrypt(String ciphertext) {
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);
            
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[aesProperties.getGcmIvLength()];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);
            
            Cipher cipher = Cipher.getInstance(aesProperties.getAlgorithm());
            GCMParameterSpec gcmSpec = new GCMParameterSpec(aesProperties.getGcmTagLength(), iv);
            SecretKeySpec keySpec = new SecretKeySpec(aesProperties.getKey().getBytes(StandardCharsets.UTF_8), "AES");
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}