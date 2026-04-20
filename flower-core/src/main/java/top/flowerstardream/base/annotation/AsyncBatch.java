package top.flowerstardream.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 批量操作异步执行注解
 * 用于标记需要异步执行的批量操作方法
 *
 * @Author: 花海
 * @Date: 2026/04/14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncBatch {

    /**
     * 触发异步的最小数据量
     * 当批量操作的数据量大于等于此值时，才会异步执行
     * 默认为 10
     */
    int threshold() default 10;

    /**
     * 使用的线程池名称
     * 默认使用 businessExecutor
     */
    String executor() default "businessExecutor";

    /**
     * 是否启用
     */
    boolean enabled() default true;

    /**
     * 超时时间（秒）
     * 异步执行的超时时间，超过则抛出异常
     */
    int timeout() default 30;
}
