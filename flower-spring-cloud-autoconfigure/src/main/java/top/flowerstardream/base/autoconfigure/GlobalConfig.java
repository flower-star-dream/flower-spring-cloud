package top.flowerstardream.base.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author: 花海
 * @Date: 2026/04/02/18:17
 * @Description: 全局自动配置
 */
@AutoConfiguration
@ComponentScan("top.flowerstardream.base.exception")
public class GlobalConfig {

}
