package top.flowerstardream.base.listener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.flowerstardream.base.state.BaseEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 花海
 * @Date: 2026/03/21/01:59
 * @Description: Bean描述枚举
 */
@Getter
@AllArgsConstructor
public enum BeanDescEnums implements IBeanDescEnums {
    ComponentScan(List.of("component"), "ComponentScan扫描"),
    Bean(List.of("@Bean method"), "@Bean方法"),
    INJECTOR(List.of("injector"), "注入器类"),
    ASPECT(List.of("aspect"), "切面类"),
    BEANS_FACTORY(List.of("beans.factory"), "Bean工厂注册"),
    STATE_MACHINE(List.of("stateMachine"), "状态机类"),
    REGISTRAR(List.of("Registrar"), "注册器类"),
    PROPERTY(List.of("Properties"), "配置属性类"),
    SERVICE(List.of("service"), "@Service类"),
    CONTROLLER(List.of("controller"), "@Controller类"),
    MAPPER(List.of("mapping"), "@Mapping类"),
    RESOLVER(List.of("resolver"), "解析器类"),
    UTIL(List.of("util"), "工具类"),
    CONTEXT(List.of("context"), "上下文类"),
    LISTENER(List.of("listener"), "监听器类"),
    ROUTER(List.of("router"), "路由类"),
    SCANNER(List.of("scanner"), "扫描类"),
    FILTER(List.of("filter"), "过滤器类"),
    SPRING_APPLICATION(List.of("Application"), "SpringApplication类"),
    CLIENT(List.of("client"), "客户端类"),
    AUTO_CONFIGURATION(List.of("auto-configuration", "autoconfigure",  "autoConfig"), "自动配置"),
    CONFIGURATION(List.of("Configuration", "config"), "@Configuration类"),
    INTERCEPTOR(List.of("interceptor"), "拦截器类");

    private final List<String> desc;
    private final String value;

    static {
        IBeanDescEnums.registerEnum(BeanDescEnums.class);
    }

}
