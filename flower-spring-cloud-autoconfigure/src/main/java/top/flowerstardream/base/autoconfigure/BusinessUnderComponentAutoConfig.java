package top.flowerstardream.base.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.ComponentScan;
import top.flowerstardream.base.service.FileStorageService;

/**
 * @Author: 花海
 * @Date: 2026/03/20/17:36
 * @Description: 启用业务底层组件自动配置
 */
@AutoConfiguration
@AutoConfigureAfter(value = {
    ThreadPoolConfig.class,
    MinIOConfig.class
})
@ComponentScan({"top.flowerstardream.base.service", "top.flowerstardream.base.controller"})
public class BusinessUnderComponentAutoConfig {
}
