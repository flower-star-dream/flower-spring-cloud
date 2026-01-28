package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: 花海
 * @Date: 2025/10/30/19:15
 * @Description: 线程池属性
 */
@Component
@ConfigurationProperties(prefix = "hcd.thread-pool")
@Data
public class ThreadPoolProperties {
    /**
     * 线程池配置基类
     */
    @Data
    public static class ThreadPoolConfig {
        private int corePoolSize; // 核心线程数
        private int maxPoolSize; // 最大线程数
        private int queueCapacity; // 队列容量
        private int keepAliveSeconds; // 线程存活时间
        private String threadNamePrefix; // 线程前缀名
        private String rejectedExecutionHandler; // 拒绝策略
        private boolean waitForTasksToCompleteOnShutdown; // 线程池关闭时等待任务完成
        private int awaitTerminationSeconds; // 线程池关闭时等待任务完成超时时间
    }
    
    /*
     * 业务线程池属性
     */
    private ThreadPoolConfig business = new ThreadPoolConfig();
    
    /*
     * 通用任务线程池属性
     */
    private ThreadPoolConfig common = new ThreadPoolConfig();
    
    /*
     * 异步消息处理线程池属性
     */
    private ThreadPoolConfig message = new ThreadPoolConfig();

}
