package top.flowerstardream.base.beans.factory;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import top.flowerstardream.base.annotation.StateRouter;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.state.IBaseEvent;
import top.flowerstardream.base.state.IBaseState;
import top.flowerstardream.base.state.IStateRouter;

import java.util.Arrays;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * @Author: 花海
 * @Date: 2026/03/08/23:05
 * @Description: 状态机工厂类注册器
 */
@Slf4j
public class StateMachineBeanRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    /**
     * 在 Bean 定义注册阶段保存 registry 引用，以便在后续工厂处理阶段使用
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {

    }

    /**
     * 处理 Bean 工厂，扫描所有实现了 IStateRouter 接口的 Bean
     * 根据泛型参数动态注册对应的状态机 Bean (StateMachineFactoryBean)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {

        // 安全获取registry
        BeanDefinitionRegistry registry = (factory instanceof BeanDefinitionRegistry)
            ? (BeanDefinitionRegistry) factory
            : null;

        // 确保 registry 可用，否则抛出异常
        if (registry == null) {
            throw new IllegalStateException("BeanDefinitionRegistry not available");
        }

        // 获取所有类型为 IStateRouter 的 Bean 名称
        String[] routerNames = factory.getBeanNamesForType(IStateRouter.class);
        log.debug(">>> 发现路由：{}", Arrays.toString(routerNames));
        
        for (String routerName : routerNames) {
            Class<?> routerClass = factory.getType(routerName);
            // 跳过未找到类或未标注 @StateRouter 注解的路由
            if (routerClass == null || AnnotationUtils.findAnnotation(routerClass, StateRouter.class) == null) {
                continue;
            }

            // 解析 IStateRouter 接口的泛型参数：State, Event, Data
            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(
                routerClass, IStateRouter.class
            );
            // 确保泛型参数数量为 3，否则跳过
            if (generics == null || generics.length != 3) {
                continue;
            }

            // 提取具体的泛型类型
            Class<? extends IBaseState<?>> stateClass = (Class<? extends IBaseState<?>>) generics[0];
            Class<? extends IBaseEvent<?>> eventClass = (Class<? extends IBaseEvent<?>>) generics[1];
            Class<? extends BaseEO> dataClass = (Class<? extends BaseEO>) generics[2];

            // 生成状态机 Bean 的名称
            String machineName = generateMachineName(routerName);
            // 检查是否已存在，避免重复注册
            if (registry.containsBeanDefinition(machineName)) {
                log.debug(">>> 状态机 {} 已存在，跳过", machineName);
                continue;
            }
            log.debug(">>> 注册状态机：{}", machineName);

            // 构建状态机工厂 Bean 的定义
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(StateMachineFactoryBean.class)
                    // 注入状态类
                .addConstructorArgValue(stateClass)      
                    // 注入事件类
                .addConstructorArgValue(eventClass)      
                    // 注入数据实体类
                .addConstructorArgValue(dataClass)       
                    // 注入路由 Bean 引用
                .addPropertyReference("router", routerName)
                    // 确保提前实例化（如果需要）
                .setLazyInit(false);

            // 注册 Bean 定义到容器
            registry.registerBeanDefinition(machineName, builder.getBeanDefinition());
            log.debug(">>> 状态机注册成功：{}", machineName);
        }
    }

    private Class<?> resolveBeanClass(BeanDefinition def, String beanName) {
        // 优先从resolvedClass获取，避免过早触发类加载
        if (def instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) def).hasBeanClass()) {
            return ((AbstractBeanDefinition) def).getBeanClass();
        }
        String className = def.getBeanClassName();
        if (className == null) {
            return null;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.warn("无法加载类: {}", className);
            return null;
        }
    }

    /**
     * 设置执行优先级
     */
    @Override
    public int getOrder() {
        // 在常规Processor之后，但在Bean实例化之前
        // 确保在ConfigurationClassPostProcessor之后执行（它处理@ComponentScan）
        return LOWEST_PRECEDENCE - 100;
    }

    /**
     * 根据路由 Bean 名称生成对应的状态机 Bean 名称
     * 规则：若名称以 "Router" 结尾，则替换为 "Machine"；否则直接追加 "Machine"
     */
    private String generateMachineName(String routerName) {
        if (routerName.endsWith("Router")) {
            return routerName.substring(0, routerName.length() - 6) + "Machine";
        }
        return routerName + "Machine";
    }
}