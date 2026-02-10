package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 花海
 * @Date: 2025/11/04/17:00
 * @Description: 网关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class MyGatewayProperties {
    private List<String> whiteList = List.of(
            "/login",
            "/swagger-ui",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/webjars",
            "/webjars/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/doc.html",
            "/doc.html/**",
            "/favicon.ico",
            "/actuator",
            "/actuator/**",
            "/knife4j",
            "/knife4j/**"
    );
}
