package top.flowerstardream.base.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import top.flowerstardream.base.autoconfigure.config.StateMachineEarlyConfiguration;
import top.flowerstardream.base.beans.factory.StateMachineBeanRegistrar;
import top.flowerstardream.base.handler.MyMetaObjectHandler;

/**
 * @Author: 花海
 * @Date: 2026/03/08/23:05
 * @Description: 状态机自动配置类
 */
@AutoConfiguration
@ConditionalOnClass({MybatisPlusInterceptor.class, MyMetaObjectHandler.class})
@Import(StateMachineEarlyConfiguration.class)
public class StateMachineAutoConfiguration {

    /**
     * 注册 StateMachineBeanRegistrar
     * 注意：Boot 3.4 中 @Bean 注册的 BFPP 会在 ConfigurationClassPostProcessor 之后执行
     * 这正好满足需求（扫描 @Component 完成后再注册动态 Bean）
     */
    @Bean
    @ConditionalOnMissingBean
    public static StateMachineBeanRegistrar stateMachineBeanRegistrar() {
        return new StateMachineBeanRegistrar();
    }
}