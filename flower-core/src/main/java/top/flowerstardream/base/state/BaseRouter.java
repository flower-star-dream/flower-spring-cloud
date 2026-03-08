package top.flowerstardream.base.state;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.bo.dto.BaseStatusDTO;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.mapper.BaseMapperX;
import top.flowerstardream.base.utils.StateRouteParams;

import java.util.Map;
import java.util.function.Function;

import static top.flowerstardream.base.exception.BaseExceptionEnum.*;
import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * @param <E> 业务实体类
 * @param <M> Mapper类
 * @Author: 花海
 * @Date: 2025/12/16/22:41
 * @Description: 基础状态路由
 */
@Slf4j
public abstract class BaseRouter<M extends BaseMapper<E>, E extends BaseEO & StatusAble<BaseStatus>> extends ServiceImpl<M, E>
        implements IStateRouter<BaseStatus, BaseEvent, E> {

    @Resource
    @Lazy
    private IStateRouter<BaseStatus, BaseEvent, E> self;
    /**
     * 状态×事件 → 目标状态  配置表
     */
    private static final Map<BaseStatus, Map<BaseEvent, BaseStatus>> CONFIG = Map.of(
        BaseStatus.ENABLE, Map.of(
                BaseEvent.START_OR_STOP, BaseStatus.DISABLE),
        BaseStatus.DISABLE, Map.of(
                BaseEvent.START_OR_STOP, BaseStatus.ENABLE)
    );

    /**
     * 事件 → 业务实现  路由表
     */
    private final Map<BaseEvent, Function<StateRouteParams, BaseStatus>> DISPATCHER =
            Map.of(
                BaseEvent.START_OR_STOP,  this::startOrStop
            );

    @Override
    public Map<BaseStatus, Map<BaseEvent, BaseStatus>> getStateEventTargetConfig() {
        return CONFIG;
    }

    @Override
    public Map<BaseEvent, Function<StateRouteParams, BaseStatus>> getEventDispatcher() {
        return DISPATCHER;
    }

    public BaseStatus startOrStop(StateRouteParams param) {
        BaseStatusDTO<E, BaseStatus> baseStatusDTO = param.getParam("baseStatusDTO");
        E data = baseStatusDTO.getData();
        data.setId(baseStatusDTO.getId());
        if (baseStatusDTO.getStatus() != null) {
            data.setStatus(baseStatusDTO.getStatus());
        }
        if (!self.updateById(data)) {
            log.error("更新状态失败：{}", data);
            throw MODIFICATION_FAILED.toException();
        }
        return data.getStatus();
    }
}
