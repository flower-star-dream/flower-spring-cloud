package top.flowerstardream.base.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import top.flowerstardream.base.listener.SpringBootStartupTracer;
import top.flowerstardream.base.properties.BeanTracerProperties;

/**
 * Bean生命周期追踪自动配置
 * @Author: 花海
 * @Date: 2026/03/20/21:35
 * @Description: 启用Bean生命周期追踪自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(BeanTracerProperties.class)
@ConditionalOnProperty(prefix = "bean.tracer", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(SpringBootStartupTracer.class)
public class BeanTracerAutoConfiguration {

}