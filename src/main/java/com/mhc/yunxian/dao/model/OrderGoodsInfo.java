package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderGoodsInfo {
    private Integer id;

    private String openid;

    /**
     * 商品id
     */
    private String goodsNum;

    /**
     * 订单id
     */
    private String orderNum;

    /**
     * 购买价格
     */
    private Integer price;

    private Integer realPrice;

    /**
     * 购买数量
     */
    private Integer buyNumber;

    private Date updateTime;

    private Date createTime;

    private Float buyWeight;

    private String goodsName;

    private String goodsImg;

    private Integer limitBuyNum;
}