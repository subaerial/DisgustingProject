package com.mhc.yunxian;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;


@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableRabbit
@CrossOrigin
@MapperScan("com.mhc.yunxian.dao")
public class YunxianApplication {


    public static void main(String[] args) {
        SpringApplication.run(YunxianApplication.class, args);


    }
}
