package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 * 此类是消息模版之中用于替换的参数
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/1/24 5:00 PM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class ConvertArgs {

    /**
     * 接龙名称
     */
    private String dragonName;

    /**
     * 自提地址
     */
    private String addr;

    /**
     * 价格
     */
    private Double price;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 发货时间
     */
    private Integer sendTime;

}
