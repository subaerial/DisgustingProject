package com.mhc.yunxian.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class OrderInfo {
    private Integer id;

    private String openid;

    private String orderNum;

    private String dragonNum;

    private String transactionId;

    private Integer orderStatus;

    private Date updateTime;//付款时间

    private Date createTime;//下单时间

    private String orderRemark;

    private Integer orderMoney;

    private String addrPhone;

    private String addrName;

    private String address;

    private Date sendTime;//发货时间

    private Date comfirmTime;//确认收货时间

    private Integer isCod;//是否货到付款

    private Integer isPayLater;//是否后付款

    private Integer sendNoticeNum; //发送通知次数

    private String parentOrderNum;

    private String dragonAddr;

    private Integer couponUserId;

    private Integer couponAmount;

    private String sellerDesc;

    private Integer isDelivery;

    /**
     * 订单类型，参照OrderTypeEnum
     */
    private Integer orderType;

}