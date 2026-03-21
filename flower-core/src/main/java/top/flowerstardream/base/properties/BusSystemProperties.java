package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.flowerstardream.base.annotation.AutoConfigProperties;

/**
 * @Author: 花海
 * @Date: 2026/03/18/01:54
 * @Description: 业务系统属性配置
 */
@Data
@AutoConfigProperties(prefix = "system")
@ConfigurationProperties(prefix = "system")
public class BusSystemProperties {
    private String ip;
    private String port;
    private String domain;
}
