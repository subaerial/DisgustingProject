package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class MoneyRecord {
    private Integer id;

    private String openid;

    private Integer money;


    private Integer recordType;

    private Date updateTime;

    private Date createTime;

    private String balance;

    //提现操作使用
    private String drawMoneyId;

    private String cause;//事由

    private String orderNum;

    private Integer status;
}