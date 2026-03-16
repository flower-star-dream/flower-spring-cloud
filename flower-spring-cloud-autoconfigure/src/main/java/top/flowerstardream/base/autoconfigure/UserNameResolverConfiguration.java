package top.flowerstardream.base.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import top.flowerstardream.base.aspect.UserNameConvertAspect;
import top.flowerstardream.base.resolver.UserNameResolver;
import top.flowerstardream.base.resolver.UserNameResolverProvider;

/**
 * @Author: 花海
 * @Date: 2026/03/10/22:21
 * @Description: 用户名称解析器配置
 */
@AutoConfiguration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class UserNameResolverConfiguration {
    
    /**
     * 业务服务定义了 UserNameResolver 就用它
     * 没定义就不创建 Provider，启动时会报错提示
     */
    @Bean
    @ConditionalOnBean(UserNameResolver.class)
    public UserNameResolverProvider userNameResolverProvider(UserNameResolver resolver) {
        return () -> resolver;
    }

    /**
     * 兜底：如果没定义 Resolver，给个空的，启动时抛异常提示
     */
    @Bean
    @ConditionalOnMissingBean(UserNameResolver.class)
    public UserNameResolverProvider emptyProvider() {
        return () -> ids -> {
            throw new IllegalStateException(
                "请在业务服务定义 UserNameResolver Bean，例如 RemoteUserNameResolver"
            );
        };
    }
    
    @Bean
    public UserNameConvertAspect userNameConvertAspect() {
        return new UserNameConvertAspect();
    }
}