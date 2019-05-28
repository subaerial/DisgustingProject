package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class SendAddr {
    private Integer id;

    private String openid;

    private String myAddr;

    /**
     * 用户联系方式
     */
    private String phone;

    private Date updateTime;

    private Date createTime;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 地址编号
     */
    private String addrNum;

    /**
     * 1:是默认，2:不是默认
     */
    private Integer isDefault;
}