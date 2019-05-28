package com.mhc.yunxian.configuration;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.configuration
 * @author: 昊天
 * @date: 2019/2/14 3:19 PM
 * @since V1.1.0-SNAPSHOT
 */
@Configuration
public class RabbitConfig {

    /**
     * 默认用的是jdk的序列化方式，
     * 现在改成json序列化方法
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
