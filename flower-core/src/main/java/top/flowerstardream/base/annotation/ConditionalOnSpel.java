package top.flowerstardream.base.annotation;

import org.springframework.context.annotation.Conditional;
import top.flowerstardream.base.condition.OnSpelCondition;

import java.lang.annotation.*;


/**
 * @Author: 花海
 * @Date: 2026/03/21/23:26
 * @Description: SpEL表达式条件注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnSpelCondition.class)
public @interface ConditionalOnSpel {
    String value() default "true";
}