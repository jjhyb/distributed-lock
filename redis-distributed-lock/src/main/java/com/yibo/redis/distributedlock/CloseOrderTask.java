package com.yibo.redis.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: huangyibo
 * @Date: 2019/12/7 15:58
 * @Description:
 */

@Component
@Slf4j
public class CloseOrderTask {

    public void closeOrderTask(){
        log.info("------------关闭订单定时任务启动------------");
        long lockTimeout = 5000;


        log.info("------------关闭订单定时任务结束------------");
    }
}
