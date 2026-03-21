package top.flowerstardream.base.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import top.flowerstardream.base.autoconfigure.config.ContextComponentConfig;
import top.flowerstardream.base.autoconfigure.config.StateMachineEarlyConfiguration;
import top.flowerstardream.base.config.AutoPropertiesConfig;

/**
 * 自动配置类
 * @author 花海
 * @date 2026/03/08/03:14
 */
@AutoConfiguration
@Import({ContextComponentConfig.class, AutoPropertiesConfig.class})
public class FlowerAutoConfiguration {
}