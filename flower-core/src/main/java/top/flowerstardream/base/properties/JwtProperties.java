package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hcd.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String employeeSecretKey; // 密钥
    private long employeeTtl; // 过期时间
    private String employeeTokenName; // 令牌名称
    private long employeeRefreshTime; // 刷新时间

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey; // 密钥
    private long userTtl; // 过期时间
    private String userTokenName; // 令牌名称
    private long userRefreshTime; // 刷新时间

}
