package top.flowerstardream.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: 花海
 * @Date: 2026/03/08/23:05
 * @Description: 状态路由标记注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StateRouter {
    // 标记用，无属性
}