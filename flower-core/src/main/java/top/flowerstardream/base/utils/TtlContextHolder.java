package top.flowerstardream.base.utils;


import com.alibaba.ttl.TransmittableThreadLocal;
import top.flowerstardream.base.context.RequestContext;

/**
 * @Author: 花海
 * @Date: 2025/11/06/22:15
 * @Description: TTL上下文
 */
public final class TtlContextHolder {
    private static final TransmittableThreadLocal<RequestContext> TTL =
            new TransmittableThreadLocal<>();

    public static void set(RequestContext ctx) { TTL.set(ctx); }
    public static RequestContext get()        { return TTL.get(); }
    public static void clear()                { TTL.remove(); }
}