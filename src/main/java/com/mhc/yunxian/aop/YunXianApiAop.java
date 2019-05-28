package com.mhc.yunxian.aop;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.enums.ResultCodeEnum;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.utils.AopUtils;
import com.mhc.yunxian.utils.HttpContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Component
@Aspect
@Slf4j
@Order(1)
public class YunXianApiAop {

    // @Pointcut("execution(public * com.mhc.yunxian.controller.*.*(..))")
    public void controllerPointcut() {

    }

    /**
     * controller切面
     * 如果aop还有什么问题，解决不了，
     * 就使用注解切面的方式,是在不行就注释掉这段代码
     *
     * @param joinPoint
     * @return
     */
    // @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {

        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        Assert.notNull(request, "request must not be null");
        String requestURI = request.getRequestURI();//  /task/user/get/param

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();

        //移除request与response对象
        Object[] args = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)))
                .toArray();

        String params = null;
        Object result;
        Object trueResult;
        Instant start = Instant.now();
        try {
            params = AopUtils.getParams(className, methodName, args, requestURI);
            log.info("uri = {} begin | params : {}", requestURI, params);

            trueResult = result = joinPoint.proceed();
        } catch (DataException e) {
            log.error(e.getMsg(), e);
            trueResult = BaseResponse.error(500, e.getMessage());
            result = BaseResponse.error(500, e.getMessage());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            trueResult = BaseResponse.error(ResultCodeEnum.SERVER_ERROR.getCode(), e.getMessage());
            result = BaseResponse.error(ResultCodeEnum.SERVER_ERROR);
        }

        Instant end = Instant.now();

        //该次请求耗时
        Duration duration = Duration.between(start, end);
        log.info("uri = {} end | duration = {} | result = {}", requestURI, duration, trueResult);

        return result;
    }
}
