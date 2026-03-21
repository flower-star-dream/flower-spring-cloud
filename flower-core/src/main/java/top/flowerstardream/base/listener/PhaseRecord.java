package top.flowerstardream.base.listener;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 花海
 * @date 2026/03/20/23:25
 * @description 阶段记录
 */
@Data
@AllArgsConstructor
public class PhaseRecord {
    final String phase;
    final String title;
    final String description;
    final long startTime;
    long costTime;
}