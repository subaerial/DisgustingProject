package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.Date;

@Data
public class WithdrawMoney{

    private String name;
    private String phone;
    private String openid;
    private Integer drawMoney;
    private Integer status;
    private Date date;//申请时间
    private Integer id;

    private String rate;


}
