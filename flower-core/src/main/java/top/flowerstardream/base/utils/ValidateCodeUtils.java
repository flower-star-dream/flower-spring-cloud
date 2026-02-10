package top.flowerstardream.base.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机生成验证码工具类
 * @Author: 花海
 * @Date: 2026/02/09/20:32
 * @Description: 随机生成验证码工具类
 */
public final class ValidateCodeUtils {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    /**
     * 随机生成验证码
     * @param length 长度
     * @return
     */
    public static Integer generateValidateCode(int length){
        // 生成指定长度的随机数字字符串
        StringBuilder sb = new StringBuilder();
        sb.append(RANDOM.nextInt(9) + 1);
        for (int i = 1; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        // 返回结果（已自动补0）
        return Integer.parseInt(sb.toString());
    }

    /**
     * 随机生成指定长度字符串验证码
     * @param length 长度
     * @return
     */
    public static String generateValidateCode4String(int length){
        Random rdm = new Random();
        String hash1 = Integer.toHexString(rdm.nextInt());
        return hash1.substring(0, length);
    }
}
