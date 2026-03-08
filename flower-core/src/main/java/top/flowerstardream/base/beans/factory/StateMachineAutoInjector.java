package top.flowerstardream.base.beans.factory;

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
 * @Author: 花海
 * @Date: 2026/03/08/23:05
 * @Description: 状态机工厂自动注入
 */
@Component
public class StateMachineAutoInjector implements BeanPostProcessor, ApplicationContextAware {
    
    private ApplicationContext ctx;
    private StateMachineFactory factory;

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        this.ctx = ctx;
        this.factory = ctx.getBean(StateMachineFactory.class);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 遍历所有字段
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(AutoStateMachine.class)) {
                continue;
            }
            
            // 解析字段的泛型类型: StateMachine<S, E, D>
            Type fieldType = field.getGenericType();
            if (!(fieldType instanceof ParameterizedType pt)) {
                continue;
            }

            Type[] args = pt.getActualTypeArguments();
            if (args.length != 3) {
                continue;
            }
            
            // 获取具体的 Class
            Class<?> sClass = resolveClass(args[0]);
            Class<?> eClass = resolveClass(args[1]);
            Class<?> dClass = resolveClass(args[2]);
            
            if (sClass == null || eClass == null || dClass == null) {
                continue;
            }
            
            // 从工厂获取 StateMachine
            @SuppressWarnings("unchecked")
            StateMachine<?, ?, ?> fsm = factory.getMachine(
                (Class<? extends IBaseState<?>>) sClass,
                (Class<? extends IBaseEvent<?>>) eClass,
                (Class<? extends BaseEO>) dClass
            );
            
            // 反射注入
            field.setAccessible(true);
            try {
                field.set(bean, fsm);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to inject StateMachine", e);
            }
        }
        return bean;
    }
    
    private Class<?> resolveClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        // 处理泛型边界等情况
        return null;
    }
}