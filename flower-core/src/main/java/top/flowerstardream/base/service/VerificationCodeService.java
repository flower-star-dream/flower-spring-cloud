package top.flowerstardream.base.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.flowerstardream.base.exception.IExceptionEnum;
import top.flowerstardream.base.properties.OtherProperties;
import top.flowerstardream.base.properties.VerifyProperties;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static top.flowerstardream.base.constant.BaseRedisKeyConstant.*;
import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * 验证码服务接口
 * @author 花海
 * @date 2026/02/09/20:32
 * @description 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     * @param target 目标
     * @param type  类型
     */
    void sendCode(String target, String type);

    /**
     * 验证验证码
     *
     * @param target   目标
     * @param type     类型
     * @param inputCode 用户输入的验证码
     * @return true-验证成功，false-验证失败（验证码错误）
     * @throws RuntimeException 验证码过期或已被锁定
     */
    boolean verifyCode(String target, String type, String inputCode);

    /**
     * 重新发送验证码（便捷方法）
     */
    void resendCode(String target, String type);

    /**
     * 获取剩余有效时间（用于前端显示）
     */
    Long getRemainingTime(String target, String type);
}