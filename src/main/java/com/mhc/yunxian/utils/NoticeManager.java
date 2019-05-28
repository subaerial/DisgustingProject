package com.mhc.yunxian.utils;

import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通知异步执行
 * @Author MoXiaoFan
 * @Date 2019/1/17 10:54
 */
public class NoticeManager {

    /**
     * 操作延时
     */
    private final static int OPERATE_DELAY_TIME = 10;

    /**
     * 异步发送消息线程池
     */
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(20);

    private static NoticeManager ourInstance = new NoticeManager();

    public static NoticeManager me() {
        return ourInstance;
    }

    private NoticeManager() {
    }


    public void executorNotice(TimerTask task){
        executor.schedule(task,OPERATE_DELAY_TIME,TimeUnit.MILLISECONDS);
    }

}
