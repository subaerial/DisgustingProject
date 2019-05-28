package com.mhc.yunxian.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * jedis生成工具
 */
@Component
@Slf4j
public class JedisProducer {

    @Value("${spring.redis.host}")
    private  String host ;

    @Value("${spring.redis.port}")
    private  Integer port ;

    @Value("${spring.redis.pool.maxActive}")
    private  Integer maxActive ;

    @Value("${spring.redis.pool.maxIdle}")
    private  Integer maxIdle;

    @Value("${spring.redis.timeout}")
    private  Integer timeOut;

    @Value("${spring.redis.pool.maxWait}")
    private  Integer maxWaitMillis;

    @Value("${spring.redis.password}")
    private  String  password;

    private static JedisPool jedisPool;

    private synchronized void  initializePool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxActive);
        config.setMaxIdle(maxIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config,host,port,timeOut,password);
    }

    public Jedis getJedis(){
        initializePool();
        int timeoutCount = 0;
        while (true) {
            try {
                if (null != jedisPool) {
                    return jedisPool.getResource();
                }
            } catch (Exception e) {
                if (e instanceof JedisConnectionException) {
                    timeoutCount++;
                    log.warn("getJedis timeoutCount={}", timeoutCount);
                    if (timeoutCount > 3) {
                        break;
                    }
                } else {
                    log.warn("jedisInfo ... NumActive=" + jedisPool.getNumActive()
                            + ", NumIdle=" + jedisPool.getNumIdle()
                            + ", NumWaiters=" + jedisPool.getNumWaiters()
                            + ", isClosed=" + jedisPool.isClosed());
                    log.error("GetJedis error,", e);
                    break;
                }
            }
            break;
        }
        return null;
    }



}
