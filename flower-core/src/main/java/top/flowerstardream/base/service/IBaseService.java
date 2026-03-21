package top.flowerstardream.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.flowerstardream.base.ao.req.BaseStatusChangeREQ;
import top.flowerstardream.base.ao.res.BaseStatusRES;
import top.flowerstardream.base.state.BaseStatus;

import java.util.List;

/**
 * @Author: 花海
 * @Date: 2025/12/16/23:50
 * @Description: 基础服务接口
 */
public interface IBaseService<T> extends IService<T> {

    /**
     * 启用或禁用
     * @param req 状态更改请求
     */
    void startOrStop(BaseStatusChangeREQ<BaseStatus> req);

    /**
     * 获取状态列表
     * @return 状态列表
     */
    List<BaseStatusRES<BaseStatus>> getStatus();
}
