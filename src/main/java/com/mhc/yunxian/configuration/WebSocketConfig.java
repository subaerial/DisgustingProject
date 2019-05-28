package com.mhc.yunxian.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.configuration
 * @author: 昊天
 * @date: 2019/2/14 3:25 PM
 * @since V1.1.0-SNAPSHOT
 */
@Configuration
public class WebSocketConfig {

    /**
     * 首先需要注入ServerEndpoint，这个Bean会自动注册使 用了@ServerEndpoint注解申明的Websocket endpoint
     * 要注意，如果使用独立的servlet容器，而不是直接使用springboot的内置容器，就不要注入ServerEndpointExporter
     * 因为他将由容器自己提供和管理
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
