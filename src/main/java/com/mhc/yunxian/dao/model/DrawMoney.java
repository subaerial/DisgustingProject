package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class DrawMoney {
    private Integer id;

    /**
     * 提现金额
     */
    private Integer drawMoney;

    private String openid;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private String formId;

    private Integer drawType;

    private String rate;

    private String drawOrderNum;

    private String paymentNo;

    private Date paymentTime;
}