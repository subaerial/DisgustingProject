package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GetMyUserOrder {

    private String nickName;

    private String headImg;

    private Integer orderMoney;

    private Date createTime;

    private String orderNum;

    private Integer orderStatus;

    private String parentOrderNum;

    private List<GetMyUserOrderGoods> goodsList;
    /**
     * 子订单列表
     */
    private List<SubBizOrder> subBizOrderList;

}
