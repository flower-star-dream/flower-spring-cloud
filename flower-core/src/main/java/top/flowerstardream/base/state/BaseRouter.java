package top.flowerstardream.base.state;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.bo.dto.BaseStatusDTO;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.mapper.BaseMapperX;

import java.util.Map;
import java.util.function.Function;

import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * @param <T> 业务实体类
 * @param <M> Mapper类
 * @Author: 花海
 * @Date: 2025/12/16/22:41
 * @Description: 基础状态路由
 */
@Slf4j
@Component
public class BaseRouter<T extends BaseEO, M extends BaseMapperX<T>> extends ServiceImpl<M, T>
        implements IStateRouter<BaseStatus, BaseEvent, T, Object>{
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
    private final Map<BaseEvent, Function<Object, BaseStatus>> DISPATCHER =
            Map.of(
                BaseEvent.START_OR_STOP,  this::startOrStop
            );

    @Override
    public Map<BaseStatus, Map<BaseEvent, BaseStatus>> getStateEventTargetConfig() {
        return CONFIG;
    }

    @Override
    public Map<BaseEvent, Function<Object, BaseStatus>> getEventDispatcher() {
        return DISPATCHER;
    }

    public BaseStatus startOrStop(Object param) {
        @Valid BaseStatusDTO<T> baseStatusDTO = (BaseStatusDTO<T>) param;
        T data = baseStatusDTO.getData();
        data.setId(baseStatusDTO.getId());
        if (data instanceof StatusAble) {
            ((StatusAble) data).setStatus(baseStatusDTO.getStatus());
        }
        boolean update = updateById(data);
        if (!update) {
            log.error("更新状态失败：{}", data);
            throw MODIFICATION_FAILED.toException();
        }
        return BaseStatus.valueOf(baseStatusDTO.getStatus());
    }
}
