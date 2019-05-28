package com.mhc.yunxian.controller;

import com.mhc.yunxian.configuration.WebSocketServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.controller
 * @author: 昊天
 * @date: 2019/2/21 4:27 PM
 * @since V1.1.0-SNAPSHOT
 */
@RestController
public class CountController {

    @GetMapping("/count")
    public int getInfo(){
        return WebSocketServer.getOnlineCount();
    }
}
