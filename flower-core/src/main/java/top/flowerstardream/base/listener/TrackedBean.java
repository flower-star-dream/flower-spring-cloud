package top.flowerstardream.base.listener;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 花海
 * @date 2026/03/20/23:25
 * @description bean追踪信息
 */
@Data
public class TrackedBean {
    final String name;
    final String definedClass;
    final String source;
    volatile Status status = Status.DEFINED;
    volatile String actualType;
    volatile long instanceStartTime;
    volatile long initCostTime;
    volatile boolean definitionConfirmed;
}