package top.flowerstardream.base.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 并行查询工具类
 * 用于并行执行多个 Feign 查询，提升数据组装性能
 *
 * @Author: 花海
 * @Date: 2026/04/14
 */
@Slf4j
@Component
public class ParallelQueryUtil {

    @Resource
    @Qualifier("commonExecutor")
    private Executor commonExecutor;

    @Resource
    @Qualifier("businessExecutor")
    private Executor businessExecutor;

    /**
     * 并行执行两个查询并合并结果
     *
     * @param query1   第一个查询
     * @param query2   第二个查询
     * @param merger   结果合并函数
     * @param timeout  超时时间（秒）
     * @return 合并后的结果
     */
    public <T1, T2, R> R parallelQuery(
            Supplier<T1> query1,
            Supplier<T2> query2,
            Merger2<T1, T2, R> merger,
            long timeout) {

        long startTime = System.currentTimeMillis();

        CompletableFuture<T1> future1 = CompletableFuture.supplyAsync(query1, businessExecutor);
        CompletableFuture<T2> future2 = CompletableFuture.supplyAsync(query2, businessExecutor);

        try {
            CompletableFuture.allOf(future1, future2).get(timeout, TimeUnit.SECONDS);
            T1 result1 = future1.get();
            T2 result2 = future2.get();

            long costTime = System.currentTimeMillis() - startTime;
            log.debug("【ParallelQuery】并行查询完成，耗时 {}ms", costTime);

            return merger.merge(result1, result2);
        } catch (Exception e) {
            log.error("【ParallelQuery】并行查询异常: {}", e.getMessage());
            throw new RuntimeException("并行查询失败", e);
        }
    }

    /**
     * 并行执行三个查询并合并结果
     */
    public <T1, T2, T3, R> R parallelQuery(
            Supplier<T1> query1,
            Supplier<T2> query2,
            Supplier<T3> query3,
            Merger3<T1, T2, T3, R> merger,
            long timeout) {

        long startTime = System.currentTimeMillis();

        CompletableFuture<T1> future1 = CompletableFuture.supplyAsync(query1, businessExecutor);
        CompletableFuture<T2> future2 = CompletableFuture.supplyAsync(query2, businessExecutor);
        CompletableFuture<T3> future3 = CompletableFuture.supplyAsync(query3, businessExecutor);

        try {
            CompletableFuture.allOf(future1, future2, future3).get(timeout, TimeUnit.SECONDS);
            T1 result1 = future1.get();
            T2 result2 = future2.get();
            T3 result3 = future3.get();

            long costTime = System.currentTimeMillis() - startTime;
            log.debug("【ParallelQuery】并行查询完成，耗时 {}ms", costTime);

            return merger.merge(result1, result2, result3);
        } catch (Exception e) {
            log.error("【ParallelQuery】并行查询异常: {}", e.getMessage());
            throw new RuntimeException("并行查询失败", e);
        }
    }

    /**
     * 并行执行多个相同类型的查询
     *
     * @param queries  查询列表
     * @param merger   结果合并函数
     * @param timeout  超时时间（秒）
     * @return 合并后的结果
     */
    @SafeVarargs
    public final <T, R> R parallelQueries(
            Function<List<T>, R> merger,
            long timeout,
            Supplier<List<T>>... queries) {

        long startTime = System.currentTimeMillis();

        @SuppressWarnings("unchecked")
        CompletableFuture<List<T>>[] futures = new CompletableFuture[queries.length];
        for (int i = 0; i < queries.length; i++) {
            futures[i] = CompletableFuture.supplyAsync(queries[i], businessExecutor);
        }

        try {
            CompletableFuture.allOf(futures).get(timeout, TimeUnit.SECONDS);

            List<T> allResults = new java.util.ArrayList<>();
            for (CompletableFuture<List<T>> future : futures) {
                allResults.addAll(future.get());
            }

            long costTime = System.currentTimeMillis() - startTime;
            log.debug("【ParallelQuery】并行查询 {} 个任务完成，耗时 {}ms", queries.length, costTime);

            return merger.apply(allResults);
        } catch (Exception e) {
            log.error("【ParallelQuery】并行查询异常: {}", e.getMessage());
            throw new RuntimeException("并行查询失败", e);
        }
    }

    /**
     * 带超时和默认值的单个查询
     *
     * @param query        查询操作
     * @param defaultValue 超时时的默认值
     * @param timeout      超时时间（秒）
     * @return 查询结果或默认值
     */
    public <T> T queryWithFallback(Supplier<T> query, T defaultValue, long timeout) {
        try {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(query, commonExecutor);
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("【ParallelQuery】查询超时，返回默认值");
            return defaultValue;
        } catch (Exception e) {
            log.error("【ParallelQuery】查询异常: {}", e.getMessage());
            return defaultValue;
        }
    }

    // 合并器接口
    @FunctionalInterface
    public interface Merger2<T1, T2, R> {
        R merge(T1 t1, T2 t2);
    }

    @FunctionalInterface
    public interface Merger3<T1, T2, T3, R> {
        R merge(T1 t1, T2 t2, T3 t3);
    }
}
