package com.yibo.jedis.distributedlock;

import com.yibo.jedis.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;

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

    private static final long lockTimeout = 5000;

    /**
     * 使用Tomcat的shutdown关闭Tomcat，Tomcat在关闭之前会调用@PreDestroy的方法
     *
     * 如果要关闭的东西非常多，那么这种方式就不可型
     * 如果直接kill掉Tomcat进程，此种方法没有意义
     */
    @PreDestroy
    public void delLock(){
        redisUtil.del(DISTRIBUTED_LOCK);
        log.info("释放{},ThreadName:{}",DISTRIBUTED_LOCK,Thread.currentThread().getName());
    }

    @Autowired
    private RedisUtil redisUtil;

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV1(){
        log.info("------------关闭订单定时任务启动------------");

        Long setnxResult = redisUtil.setnx(DISTRIBUTED_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if(setnxResult != null && setnxResult == 1){
            //如果返回值为1，代表设置成功，获取锁
            this.closeOrder(DISTRIBUTED_LOCK);
        }else {
            log.info("没有获得分布式锁：{}",DISTRIBUTED_LOCK);
        }

        log.info("------------关闭订单定时任务结束------------");
    }

    /**
     * 设置分布式锁的有效期
     * @param lockName
     */
    private void closeOrder(String lockName){
        //有效期5秒，防止死锁
        redisUtil.expire(lockName,50);
        log.info("获取{},ThreadName:{}",lockName,Thread.currentThread().getName());
        //执行关闭订单业务。。。。

        //业务执行完毕之后，释放分布式锁
        redisUtil.del(lockName);
        log.info("释放{},ThreadName:{}",lockName,Thread.currentThread().getName());
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV2(){
        log.info("------------关闭订单定时任务启动------------");

        Long setnxResult = redisUtil.setnx(DISTRIBUTED_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if(setnxResult != null && setnxResult == 1){
            //如果返回值为1，代表设置成功，获取锁
            this.closeOrder(DISTRIBUTED_LOCK);
        }else {
            //未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = redisUtil.get(DISTRIBUTED_LOCK);
            if(lockValueStr == null || (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr))){
                //锁存在，但是锁已经超时，那么用新的值替换旧的值，并返回旧的值
                //再次用当前时间戳进行getset
                //返回给定key的旧值，用旧值判断，是否可以获取锁
                //当key没有旧值时，即key不存在时，返回nil，获取锁
                //这里我们set了一个新的value值，获取旧的值
                String getsetResult = redisUtil.getset(DISTRIBUTED_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));

                if(StringUtils.isEmpty(getsetResult) || (!StringUtils.isEmpty(getsetResult) && getsetResult.equals(lockValueStr))){
                    //真正获取到分布式锁
                    this.closeOrder(DISTRIBUTED_LOCK);
                }else {
                    log.info("没有获得分布式锁：{}",DISTRIBUTED_LOCK);
                }
            }else{
                log.info("没有获得分布式锁：{}",DISTRIBUTED_LOCK);
            }
        }

        log.info("------------关闭订单定时任务结束------------");
    }
}
