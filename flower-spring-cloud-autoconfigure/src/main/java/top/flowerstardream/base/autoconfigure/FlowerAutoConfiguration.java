package top.flowerstardream.base.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 自动配置类
 * @author 花海
 * @date 2026/03/08/03:14
 */
@AutoConfiguration
@ComponentScan({
    // 只扫描包含 Spring Bean 注解的包，排除纯工具类/常量包
    "top.flowerstardream.base.service",
    "top.flowerstardream.base.controller.common",
    "top.flowerstardream.base.handler",
    "top.flowerstardream.base.context",
    "top.flowerstardream.base.beans.factory",
    "top.flowerstardream.base.properties",
})
public class FlowerAutoConfiguration {
}