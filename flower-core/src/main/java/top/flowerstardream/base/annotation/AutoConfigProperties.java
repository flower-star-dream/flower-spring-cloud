package top.flowerstardream.base.annotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;
import top.flowerstardream.base.condition.OnPropertiesEnabledCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @Author: 花海
 * @Date: 2026/03/08/15:57
 * @Description: 条件注解，用于根据配置文件中的属性值来决定是否加载某个类
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnPropertiesEnabledCondition.class)
public @interface AutoConfigProperties {
    @AliasFor("prefix")
    String value() default "";

    @AliasFor("value")
    String prefix() default "";

    boolean ignoreInvalidFields() default false;

    boolean ignoreUnknownFields() default true;
}