package com.mhc.yunxian.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @Author MoXiaoFan
 * @Date 2019/1/17 11:32
 */
@Slf4j
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        assertApplicationContext();
        return applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        assertApplicationContext();
        return (T) applicationContext.getBean(beanName);
    }

    public static <T> T getBean(Class<T> requiredType) {
        assertApplicationContext();
        return applicationContext.getBean(requiredType);
    }

    private static void assertApplicationContext() {
        if (SpringContextHolder.applicationContext == null) {
            log.info("applicaitonContext属性为null,请检查是否注入了SpringContextHolder!");
        }
    }
}
