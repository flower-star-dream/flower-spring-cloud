package top.flowerstardream.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: 花海
 * @Date: 2025/04/14/13:00
 * @Description: Redisson锁注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonLock {
    /** SpEL 表达式，支持 #参数名 */
    String key();
    /** 等待时间（秒），默认3秒 */
    long waitTime() default 3;
    /** 租约时间（秒），-1启用看门狗续期 */
    long leaseTime() default -1;
    /** 失败提示 */
    String failMsg() default "操作过于频繁，请稍后重试";
}