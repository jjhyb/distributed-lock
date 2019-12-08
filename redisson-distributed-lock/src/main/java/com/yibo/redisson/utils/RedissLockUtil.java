package com.yibo.redisson.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author: huangyibo
 * @Date: 2019/12/8 17:23
 * @Description: redis分布式锁工具类
 */
public class RedissLockUtil{

    @Autowired
    private RedissonClient redissonSingle;

    /**
     * 加锁
     * @param lockKey
     * @return
     */
    public RLock lock(String lockKey) {

        return redissonSingle.getLock(lockKey);
    }

    /**
     * 释放锁
     * @param lockKey
     */
    public void unlock(String lockKey) {
        redissonSingle.getLock(lockKey).unlock();
    }

    /**
     * 释放锁
     * @param lock
     */
    public void unlock(RLock lock) {
        lock.unlock();
    }

    /**
     * 带超时的锁
     * @param lockKey
     * @param timeout 超时时间   单位：秒
     */
    public RLock lock(String lockKey, int timeout) {
        RLock lock = redissonSingle.getLock(lockKey);
        lock.lock(timeout,TimeUnit.SECONDS);
        return lock;
    }

    /**
     * 带超时的锁
     * @param lockKey
     * @param unit 时间单位
     * @param timeout 超时时间
     */
    public RLock lock(String lockKey, TimeUnit unit , int timeout) {
        RLock lock = redissonSingle.getLock(lockKey);
        lock.lock(timeout,unit);
        return lock;
    }

    /**
     * 尝试获取锁
     * @param lockKey
     * @param waitTime 最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @return
     */
    public boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        RLock lock = redissonSingle.getLock(lockKey);
        try {
            return lock.tryLock(waitTime,leaseTime,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 尝试获取锁
     * @param lockKey
     * @param unit 时间单位
     * @param waitTime 最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @return
     */
    public boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime) {
        RLock lock = redissonSingle.getLock(lockKey);
        try {
            return lock.tryLock(waitTime,leaseTime,unit);
        } catch (InterruptedException e) {
            return false;
        }
    }
}
