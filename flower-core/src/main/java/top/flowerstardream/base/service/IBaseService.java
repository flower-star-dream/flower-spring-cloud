package top.flowerstardream.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.flowerstardream.base.ao.req.BaseStatusChangeREQ;

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
    void startOrStop(BaseStatusChangeREQ req);
}
