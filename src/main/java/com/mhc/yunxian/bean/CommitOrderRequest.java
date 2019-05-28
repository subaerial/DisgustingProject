package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.GoodsInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CommitOrderRequest extends BaseRequest {

    /**
     * 买家openid
     */
    private String openid;
    private String sessionId;
    /**
     * 卖家openid
     */
    private String sellOpenid;
    private String dragonNum;
    /**
     * 付款金额
     */
    private Integer orderMoney;

    private String orderNum;
    private String orderRemark;
    private String addrPhone;

    private String addrName;

    private String address;

    private String dragonAddr;

    private String formId;

    List<GoodsInfo> data;

    private String prepayId;

    private Integer couponUserId;
    /**
     * 订单的配送类型
     */
    private Integer deliveryType;

    /**
     * 订单类型：0：正常订单、1：退差子订单、2：补差子订单
     */
    private String orderType;

}
