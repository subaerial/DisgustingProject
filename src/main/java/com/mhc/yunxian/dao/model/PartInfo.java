package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class PartInfo {
    private Integer id;

    private String openid;

    private String dragonNum;

    private Integer userType;

    private String formId;

    private String prepayId;

    private Date updateTime;

    private Date createTime;

    private String orderNum;
}