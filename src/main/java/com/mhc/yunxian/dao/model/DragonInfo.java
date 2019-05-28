package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class DragonInfo {
    private Integer id;

    /**
     * 订单号
     */
    private String dragonNum;

    /**
     * 订单标题
     */
    private String dragonTitle;

    /**
     * 订单描述
     */
    private String dragonDesc;

    /**
     * 订单图片
     */
    private String dragonImg;

    /**
     * 截至时间
     */
    private String endTime;

    private String sendTime;

    /**
     * 是否货到付款 0:否; 1:是
     */
    private Integer cashOnDelivery;

    /**
     * 自提地址
     */
    private String addr;

    /**
     * 创建者openid
     */
    private String openid;

    /**
     * 订单状态  0:进行中，1:已结束
     */
    private Integer dragonStatus;

    private Date updateTime;

    private Date createTime;

    /**
     * 联系方式
     */
    private String phone;

    private Integer isPayLater;

    private String subDragonNum;

    private Integer isDelivery;

    private Integer globalLimit;

    private Byte isCoupon;

    /**
     * 接龙视频文件地址
     */
    private String dragonVideo;

    /**
     * 截单时间
     */
    private String cutOffTime;

    /**
     * 周期性发货时间
     */
    private String deliveryCycle;

    /**
     * 浏览时间
     */
    private Date browseTime;

}