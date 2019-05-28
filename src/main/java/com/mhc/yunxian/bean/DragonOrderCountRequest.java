package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 * 接龙-->个人订单统计
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/2/19 9:58 AM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class DragonOrderCountRequest {

    /**
     * 接龙编号
     */
    private String dragonNum;

    /**
     * 用户的sessionId
     */
    private String sessionId;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 每页记录
     */
    private Integer pageSize;

}
