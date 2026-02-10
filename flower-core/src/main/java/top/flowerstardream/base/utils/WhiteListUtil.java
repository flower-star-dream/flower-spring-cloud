package top.flowerstardream.base.utils;

import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * @Author: 花海
 * @Date: 2025/11/04/17:07
 * @Description: 白名单工具类
 */
public final class WhiteListUtil {
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    public static boolean shouldSkip(String path, List<String> patterns) {
        return patterns.stream().anyMatch(p -> MATCHER.match(p, path));
    }
}
