package com.mhc.yunxian.cache;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.util.Map;

@Configuration
public class redisConfig extends CachingConfigurerSupport {


    @Bean
    public RemoteCache redisCache(@Autowired @Qualifier("stringRedisTemplate") StringRedisTemplate stringRedisTemplate) {
        return new RemoteCache(stringRedisTemplate);
    }

    @Bean
    public CacheManager cacheManager(@Qualifier("jsonRedisTemplate")RedisTemplate redisTemplate){
        RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
        rcm.setDefaultExpiration(5*60l);
        Map<String,Long> map = Maps.newHashMap();
        map.put(RedisKeys.WXUSER_KEY,5*60l);
        map.put(RedisKeys.LOGIN_KEY,24*60*60l);
        rcm.setExpires(map);
        return rcm;
    }




    @Bean
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, String> jsonRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
