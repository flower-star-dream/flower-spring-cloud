package top.flowerstardream.base.beans.factory;

import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.state.IBaseEvent;
import top.flowerstardream.base.state.IBaseState;
import top.flowerstardream.base.state.IStateRouter;
import top.flowerstardream.base.state.StateMachine;

/**
 * 状态机工厂类
 * @author JAM
 * @date 2026/03/08/23:14
 * @param <S> 状态枚举
 * @param <E> 事件枚举
 * @param <D> 数据对象
 */
@Data
public class StateMachineFactoryBean<S extends IBaseState<?>,
                                      E extends IBaseEvent<?>,
                                      D extends BaseEO>
        implements FactoryBean<StateMachine<S, E, D>>, InitializingBean {

    private final Class<S> stateClass;
    private final Class<E> eventClass;
    private final Class<D> dataClass;
    private IStateRouter<S, E, D> router;
    private StateMachine<S, E, D> instance;

    public StateMachineFactoryBean(Class<S> stateClass, Class<E> eventClass, Class<D> dataClass) {
        this.stateClass = stateClass;
        this.eventClass = eventClass;
        this.dataClass = dataClass;
    }

    @Override
    public void afterPropertiesSet() {
        if (router == null) {
            throw new IllegalStateException("Router must be set");
        }
        this.instance = new StateMachine<>(router);
    }

    @Override
    public StateMachine<S, E, D> getObject() {
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return StateMachine.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}