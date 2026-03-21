package top.flowerstardream.base.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import top.flowerstardream.base.annotation.AutoStateMachine;
import top.flowerstardream.base.ao.req.BaseStatusChangeREQ;
import top.flowerstardream.base.ao.res.BaseStatusRES;
import top.flowerstardream.base.bo.dto.BaseStatusDTO;
import top.flowerstardream.base.bo.eo.AuditBaseEO;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.mapper.BaseMapperX;
import top.flowerstardream.base.service.IBaseService;
import top.flowerstardream.base.state.*;
import top.flowerstardream.base.utils.StateRouteParams;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * @Author: 花海
 * @Date: 2025/12/16/23:51
 * @Description: 基础服务实现类
 */
@Slf4j
public abstract class BaseServiceImpl<M extends BaseMapper<E>, E extends BaseEO & StatusAble<BaseStatus>> extends ServiceImpl<M, E> implements IBaseService<E> {

    @AutoStateMachine
    private StateMachine<BaseStatus, BaseEvent, E> fsm;

    /**
     * 启用或禁用
     * @param req 状态更改请求
     */
    @Override
    public void startOrStop(BaseStatusChangeREQ<BaseStatus> req) {
        E data = getBaseMapper().selectById(req.getId());
        BaseStatus status = null;
        if (data != null) {
            BaseStatusDTO<E, BaseStatus> baseStatusDTO = BaseStatusDTO.<E, BaseStatus>builder()
                    .id(req.getId())
                    .status(req.getStatus())
                    .data(data)
                    .build();
            StateRouteParams stateRouteParams = StateRouteParams
                    .create()
                    .addParam("baseStatusDTO", baseStatusDTO);
            status = fsm.fire(data.getStatus(), BaseEvent.START_OR_STOP, stateRouteParams);
        }
        if (status == null) {
            throw MODIFICATION_FAILED.toException();
        }
    }

    @Override
    public List<BaseStatusRES<BaseStatus>> getStatus() {
        QueryWrapper<E> wrapper = new QueryWrapper<>();
        wrapper.groupBy("status").select("status", "count(*) as count");
        // 使用LambdaQueryWrapper进行分组统计
        List<Map<String, Object>> statusCounts = getBaseMapper().selectMaps(wrapper);

        // 将统计结果转换为StatusRES列表
        return statusCounts.stream()
            .map(map -> {
                BaseStatusRES<BaseStatus> statusRES = new BaseStatusRES<>();
                statusRES.setStatus((BaseStatus) map.get("status"));
                statusRES.setCount((Integer) map.get("count"));
                statusRES.setDescription(statusRES.getStatus().getName());
                return statusRES;
            })
            .collect(Collectors.toList());
    }

}
