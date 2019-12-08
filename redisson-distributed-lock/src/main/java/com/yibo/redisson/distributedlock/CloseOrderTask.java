package com.yibo.redisson.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author: huangyibo
 * @Date: 2019/12/7 15:58
 * @Description:
 */

@Component
@Slf4j
public class CloseOrderTask {

    //redis 分布式锁的key
    private static final String DISTRIBUTED_LOCK = "DISTRIBUTED_LOCK";

    @Autowired
    private RedissonClient redissonSingle;


    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTask(){
        RLock rLock = redissonSingle.getLock(DISTRIBUTED_LOCK);
        boolean getLock = false;
        try {
            //尝试获取锁，各线程在竞争锁的时候不等待，0秒，5秒释放锁，单位为秒
            getLock = rLock.tryLock(0, 5, TimeUnit.SECONDS);
            if(getLock){
                log.info("Redisson获取到分布式锁：{},ThreadName:{}",DISTRIBUTED_LOCK,Thread.currentThread().getName());
                //执行相关业务......

            }else {
                log.info("Redisson未获取到分布式锁：{},ThreadName:{}",DISTRIBUTED_LOCK,Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson获取到分布式锁异常：{}",e);
        }finally {
            if(!getLock){
                return;
            }
            rLock.unlock();//释放锁
            log.info("Redisson分布式释放");
        }
    }
}
