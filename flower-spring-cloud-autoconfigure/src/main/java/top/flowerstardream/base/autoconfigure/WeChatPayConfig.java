package top.flowerstardream.base.autoconfigure;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import top.flowerstardream.base.properties.WeChatProperties;
import top.flowerstardream.base.utils.WeChatPayUtil;

/**
 * @Author: 花海
 * @Date: 2026/03/09/15:30
 * @Description: 微信支付配置类
 */
@AutoConfiguration
@ConditionalOnClass(name = {
    "com.wechat.pay.java.core.Config",
    "com.wechat.pay.java.service.payments.jsapi.JsapiService"
})
@ConditionalOnProperty(prefix = "wechat", name = "mchSerialNo")
public class WeChatPayConfig {

    @Bean
    public Config wechatPayConfig(WeChatProperties props) {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(props.getMchid())
                .privateKeyFromPath(props.getPrivateKeyFilePath())
                .merchantSerialNumber(props.getMchSerialNo())
                .apiV3Key(props.getApiV3Key())
                .build();
    }

    @Bean
    public JsapiService jsapiService(Config config) {
        return new JsapiService.Builder().config(config).build();
    }

    @Bean
    public JsapiServiceExtension jsapiServiceExtension(Config config) {
        return new JsapiServiceExtension.Builder().config(config).build();
    }

    @Bean
    public RefundService refundService(Config config) {
        return new RefundService.Builder().config(config).build();
    }

    @Bean
    public WeChatPayUtil weChatPayUtil(WeChatProperties props, JsapiService jsapiService, JsapiServiceExtension jsapiServiceExtension, RefundService refundService) {
        return new WeChatPayUtil(props, jsapiService, jsapiServiceExtension, refundService);
    }
}
