package top.flowerstardream.base.utils;


import top.flowerstardream.base.context.RequestContext;

/**
 * @Author: 花海
 * @Date: 2025/11/09/15:26
 * @Description: 获取信息工具类
 */
public final class GetInfoUtil {
    public static String getTraceId() {
        RequestContext ctx = TtlContextHolder.get();
        return ctx.getTraceId();
    }

    public static Long getOperatorId() {
        RequestContext ctx = TtlContextHolder.get();
        return ctx.getOperatorId();
    }

    public static String getOperatorName() {
        RequestContext ctx = TtlContextHolder.get();
        return ctx.getOperatorName();
    }

    public static String getToken() {
        RequestContext ctx = TtlContextHolder.get();
        return ctx.getToken();
    }

    // 类型安全地获取扩展数据
    public static <T> T getExtra(String key, Class<T> type) {
        RequestContext ctx = TtlContextHolder.get();
        if (ctx == null) {
            return null;
        }
        return ctx.getExtra(key, type);
    }
}
