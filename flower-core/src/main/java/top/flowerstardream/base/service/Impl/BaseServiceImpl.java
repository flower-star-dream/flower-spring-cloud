package top.flowerstardream.base.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import top.flowerstardream.base.ao.req.BaseStatusChangeREQ;
import top.flowerstardream.base.bo.dto.BaseStatusDTO;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.mapper.BaseMapperX;
import top.flowerstardream.base.service.IBaseService;
import top.flowerstardream.base.state.*;

import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * @Author: 花海
 * @Date: 2025/12/16/23:51
 * @Description: 基础服务实现类
 */
@Slf4j
public class BaseServiceImpl<T extends BaseMapperX<E>, E extends BaseEO> extends ServiceImpl<T, E> implements IBaseService<E> {
    @Resource
    private StateMachine<BaseStatus, BaseEvent, E, Object> fsm;

    @Resource
    private T mapper;

    /**
     * 启用或禁用
     * @param req 状态更改请求
     */
    @Override
    public void startOrStop(BaseStatusChangeREQ req) {
        E data = mapper.selectById(req.getId());
        BaseStatus status = null;
        if (data instanceof StatusAble) {
            BaseStatusDTO<E> baseStatusDTO = BaseStatusDTO.<E>builder()
                    .id(req.getId())
                    .status(req.getStatus())
                    .data(data)
                    .build();
            status = fsm.fire(BaseStatus.valueOf(((StatusAble) data).getStatus()), BaseEvent.START_OR_STOP, baseStatusDTO);
        }
        if (status == null) {
            throw MODIFICATION_FAILED.toException();
        }
    }
}
