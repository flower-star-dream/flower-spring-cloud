package top.flowerstardream.base.beans.factory;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.annotation.AutoStateMachine;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.state.IBaseEvent;
import top.flowerstardream.base.state.IBaseState;
import top.flowerstardream.base.state.StateMachine;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 状态机工厂自动注入器
 * @Author: 花海
 * @Date: 2026/03/08/23:05
 * @Description: 实现 BeanPostProcessor 接口，在 Bean 初始化前扫描并注入带有 @AutoStateMachine 注解的状态机字段
 */
@Slf4j
@RequiredArgsConstructor
public class StateMachineAutoInjector implements BeanPostProcessor {

    private final ObjectFactory<StateMachineFactory> factoryObjectFactory;

    /**
     * 在 Bean 初始化之前处理，扫描字段并注入状态机
     * @param bean 当前正在处理的 Bean 实例
     * @param beanName Bean 的名称
     * @return 处理后的 Bean 实例
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 跳过 Spring 基础设施 Bean，避免过早触发
        if (isInfrastructureBean(bean)) {
            return bean;
        }

        // 延迟获取，如果工厂还未就绪，记录字段待后续注入
        StateMachineFactory factory;
        try {
            factory = factoryObjectFactory.getObject();
        } catch (BeansException e) {
            // 工厂还未就绪，跳过此 Bean，它会在 factory 就绪后被重新处理
            // 或：使用 @Lazy 代理延迟注入
            log.debug("StateMachineFactory未为{}准备好，延迟注入", beanName);
            return bean;
        }

        // 遍历所有字段
        for (Field field : bean.getClass().getDeclaredFields()) {
            AutoStateMachine annotation = field.getAnnotation(AutoStateMachine.class);
            // 跳过未标注 @AutoStateMachine 注解的字段
            if (annotation == null) {
                continue;
            }
            
            // 解析字段的泛型类型：StateMachine<S, E, D>
            Type fieldType = field.getGenericType();
            if (!(fieldType instanceof ParameterizedType pt)) {
                continue;
            }

            // 获取泛型参数数组
            Type[] args = pt.getActualTypeArguments();
            // 确保泛型参数数量为 3（状态、事件、数据）
            if (args.length != 3) {
                continue;
            }
            
            // 解析具体的 Class 类型
            Class<?> sClass = resolveClass(args[0]);
            Class<?> eClass = resolveClass(args[1]);
            Class<?> dClass = resolveClass(args[2]);
            
            // 如果任一类型解析失败，则跳过该字段
            if (sClass == null || eClass == null || dClass == null) {
                continue;
            }

            try {
                // 从工厂获取对应的状态机实例
                @SuppressWarnings("unchecked")
                StateMachine<?, ?, ?> fsm = factory.getMachine(
                    (Class<? extends IBaseState<?>>) sClass,
                    (Class<? extends IBaseEvent<?>>) eClass,
                    (Class<? extends BaseEO>) dClass
                );
                // 通过反射将状态机注入到字段中
                field.setAccessible(true);
                field.set(bean, fsm);
                log.debug(">>> 注入状态机到 {}.{}", beanName, field.getName());
            } catch (IllegalAccessException e) {
                throw new RuntimeException("状态机注入失败: " + beanName + "." + field.getName(), e);
            }
        }
        return bean;
    }

    private boolean isInfrastructureBean(Object bean) {
        // 跳过 Spring 内部 Bean，避免循环
        String className = bean.getClass().getName();
        return className.startsWith("org.springframework.") ||
               className.contains("Tomcat") ||
               className.contains("WebServer");
    }
    
    /**
     * 解析 Type 为具体的 Class 对象
     * @param type 待解析的类型
     * @return 解析后的 Class 对象，若无法解析则返回 null
     */
    private Class<?> resolveClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }
        // 处理泛型边界等情况，当前暂不支持
        return null;
    }
}