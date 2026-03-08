package top.flowerstardream.base.state;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * @Author: 花海
 * @Date: 2025/12/16/23:37
 * @Description: 状态判断接口
 */
public interface StatusAble<E extends IEnum<?> & IBaseState<?>> {
    void setStatus(E status);
    E getStatus();
}
