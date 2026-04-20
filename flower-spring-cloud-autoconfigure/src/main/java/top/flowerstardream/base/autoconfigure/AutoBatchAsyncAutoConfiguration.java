package top.flowerstardream.base.autoconfigure;

import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 自动批量操作异步切面自动配置
 * 自动拦截 MyBatis-Plus 的批量操作方法，无需添加注解
 * 默认只对数据量大于阈值的操作生效
 *
 * @Author: 花海
 * @Date: 2026/04/14
 */
@Slf4j
@Configuration
@ConditionalOnClass({Aspect.class, IService.class})
@ConditionalOnProperty(prefix = "thread-pool.auto-batch", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AutoBatchAsyncAutoConfiguration {

    @Bean
    public AutoBatchAsyncAspect autoBatchAsyncAspect(
            @Qualifier("businessExecutor") Executor businessExecutor,
            @Value("${thread-pool.auto-batch.threshold:50}") int defaultThreshold,
            @Value("${thread-pool.auto-batch.timeout:60}") int defaultTimeout) {
        return new AutoBatchAsyncAspect(businessExecutor, defaultThreshold, defaultTimeout);
    }

    /**
     * 自动批量操作异步切面
     */
    @Aspect
    @Order(200)
    public static class AutoBatchAsyncAspect {

        private final Executor businessExecutor;
        private final int defaultThreshold;
        private final int defaultTimeout;

        public AutoBatchAsyncAspect(Executor businessExecutor, int defaultThreshold, int defaultTimeout) {
            this.businessExecutor = businessExecutor;
            this.defaultThreshold = defaultThreshold;
            this.defaultTimeout = defaultTimeout;
        }

        /**
         * 拦截 MyBatis-Plus IService 的批量操作方法
         */
        @Around("execution(* com.baomidou.mybatisplus.extension.service.IService.saveBatch(java.util.Collection,..)) || " +
                "execution(* com.baomidou.mybatisplus.extension.service.IService.updateBatchById(java.util.Collection,..)) || " +
                "execution(* com.baomidou.mybatisplus.extension.service.IService.saveOrUpdateBatch(java.util.Collection,..))")
        public Object aroundBatchOperation(ProceedingJoinPoint point) throws Throwable {
            Object[] args = point.getArgs();

            // 第一个参数应该是 Collection
            if (args.length == 0 || !(args[0] instanceof Collection<?> collection)) {
                return point.proceed();
            }

            int dataSize = collection.size();

            // 数据量小于阈值，同步执行
            if (dataSize < defaultThreshold) {
                log.debug("【AutoBatch】数据量 {} 小于阈值 {}，同步执行", dataSize, defaultThreshold);
                return point.proceed();
            }

            // 异步执行
            log.info("【AutoBatch】数据量 {} 大于等于阈值 {}，异步执行批量操作", dataSize, defaultThreshold);

            String methodName = point.getTarget().getClass().getSimpleName() + "." + point.getSignature().getName();

            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    Object result = point.proceed();
                    long costTime = System.currentTimeMillis() - startTime;
                    log.info("【AutoBatch】异步批量操作 [{}] 完成，耗时 {}ms，数据量 {}", methodName, costTime, dataSize);
                    return result;
                } catch (Throwable e) {
                    log.error("【AutoBatch】异步批量操作 [{}] 失败: {}", methodName, e.getMessage(), e);
                    throw new CompletionException(e);
                }
            }, businessExecutor);

            // 处理返回值
            Class<?> returnType = point.getSignature().getDeclaringType().getDeclaredMethods()[0].getReturnType();

            if (returnType == boolean.class || returnType == Boolean.class) {
                // 对于返回 boolean 的批量操作，等待结果并返回
                try {
                    return future.get(defaultTimeout, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("【AutoBatch】等待批量操作结果异常: {}", e.getMessage());
                    throw new RuntimeException("批量操作执行异常", e);
                }
            } else {
                // 其他情况直接返回异步结果
                return future.get(defaultTimeout, TimeUnit.SECONDS);
            }
        }

        /**
         * 拦截 removeByIds 批量删除方法
         */
        @Around("execution(* com.baomidou.mybatisplus.extension.service.IService.removeByIds(java.util.Collection))")
        public Object aroundRemoveByIds(ProceedingJoinPoint point) throws Throwable {
            return handleBatchDelete(point);
        }

        /**
         * 拦截 removeBatchByIds 方法
         */
        @Around("execution(* com.baomidou.mybatisplus.extension.service.IService.removeBatchByIds(java.util.Collection,..))")
        public Object aroundRemoveBatchByIds(ProceedingJoinPoint point) throws Throwable {
            return handleBatchDelete(point);
        }

        private Object handleBatchDelete(ProceedingJoinPoint point) throws Throwable {
            Object[] args = point.getArgs();

            if (args.length == 0 || !(args[0] instanceof Collection<?> collection)) {
                return point.proceed();
            }

            int dataSize = collection.size();

            if (dataSize < defaultThreshold) {
                return point.proceed();
            }

            String methodName = point.getTarget().getClass().getSimpleName() + "." + point.getSignature().getName();
            log.info("【AutoBatch】异步批量 [{}] 删除，数据量: {}", methodName, dataSize);

            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return point.proceed();
                } catch (Throwable e) {
                    throw new CompletionException(e);
                }
            }, businessExecutor);

            try {
                return future.get(defaultTimeout, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("【AutoBatch】批量删除 [{}] 异常: {}", methodName, e.getMessage());
                throw new RuntimeException("批量删除执行异常", e);
            }
        }
    }
}
