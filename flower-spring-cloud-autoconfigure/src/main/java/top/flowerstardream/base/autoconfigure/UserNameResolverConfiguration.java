package top.flowerstardream.base.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import top.flowerstardream.base.aspect.UserNameConvertAspect;
import top.flowerstardream.base.handler.MyMetaObjectHandler;
import top.flowerstardream.base.resolver.UserNameResolver;
import top.flowerstardream.base.resolver.UserNameResolverProvider;

/**
 * @Author: 花海
 * @Date: 2026/03/10/22:21
 * @Description: 用户名称解析器配置
 */
@AutoConfiguration
@AutoConfigureAfter(RedisConnectionFactory.class)
@ConditionalOnClass({MybatisPlusInterceptor.class, MyMetaObjectHandler.class})
public class UserNameResolverConfiguration {

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
    @ConditionalOnBean(UserNameResolver.class)
    public UserNameConvertAspect userNameConvertAspect() {
        return new UserNameConvertAspect();
    }
}