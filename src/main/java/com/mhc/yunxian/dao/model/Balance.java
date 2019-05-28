package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Balance {
    private Integer id;

    private String openid;

    /**
     * 余额,单位：分
     */
    private Integer balance;

    private Date updateTime;

    private Date createTime;
}