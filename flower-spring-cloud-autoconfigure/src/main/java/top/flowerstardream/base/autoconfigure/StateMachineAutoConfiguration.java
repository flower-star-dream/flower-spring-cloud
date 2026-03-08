package top.flowerstardream.base.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import top.flowerstardream.base.beans.factory.StateMachineBeanRegistrar;

/**
 * @Author: 花海
 * @Date: 2026/03/08/23:05
 * @Description: 状态机自动配置类
 */
@AutoConfiguration
public class StateMachineAutoConfiguration {

    @Bean
    public StateMachineBeanRegistrar stateMachineBeanRegistrar() {
        return new StateMachineBeanRegistrar();
    }
}