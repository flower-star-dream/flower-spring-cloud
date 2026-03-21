package top.flowerstardream.base.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 花海
 * @Date: 2026/03/20/17:55
 * @Description: 属性配置自动注入
 */
@Configuration
@ConfigurationPropertiesScan("top.flowerstardream.base.properties")
public class AutoPropertiesConfig {
}
