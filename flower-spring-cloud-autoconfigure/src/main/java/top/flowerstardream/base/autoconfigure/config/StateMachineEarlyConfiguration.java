// ========== 文件1：StateMachineEarlyConfiguration.java ==========
// 位置：src/main/java/top/flowerstardream/base/configuration/
// 作用：极早期注册，确保Processor及时生效

package top.flowerstardream.base.autoconfigure.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.flowerstardream.base.beans.factory.StateMachineAutoInjector;
import top.flowerstardream.base.beans.factory.StateMachineFactory;
import top.flowerstardream.base.handler.MyMetaObjectHandler;

/**
 * 状态机早期配置类
 * 职责：注册基础设施，确保在组件扫描阶段就可用
 * @author 花海
 * @date 2023/03/20/16:28
 * @description: 状态机早期配置类
 */
@Configuration(proxyBeanMethods = false)  // 无CGLIB代理，启动更快
@ConditionalOnClass({MybatisPlusInterceptor.class, MyMetaObjectHandler.class})
public class StateMachineEarlyConfiguration {
    
    // 显式注册Factory，避免@Component扫描的不确定性
    @Bean
    public StateMachineFactory stateMachineFactory(ApplicationContext ctx) {
        return new StateMachineFactory(ctx);
    }

    @Bean
    public static StateMachineAutoInjector stateMachineAutoInjector(
            ObjectProvider<StateMachineFactory> factoryProvider) {
        return new StateMachineAutoInjector(factoryProvider);
    }
}