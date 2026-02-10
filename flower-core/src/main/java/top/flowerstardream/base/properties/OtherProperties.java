package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: 花海
 * @Date: 2026/02/08/15:55
 * @Description: 其他配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "other")
public class OtherProperties {
    // 数据库名称
    private String dataBaseName;
    // 应用名称
    private String appName;
}
