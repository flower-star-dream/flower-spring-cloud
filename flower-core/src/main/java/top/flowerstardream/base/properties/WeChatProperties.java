package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.annotation.AutoConfigProperties;

/**
 * @Author: 花海
 * @Date: 2025/11/04/17:01
 * @Description: 微信支付配置
 */
@Data
@AutoConfigProperties(prefix = "wechat")
@ConfigurationProperties(prefix = "wechat")
public class WeChatProperties {

    //小程序的appid
    private String appid;
    // 商户号
    private String mchid;
    // 商户API证书的证书序列号
    private String mchSerialNo;
    // 小程序的秘钥
    private String secret;
    // 商户私钥文件
    private String privateKeyFilePath;
    // 证书解密的密钥
    private String apiV3Key;
    // 支付成功的回调地址
    private String notifyUrl;
    // 退款成功的回调地址
    private String refundNotifyUrl;

}
