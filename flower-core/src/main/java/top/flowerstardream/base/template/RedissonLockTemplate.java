package top.flowerstardream.base.template;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static top.flowerstardream.base.exception.ExceptionEnum.FAILED_TO_ACQUIRE_THE_LOCK;

/**
 * @Author: 花海
 * @Date: 2025/04/14/13:00
 * @Description: Redisson锁模板
 */
@Component
@RequiredArgsConstructor
public class RedissonLockTemplate {
    
    private final RedissonClient redissonClient;
    
    /**
     * 带返回值的锁操作
     */
    public <T> T execute(String lockKey, long waitSec, long leaseSec, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        
        try {
            locked = lock.tryLock(waitSec, leaseSec, TimeUnit.SECONDS);
            if (!locked) {
                throw FAILED_TO_ACQUIRE_THE_LOCK.toException(lockKey);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    /**
     * 无返回值的锁操作
     */
    public void execute(String lockKey, Runnable runnable) {
        execute(lockKey, 3, -1, () -> {
            runnable.run();
            return null;
        });
    }
    
    /**
     * 看门狗续期模式
     */
    public void executeWithWatchdog(String lockKey, long waitSec, Runnable runnable) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        
        try {
            // 不传 leaseTime = 看门狗
            locked = lock.tryLock(waitSec, TimeUnit.SECONDS);
            if (!locked) {
                throw FAILED_TO_ACQUIRE_THE_LOCK.toException(lockKey);
            }
            runnable.run();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}