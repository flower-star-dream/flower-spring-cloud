package top.flowerstardream.base.autoconfigure;

import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.flowerstardream.base.aspect.RedissonLockAspect;
import top.flowerstardream.base.template.RedissonLockTemplate;

/**
 * @Author: 花海
 * @Date: 2026/04/14/13:21
 * @Description: Redisson自动配置类
 */
@AutoConfiguration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties({RedisProperties.class, RedissonProperties.class})
@ConditionalOnClass({RedisConnectionFactory.class, StringRedisTemplate.class, RedissonClient.class})
public class RedissonAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public RedissonLockAspect redissonLockAspect(RedissonClient redissonClient) {
        return new RedissonLockAspect(redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public RedissonLockTemplate redissonLockTemplate(RedissonClient redissonClient) {
        return new RedissonLockTemplate(redissonClient);
    }
}
