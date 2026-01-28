package top.flowerstardream.base.state;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.exception.BizException;

import java.util.Map;
import java.util.Set;

import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * @param <S>
 * @param <E>
 * @param <P>
 *
 * @Author: 花海
 * @Date: 2025/12/16/14:41
 * @Description: 状态机
 */
@Component
@RequiredArgsConstructor
public class StateMachine<S extends IBaseState<?>,
                               E extends IBaseEvent<?>,
                               D extends BaseEO,
                               P> {

    @Resource
    private IStateRouter<S, E, D, P> router;

    /**
     * 触发状态迁移
     * @param currentState 当前状态枚举
     * @param event        事件枚举
     * @param param        业务实参数
     * @return 新的状态枚举
     */
    public S fire(S currentState, E event, P param) {
        /* 1. 合法性校验 */
        Map<E, S> event2Target = router.getStateEventTargetConfig().get(currentState);
        if (event2Target == null || !event2Target.containsKey(event)) {
            throw ILLEGAL_STATE_TRANSITION.toException();
        }
        if (currentState == null) {
            throw EMPTY_STATE.toException();
        }
        if (param == null) {
            throw EMPTY_PARAMETER.toException();
        }
        S target = router.route(event, param);
        if (target == null) {
            throw EMPTY_STATE.toException();
        }
        return target;
    }
}