package top.flowerstardream.base.beans.factory;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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

    // 保存 registry 供后续使用
    private BeanDefinitionRegistry registry;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
        if (this.registry == null && factory instanceof BeanDefinitionRegistry) {
            this.registry = (BeanDefinitionRegistry) factory;
        }

        if (this.registry == null) {
            throw new IllegalStateException("BeanDefinitionRegistry not available");
        }

        String[] routerNames = factory.getBeanNamesForType(IStateRouter.class);
        log.debug(">>> 发现路由: {}", Arrays.toString(routerNames));
        for (String routerName : routerNames) {
            Class<?> routerClass = factory.getType(routerName);
            if (routerClass == null || AnnotationUtils.findAnnotation(routerClass, StateRouter.class) == null) {
                continue;
            }

            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(
                routerClass, IStateRouter.class
            );
            if (generics == null || generics.length != 3) {
                continue;
            }

            Class<? extends IBaseState<?>> stateClass = (Class<? extends IBaseState<?>>) generics[0];
            Class<? extends IBaseEvent<?>> eventClass = (Class<? extends IBaseEvent<?>>) generics[1];
            Class<? extends BaseEO> dataClass = (Class<? extends BaseEO>) generics[2];

            String machineName = generateMachineName(routerName);
            log.debug(">>> 注册状态机: {}", machineName);

            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(StateMachineFactoryBean.class)
                .addConstructorArgValue(stateClass)
                .addConstructorArgValue(eventClass)
                .addConstructorArgValue(dataClass)
                .addPropertyReference("router", routerName);

            registry.registerBeanDefinition(machineName, builder.getBeanDefinition());
            log.debug(">>> 状态机注册成功: {}", machineName);
        }
    }

    @Override
    public int getOrder() {
        // 确保尽早执行
        return HIGHEST_PRECEDENCE;
    }

    private String generateMachineName(String routerName) {
        if (routerName.endsWith("Router")) {
            return routerName.substring(0, routerName.length() - 6) + "Machine";
        }
        return routerName + "Machine";
    }
}