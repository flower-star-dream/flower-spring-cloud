package top.flowerstardream.base.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import top.flowerstardream.base.annotation.AsyncBatch;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 批量操作异步执行切面自动配置
 * 拦截带有 @AsyncBatch 注解的方法，根据条件决定是否异步执行
 *
 * @Author: 花海
 * @Date: 2026/04/14
 */
@Slf4j
@Configuration
@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(prefix = "thread-pool.async-batch", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AsyncBatchAspectAutoConfiguration {

    @Bean
    public AsyncBatchAspect asyncBatchAspect(
            @Qualifier("businessExecutor") Executor businessExecutor,
            @Qualifier("commonExecutor") Executor commonExecutor) {
        return new AsyncBatchAspect(businessExecutor, commonExecutor);
    }

    /**
     * 批量操作异步执行切面
     */
    @Aspect
    @Order(100)
    public static class AsyncBatchAspect {

        private final Executor businessExecutor;
        private final Executor commonExecutor;

        // 用于存储正在执行的异步任务，便于监控
        private final Map<String, CompletableFuture<?>> runningTasks = new ConcurrentHashMap<>();

        public AsyncBatchAspect(Executor businessExecutor, Executor commonExecutor) {
            this.businessExecutor = businessExecutor;
            this.commonExecutor = commonExecutor;
        }

        /**
         * 定义切点：所有带有 @AsyncBatch 注解的方法
         */
        @Pointcut("@annotation(top.flowerstardream.base.annotation.AsyncBatch)")
        public void asyncBatchPointcut() {
        }

        /**
         * 环绕通知
         * 根据数据量决定是否异步执行
         */
        @Around("asyncBatchPointcut()")
        public Object aroundAsyncBatch(ProceedingJoinPoint point) throws Throwable {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            AsyncBatch asyncBatch = method.getAnnotation(AsyncBatch.class);

            // 如果禁用，直接同步执行
            if (!asyncBatch.enabled()) {
                return point.proceed();
            }

            // 获取数据量
            int dataSize = getDataSize(point.getArgs());

            // 如果数据量小于阈值，同步执行
            if (dataSize < asyncBatch.threshold()) {
                log.debug("【AsyncBatch】数据量 {} 小于阈值 {}，同步执行: {}.{}",
                        dataSize, asyncBatch.threshold(),
                        point.getTarget().getClass().getSimpleName(), method.getName());
                return point.proceed();
            }

            // 数据量大于等于阈值，异步执行
            log.info("【AsyncBatch】数据量 {} 大于等于阈值 {}，异步执行: {}.{}",
                    dataSize, asyncBatch.threshold(),
                    point.getTarget().getClass().getSimpleName(), method.getName());

            return executeAsync(point, asyncBatch, dataSize);
        }

        /**
         * 执行异步操作
         * 根据方法返回类型决定如何处理返回值
         */
        private Object executeAsync(ProceedingJoinPoint point, AsyncBatch asyncBatch, int dataSize) throws Throwable {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Class<?> returnType = method.getReturnType();

            // 选择线程池
            Executor executor = selectExecutor(asyncBatch.executor());

            // 生成任务ID
            String taskId = generateTaskId(point);

            // 异步执行
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    Object result = point.proceed();
                    long costTime = System.currentTimeMillis() - startTime;
                    log.info("【AsyncBatch】异步任务 [{}] 执行完成，耗时 {}ms，数据量 {}",
                            taskId, costTime, dataSize);
                    return result;
                } catch (Throwable e) {
                    log.error("【AsyncBatch】异步任务 [{}] 执行失败: {}", taskId, e.getMessage(), e);
                    throw new CompletionException(e);
                } finally {
                    runningTasks.remove(taskId);
                }
            }, executor);

            // 存储任务
            runningTasks.put(taskId, future);

            // 根据返回类型处理
            if (returnType == void.class || returnType == Void.class) {
                // void 方法：不等待结果，但添加超时控制
                future.orTimeout(asyncBatch.timeout(), TimeUnit.SECONDS)
                        .exceptionally(ex -> {
                            log.error("【AsyncBatch】异步任务 [{}] 超时或异常: {}", taskId, ex.getMessage());
                            return null;
                        });
                return null;
            } else if (returnType == CompletableFuture.class) {
                // 如果方法本身就返回 CompletableFuture，直接返回
                return future;
            } else {
                // 其他返回类型：等待结果（带超时）
                try {
                    return future.get(asyncBatch.timeout(), TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    log.error("【AsyncBatch】异步任务 [{}] 等待结果超时", taskId);
                    future.cancel(true);
                    throw new RuntimeException("批量操作执行超时", e);
                } catch (Exception e) {
                    log.error("【AsyncBatch】异步任务 [{}] 获取结果异常: {}", taskId, e.getMessage());
                    throw new RuntimeException("批量操作执行异常", e);
                }
            }
        }

        /**
         * 获取数据量大小
         * 从方法参数中查找 Collection 或 Map 类型，获取其大小
         */
        private int getDataSize(Object[] args) {
            if (args == null || args.length == 0) {
                return 0;
            }

            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                // 如果是集合类型
                if (arg instanceof Collection) {
                    return ((Collection<?>) arg).size();
                }
                // 如果是 Map 类型
                if (arg instanceof Map) {
                    return ((Map<?, ?>) arg).size();
                }
                // 如果是数组类型
                if (arg.getClass().isArray()) {
                    return java.lang.reflect.Array.getLength(arg);
                }
            }

            // 如果没有找到集合类型，返回 1（表示有数据但无法确定大小）
            return 1;
        }

        /**
         * 选择线程池
         */
        private Executor selectExecutor(String executorName) {
            return switch (executorName) {
                case "businessExecutor" -> businessExecutor;
                case "commonExecutor" -> commonExecutor;
                default -> businessExecutor;
            };
        }

        /**
         * 生成任务ID
         */
        private String generateTaskId(ProceedingJoinPoint point) {
            return point.getTarget().getClass().getSimpleName() + "." +
                   point.getSignature().getName() + "-" +
                   System.currentTimeMillis();
        }

        /**
         * 获取正在运行的任务数
         */
        public int getRunningTaskCount() {
            return runningTasks.size();
        }

        /**
         * 获取任务列表（用于监控）
         */
        public Map<String, CompletableFuture<?>> getRunningTasks() {
            return new ConcurrentHashMap<>(runningTasks);
        }
    }
}
