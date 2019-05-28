package com.mhc.yunxian.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class HttpClientController {


    /**
     * 获取用户IP函数
     * @param request
     * @return
     */
    public String getClientIp(HttpServletRequest request) {

        try {
            if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
                return (request.getHeader("X-Real-IP"));
            }


            String ip = request.getHeader("x-forwarded-for");

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;

        } catch (Exception e) {
            e.printStackTrace();
            log.info("get client ip error: {}",e.getMessage());
        }

        return "0.0.0.0";
    }


}