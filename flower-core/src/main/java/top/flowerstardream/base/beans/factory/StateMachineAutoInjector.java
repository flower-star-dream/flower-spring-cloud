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
 * 状态机工厂自动注入器
 * @Author: 花海
 * @Date: 2026/03/08/23:05
 * @Description: 实现 BeanPostProcessor 接口，在 Bean 初始化前扫描并注入带有 @AutoStateMachine 注解的状态机字段
 */
@Component
public class StateMachineAutoInjector implements BeanPostProcessor, ApplicationContextAware {
    
    /** Spring 应用上下文 */
    private ApplicationContext ctx;
    
    /** 状态机工厂，用于获取具体的状态机实例 */
    private StateMachineFactory factory;

    /**
     * 设置应用上下文，并从中获取状态机工厂实例
     * @param ctx Spring 应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        this.ctx = ctx;
        this.factory = ctx.getBean(StateMachineFactory.class);
    }

    /**
     * 在 Bean 初始化之前处理，扫描字段并注入状态机
     * @param bean 当前正在处理的 Bean 实例
     * @param beanName Bean 的名称
     * @return 处理后的 Bean 实例
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 遍历所有字段
        for (Field field : bean.getClass().getDeclaredFields()) {
            // 跳过未标注 @AutoStateMachine 注解的字段
            if (!field.isAnnotationPresent(AutoStateMachine.class)) {
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
            
            // 从工厂获取对应的状态机实例
            @SuppressWarnings("unchecked")
            StateMachine<?, ?, ?> fsm = factory.getMachine(
                (Class<? extends IBaseState<?>>) sClass,
                (Class<? extends IBaseEvent<?>>) eClass,
                (Class<? extends BaseEO>) dClass
            );
            
            // 通过反射将状态机注入到字段中
            field.setAccessible(true);
            try {
                field.set(bean, fsm);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to inject StateMachine", e);
            }
        }
        return bean;
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
        // 处理泛型边界等情况，当前暂不支持
        return null;
    }
}