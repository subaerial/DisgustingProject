package com.mhc.yunxian.cache;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@AllArgsConstructor
public class RemoteCache {

    private StringRedisTemplate redisTemplate;


    public void set(final String key, final String value) {
        redisTemplate.boundValueOps(key).set(value);
    }

    public void set(final String key, final String value, final long timeout, final TimeUnit unit) {
        redisTemplate.boundValueOps(key).set(value,timeout,unit);
    }

    public Long incr(final String key, final long timeout, final TimeUnit unit) {
        Long current = redisTemplate.boundValueOps(key).increment(1L);
        if (current == 1) {
            redisTemplate.boundValueOps(key).expire(timeout, unit);
        }
        return current;
    }


    public Long incr(final String key, final Date date) {
        Long current = redisTemplate.boundValueOps(key).increment(1L);
        if (current == 1) {
            redisTemplate.boundValueOps(key).expireAt(date);
        }
        return current;
    }

    public Long incr(final String key, final long increment) {
        return redisTemplate.boundValueOps(key).increment(increment);
    }

    public String get(final String key) {
        return redisTemplate.boundValueOps(key).get();
    }

    public void del(final String key) {
        redisTemplate.delete(key);
    }

    public void del(final Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    public Boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Boolean expireAt(final String key, final Date date) {
        return redisTemplate.expireAt(key, date);
    }

    public void hset(final String key, final String field, final String value) {
        redisTemplate.boundHashOps(key).put(field, value);
    }

    public void hset(final String key, final Map<String, String> map) {
        redisTemplate.boundHashOps(key).putAll(map);
    }

    public <T> void hset(final String key, final String field, final T value) {
        redisTemplate.boundHashOps(key).put(field, JSON.toJSON(value));
    }

    public void hincr(final String key, final String field) {
        hincr(key, field, 1L);
    }

    public Long hincr(final String key, final String field, final Long value) {
        return redisTemplate.boundHashOps(key).increment(field, value);
    }

    public String hget(final String key, final String field) {
        return (String) redisTemplate.boundHashOps(key).get(field);
    }

    public Map<Object, Object> hgetAll(final String key) {
        return redisTemplate.boundHashOps(key).entries();
    }

    public <T> T hget(final String key, final String field, Class<T> type) {
        String result = hget(key, field);
        if (StringUtils.isNotEmpty(result)) {
            return JSON.parseObject(result, type);
        }
        return null;
    }

    public void hdel(final String key, final String... fields) {
        redisTemplate.boundHashOps(key).delete(fields);
    }

    public void leftPush(final String key, final String value) {
        redisTemplate.boundListOps(key).leftPush(value);
    }

    public String rightPop(final String key) {
        return redisTemplate.boundListOps(key).rightPop();
    }

    public String execute(RedisCallback<String> redisCallback) {
        return redisTemplate.execute(redisCallback);
    }
}
