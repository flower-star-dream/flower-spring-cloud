package top.flowerstardream.base.service.Impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.flowerstardream.base.enums.ITCEnum;
import top.flowerstardream.base.exception.IExceptionEnum;
import top.flowerstardream.base.properties.OtherProperties;
import top.flowerstardream.base.properties.VerifyProperties;
import top.flowerstardream.base.service.MailService;
import top.flowerstardream.base.service.VerificationCodeService;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static top.flowerstardream.base.constant.BaseRedisKeyConstant.*;
import static top.flowerstardream.base.enums.ITCEnum.*;
import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * 验证码服务实现类
 * @author 花海
 * @date 2026/02/09/20:32
 * @description 验证码服务实现类
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Resource
    private MailService mailService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private VerifyProperties verifyProperties;

    @Resource
    private OtherProperties otherProperties;

    private final Long codeExpireSeconds = verifyProperties.getCode().getExpire();
    private final Integer codeLength = verifyProperties.getCode().getLength();
    private final String codeType = verifyProperties.getCode().getType();
    private final Long sendIntervalSeconds = verifyProperties.getLimit().getInterval();
    private final Integer dailyMaxCount = verifyProperties.getLimit().getDaily();
    private final Integer maxErrorCount = verifyProperties.getLimit().getError();
    private final String appName = otherProperties.getAppName();

    /**
     * 发送验证码
     * @param target
     * @param type
     */
    @Override
    public void sendCode(String target, ITCEnum type) {
        // 1. 参数校验
        if (!StringUtils.hasText(target) || type == null) {
            throw EMPTY_PARAMETER.toException();
        }

        target = target.trim().toLowerCase();
        log.info("开始发送验证码，目标：{}，类型：{}", maskTarget(target), type);

        // 2. 检查发送间隔（防高频发送）
        checkSendInterval(target, type);

        // 3. 检查每日发送次数（防短信/邮件轰炸）
        checkDailyLimit(target, type);

        // 4. 检查是否已有有效验证码（防止重复生成）
        String existCode = getExistCode(target, type);
        String code;
        code = generateCode();
        if (existCode != null) {
            // 可选：如果已有有效验证码，可以选择重新发送同一个，或者生成新的
            // 这里选择生成新的，但你可以根据业务调整
            log.debug("已有有效验证码，重新生成新的验证码");
        }

        // 5. 异步发送（防止阻塞主线程）
        asyncSend(type, target, code, appName);

        // 6. 存入 Redis 并更新限流计数
        saveCodeToRedis(target, type, code);
        updateLimitCounter(target, type);

        log.info("验证码发送成功，目标：{}", maskTarget(target));
    }

    /**
     * 验证验证码
     *
     * @param target   目标
     * @param type     类型
     * @param inputCode 用户输入的验证码
     * @return true-验证成功，false-验证失败（验证码错误）
     * @throws RuntimeException 验证码过期或已被锁定
     */
    @Override
    public boolean verifyCode(String target, ITCEnum type, String inputCode) {
        if (!StringUtils.hasText(target) || !StringUtils.hasText(inputCode)) {
            return false;
        }

        target = target.trim().toLowerCase();
        String key = buildKey(KEY_CODE, type, target);

        // 1. 获取存储的验证码
        String correctCode = stringRedisTemplate.opsForValue().get(key);

        if (!StringUtils.hasText(correctCode)) {
            log.warn("验证码已过期或不存在，目标：{}", maskTarget(target));
            throw VERIFICATION_EXCEPTION.toException();
        }

        // 2. 检查错误次数（防暴力破解）
        String errorKey = buildKey(KEY_ERROR, type, target);
        String errorCountStr = stringRedisTemplate.opsForValue().get(errorKey);
        int errorCount = errorCountStr == null ? 0 : Integer.parseInt(errorCountStr);

        if (errorCount >= maxErrorCount) {
            log.warn("验证码错误次数过多，已锁定，目标：{}", maskTarget(target));
            // 清除验证码
            stringRedisTemplate.delete(key);
            stringRedisTemplate.delete(errorKey);
            throw VERIFICATION_EXCEPTION.toException();
        }

        // 3. 验证
        if (correctCode.equalsIgnoreCase(inputCode)) {
            // 验证成功，删除相关记录
            stringRedisTemplate.delete(key);
            stringRedisTemplate.delete(errorKey);
            // 可选：清除发送间隔，允许立即重发
            stringRedisTemplate.delete(buildKey(KEY_INTERVAL, type, target));

            log.info("验证码验证成功，目标：{}", maskTarget(target));
            return true;
        } else {
            // 验证失败，增加错误计数
            long remainSeconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set(errorKey, String.valueOf(errorCount + 1),
                    remainSeconds > 0 ? remainSeconds : codeExpireSeconds, TimeUnit.SECONDS);
            log.warn("验证码错误，目标：{}，当前错误次数：{}", maskTarget(target), errorCount + 1);
            return false;
        }
    }

    /**
     * 重新发送验证码（便捷方法）
     */
    @Override
    public void resendCode(String target, ITCEnum type) {
        // 清除旧的发送间隔限制，允许立即重发
        String intervalKey = buildKey(KEY_INTERVAL, type, target);
        stringRedisTemplate.delete(intervalKey);
        sendCode(target, type);
    }

    /**
     * 获取剩余有效时间（用于前端显示）
     */
    @Override
    public Long getRemainingTime(String target, ITCEnum type) {
        String key = buildKey(KEY_CODE, type, target);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire > 0 ? expire : 0L;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 异步发送验证码
     */
    @Async("messageExecutor")  // 需要配置线程池
    public void asyncSend(ITCEnum type, String target, String code, String appName) {
        try {
            int expireMinutes = (int) (codeExpireSeconds / 60);

            if (EMAIL.equals(type)) {
                mailService.sendVerificationCode(target, code, expireMinutes, appName);
            } else if (SMS.equals(type)) {
                // TODO: 接入短信服务
                // smsService.sendSms(target, code);
                log.info("短信发送模拟：目标 {}，验证码 {}", maskTarget(target), code);
            } else {
                throw VERIFICATION_EXCEPTION.toException();
            }
        } catch (Exception e) {
            log.error("验证码发送失败，目标：{}，错误：{}", maskTarget(target), e.getMessage());
            // 发送失败不抛出异常，但记录日志，用户端显示发送成功，但实际可能失败
            // 或者可以选择抛出异常让事务回滚（如果需要）
            throw VERIFICATION_EXCEPTION.toException();
        }
    }

    /**
     * 检查发送间隔
     */
    private void checkSendInterval(String target, ITCEnum type) {
        String key = buildKey(KEY_INTERVAL, type, target);
        Boolean exists = stringRedisTemplate.hasKey(key);

        if (exists) {
            Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            String message = String.format("请%d秒后再试", ttl);
            throw IExceptionEnum.of(10010, message).toException();
        }
    }

    /**
     * 检查每日发送限制
     */
    private void checkDailyLimit(String target, ITCEnum type) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String key = String.format(KEY_DAILY, type, target, today);

        String countStr = stringRedisTemplate.opsForValue().get(key);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= dailyMaxCount) {
            throw TOO_MANY_REQUESTS.toException();
        }
    }

    /**
     * 保存验证码到 Redis
     */
    private void saveCodeToRedis(String target, ITCEnum type, String code) {
        String key = buildKey(KEY_CODE, type, target);
        stringRedisTemplate.opsForValue().set(key, code, codeExpireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 更新限流计数器
     */
    private void updateLimitCounter(String target, ITCEnum type) {
        // 1. 发送间隔
        String intervalKey = buildKey(KEY_INTERVAL, type, target);
        stringRedisTemplate.opsForValue().set(intervalKey, "1", sendIntervalSeconds, TimeUnit.SECONDS);

        // 2. 每日计数（当天剩余时间）
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String dailyKey = String.format(KEY_DAILY, type, target, today);

        stringRedisTemplate.opsForValue().increment(dailyKey);
        // 设置当天过期（到第二天凌晨）
        long secondsUntilMidnight = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                java.time.LocalDate.now().plusDays(1).atStartOfDay()
        ).getSeconds();
        stringRedisTemplate.expire(dailyKey, secondsUntilMidnight, TimeUnit.SECONDS);
    }

    /**
     * 获取已存在的有效验证码（用于判断是否重复发送）
     */
    private String getExistCode(String target, ITCEnum type) {
        String key = buildKey(KEY_CODE, type, target);
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 生成验证码
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        String chars = switch (codeType.toLowerCase()) {
            // 去除易混淆字符
            case "alpha" -> "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
            // 去除 0,1,I,O 等易混淆字符
            case "alphanumeric" -> "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
            default -> "0123456789";
        };

        for (int i = 0; i < codeLength; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * 构建 Redis Key
     */
    private String buildKey(String pattern, ITCEnum type, String target) {
        return String.format(pattern, type.getType(), target);
    }

    /**
     * 脱敏处理（日志中隐藏部分信息）
     */
    private String maskTarget(String target) {
        if (target == null || target.length() < 5) {
            return "****";
        }
        // 邮箱：a***@example.com
        if (target.contains("@")) {
            int atIndex = target.indexOf('@');
            return target.charAt(0) + "***" + target.substring(atIndex);
        }
        // 手机号：138****8888
        return target.substring(0, 3) + "****" + target.substring(target.length() - 4);
    }
}