package com.mhc.yunxian.constants;

/**
 * Created by zhangyingdong on 2018/05/03.
 */
public interface Type<T> {

    /**
     * 枚举值
     */
    T getKey();

    /**
     * 枚举描述
     */
    String getDesc();
}