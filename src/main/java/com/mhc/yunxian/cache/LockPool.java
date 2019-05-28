package com.mhc.yunxian.cache;

import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * 分布式锁
 * @author 昊天
 * @vsersion V1.0
 */
public class LockPool {

    private static final String LOOK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;


    /**
     * 尝试获取分布式锁
     * @param jedis       Redis客户端
     * @param lockKey     锁
     * @param requestId   请求标识
     * @param expireTime  超期时间
     * @return            是否获取成功
     */
    public static boolean tryGetDistributeLock(Jedis jedis, String lockKey, String requestId, int expireTime){
        /**
         * 第一个参数：key，是唯一的
         * 第二个参数：value,通过给value赋值为requestId，我们就知道这把锁是哪个请求加的了，在解锁的时候就可以有依据
         * 第三个参数：nxxx，这个参数我们填的是NX，意思是SET IF NOT EXIST，即当key不存在时，我们进行set操作；若key已经存在，则不做任何操作
         * 第四个参数：expx，这个参数我们传的是PX，意思是我们要给这个key加一个过期的设置，具体时间由第五个参数决定
         * 第五个参数：time，与第四个参数相呼应，代表key的过期时间。
         */
        String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        if(LOOK_SUCCESS.equals(result)){
            return true;
        }
        return false;
    }

    /**
     * 释放分布式锁
     * @param jedis      Redis客户端
     * @param lockKey    锁
     * @param requestId  请求标识
     * @return           是否获取成功
     */
    public static boolean releaseDistributeLock(Jedis jedis,String lockKey,String requestId){
        /**
         * script：Lua脚本代码，首先获取锁对应的value值，检查是否与requestId相等，如果相等则删除锁（解锁）。上述操作是原子性的
         */
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        if(RELEASE_SUCCESS.equals(result)){
            return true;
        }
        return false;
    }
}
