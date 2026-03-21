package top.flowerstardream.base.annotation;

import java.lang.annotation.*;

/**
 * @Author: 花海
 * @Date: 2026/03/16/20:16
 * @Description: 追踪生命周期注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceLifecycle {
    /** 追踪名称，默认使用Bean名称 */
    String value() default "";
    /** 是否追踪其依赖的Bean */
    boolean traceDependencies() default false;
}