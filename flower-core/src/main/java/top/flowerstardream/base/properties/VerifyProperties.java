package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: 花海
 * @Date: 2026/02/10/01:08
 * @Description: 验证码配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "verify")
public class VerifyProperties {

    private Code code = new Code();
    private Limit limit = new Limit();

    @Data
    public static class Code {
        // 验证码过期时间（秒），默认5分钟
        private Long expire = 300L;
        // 验证码长度，默认6位
        private Integer length = 6;
        // 验证码类型：numeric(纯数字), alpha(纯字母), alphanumeric(数字+字母)
        private String type = "numeric";
    }

    @Data
    public static class Limit {
        // 发送间隔（秒），默认60秒
        private Long interval = 60L;
        // 每日最大发送次数，默认5次
        private Integer daily = 5;
        // 最大验证错误次数，默认3次
        private Integer error = 3;
    }

}
