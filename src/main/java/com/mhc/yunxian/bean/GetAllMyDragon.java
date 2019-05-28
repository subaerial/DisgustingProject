package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetAllMyDragon implements Serializable {

    /**
     * 创建时间
     */
    private String createTime;
    private String dragonImg;
    private String title;

    /**
     * 参与人数(累计接龙人数)
     */
    private Integer partyNumber;
    /**
     * 截单参与人数
     */
    private Integer cutOffPartyNumber;
    /**
     * 累计收入
     */
    private Integer totalMoney;
    /**
     * 截单收入
     */
    private Integer cutOffTotalMoney;

    private String dragonNum;
    private String subDragonNum;
    /**
     * 退款中订单数
     */
    private Integer refundingNum = 0;
    /**
     * 待发货订单数
     */
    private Integer pendingNum = 0;

    /**
     * 添加本期截单时间
     */
    private String cutOffTime;
    /**
     * 发货时间
     */
    private String sendTime;

    /**
     * 发货周期对象
     */
    private DeliveryCycle deliveryCycle;
}
