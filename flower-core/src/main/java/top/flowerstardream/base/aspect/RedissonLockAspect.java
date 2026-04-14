package top.flowerstardream.base.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import top.flowerstardream.base.annotation.RedissonLock;

import java.util.concurrent.TimeUnit;

/**
 * @Author: 花海
 * @Date: 2025/04/14/13:00
 * @Description: Redisson锁切面
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class RedissonLockAspect {
    
    private final RedissonClient redissonClient;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(redissonLock)")
    public Object around(ProceedingJoinPoint point, RedissonLock redissonLock) throws Throwable {
        // 解析 SpEL 获取 key
        String lockKey = parseKey(point, redissonLock.key());
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            if (redissonLock.leaseTime() == -1) {
                locked = lock.tryLock(redissonLock.waitTime(), TimeUnit.SECONDS);
            } else {
                locked = lock.tryLock(
                    redissonLock.waitTime(),
                    redissonLock.leaseTime(),
                    TimeUnit.SECONDS
                );
            }

            if (!locked) {
                throw new RuntimeException(redissonLock.failMsg());
            }

            log.debug("[RedissonLock] 获取锁成功: {}", lockKey);
            return point.proceed();

        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[RedissonLock] 释放锁: {}", lockKey);
            }
        }
    }

    private String parseKey(ProceedingJoinPoint point, String spel) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Object[] args = point.getArgs();
        String[] paramNames = signature.getParameterNames();

        // 创建上下文并设置变量
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        return parser.parseExpression(spel).getValue(context, String.class);
    }
}