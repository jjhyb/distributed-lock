package com.yibo.redisson.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: huangyibo
 * @Date: 2019/12/8 15:29
 * @Description:
 */

@Configuration
@Slf4j
public class RedissonManager {

    @Value("${spring.redis.host}")
    private String host;

    private String password;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.min-idlel}")
    private int minIdle;

    @Value("${spring.redis.lettuce.pool.max-wait}")
    private int maxWaitMillis;

    @Value("${spring.redis.cluster.nodes}")
    private String nodesStr;

    /**
     * 单机模式 redisson 客户端
     */
    @Bean
    public RedissonClient redissonSingle() {
        Config config = new Config();
        String singleAddress = "redis://" + host +":" + port;
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(singleAddress)
                .setTimeout(timeout)
                .setConnectionMinimumIdleSize(minIdle);
        if (!StringUtils.isEmpty(password)) {
            serverConfig.setPassword(password);
        }
        return Redisson.create(config);
    }


    /**
     * 集群模式的 redisson 客户端
     * @return
     */
    @Bean
    public Redisson redisson() {
        //redisson版本是3.5，集群的ip前面要加上“redis://”，不然会报错，3.2版本可不加
        List<String> clusterNodes = new ArrayList<>();
        String[] nodes = nodesStr.split(",");
        for (int i = 0; i < nodes.length; i++) {
            clusterNodes.add("redis://" + nodes[i]);
        }
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config.useClusterServers()
                .addNodeAddress(clusterNodes.toArray(new String[clusterNodes.size()]));
        if (!StringUtils.isEmpty(password)) {
            clusterServersConfig.setPassword(password);//设置密码
        }
        return (Redisson) Redisson.create(config);
    }
}
