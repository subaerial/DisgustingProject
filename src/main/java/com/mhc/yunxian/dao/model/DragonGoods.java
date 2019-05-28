package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DragonGoods {
    private Integer id;

    private String goodsNum;

    private String dragonNum;

    private Integer currentPrice;

    private Integer currentNumber;

    private Integer limitBuyNum;

    private Date updateTime;

    private Date createTime;

    private String goodsName;

    private String goodsImg;

    private String specification;

    private Byte isHidden;
}