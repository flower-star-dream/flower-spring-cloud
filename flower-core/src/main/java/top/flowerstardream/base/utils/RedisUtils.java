package top.flowerstardream.base.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis操作工具类
 * 提供带统一异常处理的Redis操作方法
 *
 * @Author: 花海
 * @Date: 2026/03/29
 * @Description: Redis操作工具类
 */
@Slf4j
@RequiredArgsConstructor
public class RedisUtils {
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 执行Redis操作并统一处理异常
     *
     * @param operation 操作描述
     * @param action    要执行的Redis操作
     * @param <T>       返回值类型
     * @return 操作结果，异常时返回null
     */
    public <T> T execute(String operation, Supplier<T> action) {
        try {
            return action.get();
        } catch (RedisSystemException e) {
            log.error("Redis操作失败 - operation: {}, error: {}", operation, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Redis操作异常 - operation: {}, error: {}", operation, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 执行无返回值的Redis操作
     *
     * @param operation 操作描述
     * @param action    要执行的Redis操作
     * @return 是否执行成功
     */
    public boolean execute(String operation, Runnable action) {
        try {
            action.run();
            return true;
        } catch (RedisSystemException e) {
            log.error("Redis操作失败 - operation: {}, error: {}", operation, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Redis操作异常 - operation: {}, error: {}", operation, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置字符串值（带异常处理）
     *
     * @param key   键
     * @param value 值
     * @return 是否成功
     */
    public boolean set(String key, String value) {
        return execute("set", () -> stringRedisTemplate.opsForValue().set(key, value));
    }

    /**
     * 设置字符串值并指定过期时间（带异常处理）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否成功
     */
    public boolean set(String key, String value, long timeout, TimeUnit unit) {
        return execute("setWithExpire", () -> stringRedisTemplate.opsForValue().set(key, value, timeout, unit));
    }

    /**
     * 获取字符串值（带异常处理）
     *
     * @param key 键
     * @return 值，异常时返回null
     */
    public String get(String key) {
        return execute("get", () -> stringRedisTemplate.opsForValue().get(key));
    }

    /**
     * 删除键（带异常处理）
     *
     * @param key 键
     * @return 是否成功
     */
    public boolean delete(String key) {
        Boolean result = execute("delete", () -> stringRedisTemplate.delete(key));
        return result != null && result;
    }

    /**
     * 设置过期时间（带异常处理）
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        Boolean result = execute("expire", () -> stringRedisTemplate.expire(key, timeout, unit));
        return result != null && result;
    }

    /**
     * 检查键是否存在（带异常处理）
     *
     * @param key 键
     * @return 是否存在，异常时返回false
     */
    public boolean hasKey(String key) {
        Boolean result = execute("hasKey", () -> stringRedisTemplate.hasKey(key));
        return result != null && result;
    }

    /**
     * 获取键的过期时间（带异常处理）
     * @param key 键
     * @param timeUnit 时间单位
     * @return 过期时间
     */
    public long getExpire(String key, TimeUnit timeUnit) {
        return execute("getExpire", () -> stringRedisTemplate.getExpire(key, timeUnit));
    }

    /**
     * 自增键值（带异常处理）
     * @param dailyKey 键
     * @return 自增后的值
     */
    public Long increment(String dailyKey) {
        return execute("increment", () -> stringRedisTemplate.opsForValue().increment(dailyKey));
    }
}
