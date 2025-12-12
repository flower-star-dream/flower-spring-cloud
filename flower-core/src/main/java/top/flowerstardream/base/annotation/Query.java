package top.flowerstardream.base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Author: 花海
 * @Date: 2025/12/09/23:48
 * @Description: 自动查询注解
 */
public interface Query {
    // 需要忽略的字段可以打这个标记
    @Retention(RetentionPolicy.RUNTIME)
    @interface Ignore {}

    // 需要手动指定查询方式（like、ge、le...）的标记
    @Retention(RetentionPolicy.RUNTIME)
    @interface Condition {
        String value() default "eq"; // 例如 "like", "ge", "le", "in" ...
        String left() default "";
        String right() default "";
    }
}