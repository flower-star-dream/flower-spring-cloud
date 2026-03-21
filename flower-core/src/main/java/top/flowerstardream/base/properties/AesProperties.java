package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.annotation.AutoConfigProperties;

/**
 * @Author: 花海
 * @Date: 2026/03/11/15:52
 * @Description: AES 加密配置
 */
@Data
@AutoConfigProperties(prefix = "aes")
@ConfigurationProperties(prefix = "aes")
public class AesProperties {
    public String key;
    private String algorithm = "AES/GCM/NoPadding";
    private int gcmIvLength = 12;
    private int gcmTagLength = 128;
}
