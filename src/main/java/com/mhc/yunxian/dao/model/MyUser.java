package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class MyUser {
    private Integer id;

    private String userOpenid;

    private String myOpenid;

    /**
     * 购买次数
     */
    private Integer buyTime;

    /**
     * 消费总额
     */
    private int totalMoney;

    private Date updateTime;

    private Date createTime;
}