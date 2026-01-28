package top.flowerstardream.base.state;

import java.io.Serializable;

/**
 * @Author: 花海
 * @Date: 2025/12/15/21:21
 * @Description: 基础状态接口
 */
public interface IBaseState<C extends Serializable> {
    C getCode();
    String getName();
}