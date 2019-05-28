package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderStatisticsListDetail {

    private Date createTime;

    private String nickName;

    private String headImg;

    private int realMoney;

    private int orderStatus;

    private String orderNum;

    List<OrderStatisticsListGoodsDetail> goodsList;

}
