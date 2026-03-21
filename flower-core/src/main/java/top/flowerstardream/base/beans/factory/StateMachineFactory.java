package top.flowerstardream.base.beans.factory;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.state.IBaseEvent;
import top.flowerstardream.base.state.IBaseState;
import top.flowerstardream.base.state.StateMachine;

import java.beans.Introspector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 花海
 * @Date: 2026/03/08/23:05
 * @Description: 状态机工厂类
 */
@RequiredArgsConstructor
public class StateMachineFactory {

    private final ApplicationContext ctx;
    
    private final Map<String, StateMachine<?, ?, ?>> cache = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <S extends IBaseState<?>, E extends IBaseEvent<?>, D extends BaseEO>
           StateMachine<S, E, D> getMachine(Class<S> s, Class<E> e, Class<D> d) {
        
        String key = s.getName() + "#" + e.getName() + "#" + d.getName();
        
        return (StateMachine<S, E, D>) cache.computeIfAbsent(key, k -> {
            String machineName = Introspector.decapitalize(
                d.getSimpleName().replace("EO", "Machine")
            );
            // 使用getBeanProvider避免NoSuchBeanDefinitionException
            ObjectProvider<StateMachine<?, ?, ?>> provider = ctx.getBeanProvider(
                ResolvableType.forClassWithGenerics(StateMachine.class, s, e, d)
            );
            return provider.getIfAvailable(() -> {
                // 回退：尝试按名称获取
                return ctx.getBean(machineName, StateMachine.class);
            });
        });
    }
}