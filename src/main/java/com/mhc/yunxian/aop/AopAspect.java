package com.mhc.yunxian.aop;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.aop
 * @author: 昊天
 * @date: 2019-02-27 17:48
 * @since V1.1.0-SNAPSHOT
 */
@Aspect
@Component
@Slf4j
public class AopAspect {

    @Pointcut("execution(public * com.mhc.yunxian.controller.*.*(..))")
    public void log() {

    }

    @Before("log()")
    public void exBefore(JoinPoint joinPoint) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        log.info("请求url:{}", request.getRequestURL());
        log.info("请求method:{}", request.getMethod());
        //log.info("请求参数args:{}", JSON.toJSON(joinPoint.getArgs()));
    }

    @After("log()")
    public void exAfter(JoinPoint joinPoint) {
        log.info("class method:{}.{}执行完毕!", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
    }

//    @AfterReturning(returning = "result",pointcut = "log()")
//    public void exAfterRunning(Object result){
//        log.info("执行返回值:{}",JSON.toJSON(result));
//    }

}
