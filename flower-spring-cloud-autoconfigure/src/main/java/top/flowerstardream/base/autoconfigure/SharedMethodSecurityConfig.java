package top.flowerstardream.base.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.util.StringUtils;

/**
 * 共享方法级安全配置
 * 所有业务服务引入起步依赖后自动生效
 * @author 花海
 */
@AutoConfiguration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SharedMethodSecurityConfig {

    @Value("${security.role-hierarchy:ROLE_ADMIN > ROLE_USER}")
    private String roleHierarchyString;

    @Bean
    public RoleHierarchy roleHierarchy() {
        // 从配置读取，支持多行格式
        String hierarchy = roleHierarchyString.replace("\n", " ");

        if (!StringUtils.hasText(hierarchy)) {
            // 默认最小层级
            hierarchy = "ROLE_ADMIN > ROLE_USER";
        }

        return RoleHierarchyImpl.fromHierarchy(hierarchy);
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }
}