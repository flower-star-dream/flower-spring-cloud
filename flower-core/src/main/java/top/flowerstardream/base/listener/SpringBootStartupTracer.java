package top.flowerstardream.base.listener;

import cn.hutool.core.util.StrUtil;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.*;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import top.flowerstardream.base.properties.BeanTracerProperties;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Bean追踪监听器 - 支持从Bootstrap到启动完成的全周期追踪
 * @author 花海
 * @date 2026/03/20/20:43
 * @description Bean追踪监听器
 */
@NoArgsConstructor
public class SpringBootStartupTracer implements
        BootstrapRegistryInitializer,          // 1. 极早期（甚至早于SpringApplication创建）
        ApplicationListener<ApplicationEvent>,  // 2. 环境准备 & 上下文事件
        BeanDefinitionRegistryPostProcessor,    // 3. Bean定义注册（图中2.3阶段）
        BeanFactoryPostProcessor,               // 4. Bean工厂后置（图中2.4阶段）
        InstantiationAwareBeanPostProcessor,    // 5. 实例化前后（图中2.5阶段）
        BeanPostProcessor,                      // 6. 初始化前后
        SmartInitializingSingleton,             // 7. 所有单例就绪后
        CommandLineRunner,                      // 8. 应用启动完成前
        Ordered {

    static {
        // 类被加载时会打印，验证是否在启动早期被加载
        System.out.println(">>> SpringBootStartupTracer 类已加载 - " + LocalDateTime.now());
    }

    private static final Logger log = LoggerFactory.getLogger(SpringBootStartupTracer.class);

    // ========== 追踪配置（解耦，通过注解或配置类指定） ==========

    private static BeanTracerProperties properties;
    // 默认开启，等配置加载后再判断
    private static final AtomicBoolean enabled = new AtomicBoolean(true);
    private static final AtomicBoolean propertiesLoaded = new AtomicBoolean(false);

    // 追踪目标收集器
    private static final Set<Class<?>> trackedInterfaces = ConcurrentHashMap.newKeySet();
    private static final Set<Class<? extends Annotation>> trackedAnnotations = ConcurrentHashMap.newKeySet();
    private static final Set<String> trackedPackages = ConcurrentHashMap.newKeySet();
    private static final List<String> namePatterns = new ArrayList<>();

    // Bean状态追踪表
    private static final Map<String, TrackedBean> beanRegistry = new ConcurrentHashMap<>();
    private static final List<PhaseRecord> phaseTimeline = new ArrayList<>();

    private static long startupStartTime;
    private ConfigurableApplicationContext applicationContext;
    private static boolean rulesLoaded = false;

    public SpringBootStartupTracer(BeanTracerProperties properties) {
        SpringBootStartupTracer.properties = properties;
        // 构造器里先加载简单规则，复杂类加载等Environment准备好后
        namePatterns.addAll(properties.getNamePatterns());
        trackedPackages.addAll(properties.getBasePackages());
    }

    // ========== 阶段1：极早期（BootstrapRegistryInitializer） ==========
    // 这是最早能介入的时机，甚至SpringApplication都还没完全创建

    @Override
    public void initialize(BootstrapRegistry registry) {
        startupStartTime = System.currentTimeMillis();
        registry.register(SpringBootStartupTracer.class, context -> this);
        enterPhase("BOOTSTRAP", "BootstrapRegistry初始化",
            "极早期，SpringApplication创建前，用于注册启动所需的早期对象");
    }

    // ========== 阶段2：环境准备（通过ApplicationListener） ==========

    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        System.out.println(">>> 收到事件: " + event.getClass().getSimpleName());
        if (event instanceof ApplicationStartingEvent) {
            System.out.println(">>> ApplicationStartingEvent 收到");
            enterPhase("STARTING", "应用启动中 - Environment准备前",
                "Environment 准备前，监听器已注册");
        } else if (event instanceof ApplicationEnvironmentPreparedEvent e) {
            System.out.println(">>> ApplicationEnvironmentPreparedEvent 收到");
            enterPhase("ENV_PREPARED", "Environment 准备完成",
                "application.properties/yml 已加载，ConfigurableEnvironment 就绪");
            loadPropertiesFromEnvironment(e.getEnvironment());
            if (!enabled.get()) {
                if (log == null){
                    System.out.println("[BeanTracer] 已通过配置禁用");
                } else {
                    log.info("[BeanTracer] 已通过配置禁用");
                }
                return;
            }
            loadTraceRules(e.getEnvironment());
        } else if (event instanceof ApplicationContextInitializedEvent e) {
            System.out.println(">>> ApplicationContextInitializedEvent 收到");
            enterPhase("CONTEXT_INIT", "ApplicationContext 初始化完成",
                "AnnotationConfigServletWebServerApplicationContext 已创建");
            this.applicationContext = e.getApplicationContext();
        } else if (event instanceof ContextRefreshedEvent) {
            System.out.println(">>> ContextRefreshedEvent 收到");
            if (!enabled.get()) {
                return;
            }
            enterPhase("REFRESHED", "容器刷新完成",
                "所有 Bean 定义已加载，即将开始实例化");
            printBeanDefinitionReport();
        } else if (event instanceof ApplicationReadyEvent) {
            System.out.println(">>> ApplicationReadyEvent 收到");
            if (!enabled.get()) {
                return;
            }
            enterPhase("READY", "应用就绪",
                "所有 Bean 实例化完成，ApplicationRunner/CommandLineRunner 即将执行");
            printFinalReport();
        } else if (event instanceof ApplicationFailedEvent e) {
            System.out.println(">>> ApplicationFailedEvent 收到");
            enterPhase("FAILED", "启动失败", e.getException().getMessage());
        }
    }

    // ========== 阶段3：Bean定义加载（BeanDefinitionRegistryPostProcessor） ==========
    // 对应图中 2.3 阶段：@ComponentScan -> @Import -> Auto-configuration -> ImportSelector

    @Override
    public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws BeansException {
        if (!enabled.get()) {
            return;
        }

        enterPhase("BEAN_DEFINITION", "Bean定义注册阶段",
            "@ComponentScan扫描 → @Import导入 → 自动配置类加载 → ImportSelector执行");

        String[] allBeanNames = registry.getBeanDefinitionNames();
        int CustomFrameworkCount = 0;
        int componentCount = 0;

        for (String beanName : allBeanNames) {
            log.debug("[定义追踪] beanName: {}", beanName);
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            String beanClassName = bd.getBeanClassName();
            log.debug("[定义追踪] beanClassName: {}", beanClassName);
            if (StrUtil.isEmpty(beanClassName)) {
                continue;
            }

            // 分类统计
            if (beanName.contains("top.flowerstardream.base")) {
                CustomFrameworkCount++;
            } else if (bd.getRole() == BeanDefinition.ROLE_APPLICATION) {
                componentCount++;
            }

            // 追踪目标检测
            if (shouldTrack(beanName, bd)) {
                TrackedBean tb = new TrackedBean(beanName, beanClassName, detectSource(bd));
                beanRegistry.put(beanName, tb);
                log.info("[定义注册] {} ← {} ({})",
                    beanName, tb.getSource(), beanClassName.substring(beanClassName.lastIndexOf('.') + 1));
            }

            // verbose模式打印所有
            if (properties.isVerbose() && !beanRegistry.containsKey(beanName)) {
                log.debug("[定义] {} ({})", beanName, bd.getBeanClassName());
            }
        }

        log.info("[统计] 总Bean定义: {}, 自定义框架组件类: {}, 业务组件: {}, 追踪目标: {}", allBeanNames.length,
            CustomFrameworkCount, componentCount, beanRegistry.size());
        exitPhase("BEAN_DEFINITION");
    }

    // ========== 阶段4：Bean工厂后置处理（BeanFactoryPostProcessor） ==========
    // 对应图中 2.4 阶段：处理@Configuration，可修改Bean定义

    @Override
    public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!enabled.get()) {
            return;
        }
        enterPhase("BEAN_FACTORY", "BeanFactory后置处理阶段",
            "ConfigurationClassPostProcessor执行，@Configuration类处理完成");

        beanRegistry.forEach((name, info) -> {
            boolean exists = beanFactory.containsBeanDefinition(name);
            info.setDefinitionConfirmed(exists);
            log.info("[定义确认] {}: {}", name, exists ? "✓" : "✗");
        });

        exitPhase("BEAN_FACTORY");
    }

    // ========== 阶段5：Bean实例化（InstantiationAwareBeanPostProcessor） ==========
    // 对应图中 2.5 阶段：真正创建对象

    @Override
    public Object postProcessBeforeInstantiation(@NotNull Class<?> beanClass, @NotNull String beanName) throws BeansException {
        if (!enabled.get()) {
            return null;
        }
        TrackedBean info = beanRegistry.get(beanName);
        if (info != null) {
            enterPhase("INSTANTIATE", "实例化: " + beanName,
                "调用构造方法创建对象，尚未注入依赖");
            info.setStatus(Status.INSTANTIATING);
            info.setInstanceStartTime(System.currentTimeMillis());
        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (!enabled.get()) {
            return true;
        }
        TrackedBean info = beanRegistry.get(beanName);
        if (info != null) {
            info.setStatus(Status.INSTANTIATED);
            info.setActualType(bean.getClass().getName());
            log.info("[实例化完成] {} → {}", beanName, bean.getClass().getSimpleName());
        }
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(@NotNull PropertyValues pvs, @NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (!enabled.get()) {
            return pvs;
        }
        TrackedBean info = beanRegistry.get(beanName);
        if (info != null) {
            log.info("[依赖注入] {} 注入 {} 个属性", beanName, pvs.getPropertyValues().length);
        }
        return pvs;
    }

    // ========== 阶段6：Bean初始化（BeanPostProcessor） ==========

    @Override
    public Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (!enabled.get()) {
            return bean;
        }
        TrackedBean info = beanRegistry.get(beanName);
        if (info != null) {
            enterPhase("INIT", "初始化: " + beanName,
                "@PostConstruct → InitializingBean.afterPropertiesSet()");
            info.setStatus(Status.INITIALIZING);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (!enabled.get()) {
            return bean;
        }
        TrackedBean info = beanRegistry.get(beanName);
        if (info != null) {
            info.setStatus(Status.READY);
            info.setInitCostTime(System.currentTimeMillis() - info.getInstanceStartTime());
            log.info("[初始化完成] {} 已就绪，耗时 {}", beanName, getCostTimeFormat(info.getInitCostTime()));
            exitPhase("INIT");
        }
        return bean;
    }

    // ========== 阶段7：所有单例就绪（SmartInitializingSingleton） ==========

    @Override
    public void afterSingletonsInstantiated() {
        if (!enabled.get()) {
            return;
        }
        enterPhase("SINGLETONS_READY", "所有单例Bean实例化完成",
            "非懒加载的单例Bean全部创建完毕，依赖注入完成");

        long readyCount = beanRegistry.values().stream()
            .filter(b -> b.getStatus() == Status.READY).count();
        log.info("[单例统计] 追踪的Bean: {}/{} 已就绪", readyCount, beanRegistry.size());

        exitPhase("SINGLETONS_READY");
    }

    // ========== 阶段8：应用启动完成前（CommandLineRunner） ==========

    @Override
    public void run(String... args) {
        if (!enabled.get()) {
            return;
        }
        enterPhase("STARTUP_COMPLETE", "应用启动完成",
            "CommandLineRunner执行，即将接收请求");

        long totalCost = System.currentTimeMillis() - startupStartTime;
        log.info("[启动耗时] 总耗时: {}", getCostTimeFormat(totalCost));

        exitPhase("STARTUP_COMPLETE");
    }

    // ========== 报告输出 ==========

    private void printBeanDefinitionReport() {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║              Bean定义加载完成 - 追踪目标清单                       ║");
        log.info("╠════════════════════════════════════════════════════════════════╣");

        Map<String, List<TrackedBean>> bySource = beanRegistry.values().stream()
            .collect(Collectors.groupingBy(TrackedBean::getSource));

        bySource.forEach((source, beans) -> {
            log.info("║ 【{}】 ║", source);
            beans.forEach(b -> log.info("║   • {} ║", b.getName()));
        });

        log.info("╚════════════════════════════════════════════════════════════════╝");
    }

    private void printFinalReport() {
        long totalCost = System.currentTimeMillis() - startupStartTime;
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║                  应用启动完成 - Bean生命周期报告                   ║");
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║ 启动总耗时: {} ║", getCostTimeFormat(totalCost));
        log.info("║ 追踪Bean数: {} 个 ║", beanRegistry.size());
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║ Bean名称                          状态     耗时    来源           ║");
        log.info("╠════════════════════════════════════════════════════════════════╣");

        beanRegistry.values().stream()
            .sorted(Comparator.comparing(TrackedBean::getInitCostTime).reversed())
            .forEach(b -> {
                String shortName = b.getName().length() > 30 ?
                    b.getName().substring(0, 27) + "..." : b.getName();
                String costStr = b.getInitCostTime() > 0 ? getCostTimeFormat(b.getInitCostTime()) : "-";
                log.info(String.format("║ %-33s %-8s %6s  %-20s ║",
                    shortName, b.getStatus(), costStr, b.getSource()));
            });

        log.info("╚════════════════════════════════════════════════════════════════╝");
        // 阶段时间线
        log.info("\n[启动阶段时间线]");
        phaseTimeline.forEach(p -> log.info("  {} {} - {}",
            p.phase,
            p.costTime > 0 ? getCostTimeFormat(p.costTime) : "     -",
            p.description));
        log.info("[配置来源] 包路径: {}, 接口: {}, 注解: {}, 模式: {}",
            trackedPackages, trackedInterfaces, trackedAnnotations, namePatterns);
    }

    // ========== 辅助方法 ==========

    /**
     * 从 Environment 手动绑定配置（阶段2关键方法）
     */
    private void loadPropertiesFromEnvironment(ConfigurableEnvironment env) {
        if (propertiesLoaded.get()) {
            return;
        }

        try {
            // 使用 Spring Boot 的 Binder 手动绑定
            properties = Binder.get(env)
                .bind("bean.tracer", BeanTracerProperties.class)
                .orElseGet(BeanTracerProperties::new);

            enabled.set(properties.isEnabled());

            // 加载追踪规则
            if (properties.getBasePackages() != null) {
                trackedPackages.addAll(properties.getBasePackages());
            }
            if (properties.getNamePatterns() != null) {
                namePatterns.addAll(properties.getNamePatterns());
            }
            if (log == null){
                System.out.println("[BeanTracer] 配置加载完成: enabled=" + enabled.get() + ", packages=" + trackedPackages + ", patterns=" + namePatterns);
            } else {
                log.info("[BeanTracer] 配置加载完成: enabled={}, packages={}, patterns={}",
                    enabled.get(), trackedPackages, namePatterns);
            }

        } catch (Exception ex) {
            if (log == null){
                System.out.println("[BeanTracer] 配置绑定失败，使用默认设置: " + ex.getMessage());
            } else {
                log.warn("[BeanTracer] 配置绑定失败，使用默认设置: {}", ex.getMessage());
            }
            properties = new BeanTracerProperties();
        }

        propertiesLoaded.set(true);
    }

    private void enterPhase(String phase, String title, String desc) {
        PhaseRecord record = new PhaseRecord(phase, title, desc, System.currentTimeMillis(), 0);
        phaseTimeline.add(record);
        String str1 = "╔════════════════════════════════════════════════════════════════╗";
        String str2 = "║ 【" + phase + "】" + StrUtil.center(title, 12) + " ║";
        String str3 = "╠════════════════════════════════════════════════════════════════╣";
        String str4 = "║ " + desc + " ║";
        String str5 = "╚════════════════════════════════════════════════════════════════╝";
        if (log == null) {
            System.out.println(str1);
            System.out.println(str2);
            System.out.println(str3);
            System.out.println(str4);
            System.out.println(str5);
        } else {
            log.info(str1);
            log.info("║ 【{}】{} ║", phase, StrUtil.center(title, 12));
            log.info(str3);
            log.info("║ {} ║", desc);
            log.info(str5);
        }
    }

    private void exitPhase(String phase) {
        phaseTimeline.stream()
                .filter(p -> p.phase.equals(phase))
                .findFirst().ifPresent(record -> record.costTime = System.currentTimeMillis() - record.startTime);
    }

    private boolean shouldTrack(String beanName, BeanDefinition bd) {
        String beanClassName = bd.getBeanClassName();
        if (beanClassName == null) {
            return false;
        }

        // 1. 名称模式匹配（最快，无需类加载）
        for (String pattern : namePatterns) {
            if (beanName.contains(pattern) || beanClassName.contains(pattern)) {
                return true;
            }
        }

        // 2. 包路径匹配
        for (String pkg : trackedPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }

        // 3. 注解匹配（需要加载类）
        try {
            Class<?> clazz = Class.forName(beanClassName);
            for (Class<? extends Annotation> ann : trackedAnnotations) {
                if (clazz.isAnnotationPresent(ann)) {
                    return true;
                }
            }
            // 检查是否实现追踪的接口
            for (Class<?> iface : trackedInterfaces) {
                if (iface.isAssignableFrom(clazz)) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            // 类还不可用，跳过
        }

        return false;
    }

    private String detectSource(BeanDefinition bd) {
        if (bd.getBeanClassName() != null) {
            String desc = bd.getBeanClassName();
            log.info("[配置] 追踪来源: {}", desc);
            String source = IBeanDescEnums.findValueByDescGlobal(desc);
            if (source != null) {
                return source;
            }

            for (Class<?> enumClass : IBeanDescEnums.getAllRegisteredEnumClasses()) {
                if (enumClass.isEnum() && IBeanDescEnums.class.isAssignableFrom(enumClass)) {
                    source = checkEnumMatch(enumClass, desc);
                    if (source != null) {
                        return source;
                    }
                }
            }
            source = IBeanDescEnums.findValueByDesc(BeanDescEnums.class, desc);
            if (source != null) {
                return source;
            }
        }
        return "未知";
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E> & IBeanDescEnums> String checkEnumMatch(Class<?> enumClass, String desc) {
        try {
            Class<E> clazz = (Class<E>) enumClass;
            E[] constants = clazz.getEnumConstants();
            if (constants == null) {
                return null;
            }

            for (E enumItem : constants) {
                List<String> descList = enumItem.getDesc();
                if (descList != null) {
                    for (String enumDesc : descList) {
                        if (desc.toLowerCase().contains(enumDesc.toLowerCase())) {
                            return enumItem.getValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[detectSource] 检查枚举匹配失败：{}", enumClass.getName(), e);
        }
        return null;
    }

    private void loadTraceRules(ConfigurableEnvironment env) {
        if (rulesLoaded) {
            return;
        }

        log.info("[配置加载] 从Properties加载追踪规则...");

        // 1. 加载包路径
        trackedPackages.addAll(properties.getBasePackages());
        log.info("[配置] 追踪包路径: {}", trackedPackages);

        // 2. 加载接口类（通过反射）
        for (String className : properties.getInterfaceClasses()) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isInterface()) {
                    trackedInterfaces.add(clazz);
                    log.info("[配置] 追踪接口: {}", clazz.getSimpleName());
                }
            } catch (ClassNotFoundException e) {
                log.warn("[配置] 接口类未找到: {}", className);
            }
        }

        // 3. 加载注解类
        for (String annClass : properties.getAnnotationClasses()) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> clazz = (Class<? extends Annotation>) Class.forName(annClass);
                trackedAnnotations.add(clazz);
                log.info("[配置] 追踪注解: {}", clazz.getSimpleName());
            } catch (ClassNotFoundException e) {
                log.warn("[配置] 注解类未找到: {}", annClass);
            }
        }

        // 4. 加载名称模式
        namePatterns.addAll(properties.getNamePatterns());
        log.info("[配置] 名称模式: {}", namePatterns);

        // 5. 兜底默认值（如果没配置）
        if (trackedInterfaces.isEmpty() && trackedAnnotations.isEmpty()
                && namePatterns.isEmpty() && trackedPackages.isEmpty()) {
            // 默认追踪状态机相关
            namePatterns.addAll(List.of("StateMachine", "Router", "Fsm"));
            log.info("[配置] 使用默认规则: {}", namePatterns);
        }

        rulesLoaded = true;
    }

    private String getCostTimeFormat(long costTime) {
        if (costTime > 60 * 1000) {
            long minutes = costTime / 60000;
            long seconds = (costTime % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        } else if (costTime > 1000) {
            return String.format("%.2fs", costTime / 1000.0);
        } else {
            return costTime + "ms";
        }
    }

    @Override
    public int getOrder() {
        // 确保最先执行，否则会被吞
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

}