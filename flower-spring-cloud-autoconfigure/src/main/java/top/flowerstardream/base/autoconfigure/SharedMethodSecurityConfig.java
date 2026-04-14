package top.flowerstardream.base.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * 共享方法级安全配置
 * 所有业务服务引入起步依赖后自动生效
 * @author 花海
 */
@AutoConfiguration
@ConditionalOnClass({EnableMethodSecurity.class, SecurityFilterChain.class})  // 修改：明确指定需要的类
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SharedMethodSecurityConfig {

    @Value("${security.role-hierarchy:ROLE_ADMIN > ROLE_USER}")
    private String roleHierarchyString;

    @Bean
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    /**
     * 默认的安全过滤器链配置
     * 允许所有请求通过，具体权限控制交给方法级注解处理
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(AbstractHttpConfigurer::disable)
            // 禁用 CSRF（适用于无状态 API 服务）
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用 HTTP Basic 认证（避免浏览器弹出登录窗口）
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用表单登录（避免跳转到登录页面）
            .formLogin(AbstractHttpConfigurer::disable)
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 允许所有请求通过，方法级安全由 @EnableMethodSecurity 处理
                .anyRequest().permitAll()
            );

        return http.build();
    }
}