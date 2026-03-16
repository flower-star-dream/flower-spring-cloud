package top.flowerstardream.base.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import top.flowerstardream.base.utils.WeChatPayUtil;

/**
 * @Author: 花海
 * @Date: 2026/03/09/15:30
 * @Description: 微信支付配置类
 */
@AutoConfiguration
public class WeChatPayConfig {

    @Bean
    public WeChatPayUtil weChatPayUtil() {
        return new WeChatPayUtil();
    }
}
