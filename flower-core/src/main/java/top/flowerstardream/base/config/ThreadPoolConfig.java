package top.flowerstardream.base.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.flowerstardream.base.properties.ThreadPoolProperties;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 *
 * @author 花海
 * @date 2025-10-14
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    @Resource
    private ThreadPoolProperties threadPoolProperties;


    /**
     * 核心业务线程池
     */
    @Bean("businessExecutor")
    public Executor businessExecutor() {
        ThreadPoolTaskExecutor origin =createThreadPoolExecutor(threadPoolProperties.getBusiness(), "核心业务线程池");
        return TtlExecutors.getTtlExecutor(origin);
    }

    /**
     * 通用任务线程池
     */
    @Bean("commonExecutor")
    public Executor commonExecutor() {
        ThreadPoolTaskExecutor origin = createThreadPoolExecutor(threadPoolProperties.getCommon(), "通用任务线程池");
        return TtlExecutors.getTtlExecutor(origin);
    }

    /**
     * 异步消息处理线程池
     */
    @Bean("messageExecutor")
    public Executor messageExecutor() {
        ThreadPoolTaskExecutor origin = createThreadPoolExecutor(threadPoolProperties.getMessage(), "异步消息处理线程池");
        return TtlExecutors.getTtlExecutor(origin);
    }

    /**
     * 创建线程池执行器的通用方法
     *
     * @param config 线程池配置
     * @param poolName 线程池名称
     * @return 配置好的线程池执行器
     */
    private ThreadPoolTaskExecutor createThreadPoolExecutor(ThreadPoolProperties.ThreadPoolConfig config, String poolName) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(getRejectedExecutionHandler(config.getRejectedExecutionHandler()));
        executor.setWaitForTasksToCompleteOnShutdown(config.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(config.getAwaitTerminationSeconds());
        executor.initialize();
        log.info("{}初始化完成", poolName);
        return executor;
    }

    /**
     * 根据配置字符串获取对应的拒绝策略处理器
     *
     * @param handlerName 拒绝策略名称
     * @return 对应的拒绝策略处理器
     */
    private RejectedExecutionHandler getRejectedExecutionHandler(String handlerName) {
        return switch (handlerName) {
            case "CallerRunsPolicy" -> new ThreadPoolExecutor.CallerRunsPolicy();
            case "DiscardPolicy" -> new ThreadPoolExecutor.DiscardPolicy();
            case "DiscardOldestPolicy" -> new ThreadPoolExecutor.DiscardOldestPolicy();
            default -> new ThreadPoolExecutor.AbortPolicy();
        };
    }
}