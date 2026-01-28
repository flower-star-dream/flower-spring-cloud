package top.flowerstardream.base.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring上下文
 * @Author: 花海
 * @Date: 2025/12/13/02:09
 * @Description: Spring上下文
 */
@Component
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext ctx;
    @Override public void setApplicationContext(@NonNull ApplicationContext ac) { ctx = ac; }
    public static <T> T getBean(String name) { return (T) ctx.getBean(name); }
    public static <T> T getBean(Class<T> clazz) { return ctx.getBean(clazz); }
}