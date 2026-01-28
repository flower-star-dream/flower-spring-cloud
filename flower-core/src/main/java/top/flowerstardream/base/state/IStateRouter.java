package top.flowerstardream.base.state;

import com.baomidou.mybatisplus.extension.service.IService;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.exception.BizException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * 所有业务路由实现此接口
 * @param <S> 状态枚举
 * @param <E> 事件枚举
 * @param <D> 业务实体类
 * @param <P> 业务参数
 *
 * @Author: 花海
 * @Date: 2025/12/16/22:18
 * @Description: 状态路由接口
 */
public interface IStateRouter<S extends IBaseState<?>, E extends IBaseEvent<?>, D extends BaseEO, P> extends IService<D>{

    /**
     * 状态×事件 合法组合表
     * Map<当前状态, Map<事件, 目标状态>>
     */
    Map<S, Map<E, S>> getStateEventTargetConfig();

    /**
     * 事件业务逻辑表
     * Map<事件, Function<参数包, 目标状态>>
     */
    Map<E, Function<P, S>> getEventDispatcher();

    /**
     * 统一入口：引擎唯一调用
     */
    default S route(E event, P param) {
        Function<P, S> func = getEventDispatcher().get(event);
        if (func == null) {
            throw EVENT_ROUTER_ERROR.toException();
        }
        return func.apply(param);
    }
}