package top.flowerstardream.base.resolver;

import java.util.Collection;
import java.util.Map;

/**
 * @Author: 花海
 * @Date: 2026/03/10/22:21
 * @Description: 用户ID -> Name 解析器
 */
public interface UserNameResolver {
    /**
     * 批量解析用户ID -> Name
     * @param userIds 用户ID集合
     * @return Map<userId, userName>
     */
    Map<Long, String> resolve(Collection<Long> userIds);
}